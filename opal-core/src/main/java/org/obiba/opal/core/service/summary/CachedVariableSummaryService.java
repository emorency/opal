package org.obiba.opal.core.service.summary;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.math.summary.AbstractVariableSummary;
import org.obiba.magma.math.summary.CategoricalVariableSummary;
import org.obiba.magma.math.summary.CategoricalVariableSummaryFactory;
import org.obiba.magma.math.summary.ContinuousVariableSummary;
import org.obiba.magma.math.summary.ContinuousVariableSummaryFactory;
import org.obiba.magma.math.summary.DefaultVariableSummary;
import org.obiba.magma.math.summary.DefaultVariableSummaryFactory;
import org.obiba.magma.math.summary.VariableSummary;
import org.obiba.magma.math.summary.VariableSummaryFactory;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.service.VariableSummaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

@Component
public class CachedVariableSummaryService implements VariableSummaryService {

  private static final Logger log = LoggerFactory.getLogger(CachedVariableSummaryService.class);

  @org.springframework.beans.factory.annotation.Value("${org.obiba.opal.cache.variableSummaries}")
  private boolean cacheSummaries;

  @Autowired
  private CacheManager cacheManager;

  private final Map<Class<? extends VariableSummaryFactory<?>>, AbstractVariableSummaryCachedService<?, ?, ?>>
      summaryServices = new HashMap<>();

  public CachedVariableSummaryService() {
    summaryServices.put(ContinuousVariableSummaryFactory.class, new ContinuousVariableSummaryCachedService());
    summaryServices.put(CategoricalVariableSummaryFactory.class, new CategoricalVariableSummaryCachedService());
    summaryServices.put(DefaultVariableSummaryFactory.class, new DefaultVariableSummaryCachedService());
  }

  @SuppressWarnings("unchecked")
  private <TVariableSummary extends VariableSummary, //
      TVariableSummaryFactory extends VariableSummaryFactory<TVariableSummary>, //
      TVariableSummaryBuilder extends AbstractVariableSummary.VariableSummaryBuilder<TVariableSummary, TVariableSummaryBuilder>> //
  AbstractVariableSummaryCachedService<TVariableSummary, TVariableSummaryFactory, TVariableSummaryBuilder> //
  getService(Class<? extends VariableSummaryFactory<TVariableSummary>> factoryClass) {
    return (AbstractVariableSummaryCachedService<TVariableSummary, TVariableSummaryFactory, TVariableSummaryBuilder>) summaryServices
        .get(factoryClass);
  }

  @Override
  public void stackVariable(@NotNull ValueTable valueTable, @NotNull Variable variable, @NotNull Value value) {
    if(!cacheSummaries) log.debug("Variable summaries cache disabled!");

    // skip binary variable
    Preconditions.checkArgument(!BinaryType.get().equals(variable.getValueType()),
        "Cannot compute summary for binary variable " + variable.getName());

    getService(CategoricalVariableSummaryFactory.class).getSummaryBuilder(valueTable, variable).addValue(value);

    if(variable.getValueType().isNumeric()) {
      getService(ContinuousVariableSummaryFactory.class).getSummaryBuilder(valueTable, variable).addValue(value);
    } else {
      getService(DefaultVariableSummaryFactory.class).getSummaryBuilder(valueTable, variable).addValue(value);
    }
  }

  @Override
  public void computeSummaries(@NotNull ValueTable table) {
    if(!cacheSummaries) {
      log.info("Variable summaries cache disabled!");
      return;
    }
    Stopwatch stopwatch = Stopwatch.createStarted();
    for(AbstractVariableSummaryCachedService<?, ?, ?> summaryService : summaryServices.values()) {
      summaryService.computeAndCacheSummaries(table);
    }
    clearComputingSummaries(table);
    log.info("Variables summaries for {} computed in {}", table.getTableReference(), stopwatch.stop());
  }

  @Override
  public void clearComputingSummaries(@NotNull ValueTable valueTable) {
    if(!cacheSummaries) return;
    for(AbstractVariableSummaryCachedService<?, ?, ?> summaryService : summaryServices.values()) {
      summaryService.clearComputingSummaries(valueTable);
    }
  }

  @NotNull
  @Override
  @SuppressWarnings("unchecked")
  public <TVariableSummary extends VariableSummary, //
      TVariableSummaryFactory extends VariableSummaryFactory<TVariableSummary>> TVariableSummary getSummary(
      @NotNull VariableSummaryFactory<TVariableSummary> summaryFactory, boolean refreshCache) {
    return getService((Class<TVariableSummaryFactory>) summaryFactory.getClass())
        .getSummary(summaryFactory, refreshCache);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <TVariableSummary extends VariableSummary, //
      TVariableSummaryFactory extends VariableSummaryFactory<TVariableSummary>> boolean isSummaryCached(
      @NotNull VariableSummaryFactory<TVariableSummary> summaryFactory) {
    return getService((Class<TVariableSummaryFactory>) summaryFactory.getClass()).isSummaryCached(summaryFactory);
  }

  private class ContinuousVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<ContinuousVariableSummary, ContinuousVariableSummaryFactory, ContinuousVariableSummary.Builder> {

    @NotNull
    @Override
    protected Cache getCache() {
      return cacheManager.getCache("opal-variable-summary-continuous");
    }

    @NotNull
    @Override
    protected ContinuousVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      return new ContinuousVariableSummary.Builder(variable, ContinuousVariableSummary.Distribution.normal);
    }
  }

  private class CategoricalVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<CategoricalVariableSummary, CategoricalVariableSummaryFactory, CategoricalVariableSummary.Builder> {

    @NotNull
    @Override
    protected Cache getCache() {
      return cacheManager.getCache("opal-variable-summary-categorical");
    }

    @NotNull
    @Override
    protected CategoricalVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      boolean distinct = TextType.get().equals(variable.getValueType()) && variable.areAllCategoriesMissing();
      return new CategoricalVariableSummary.Builder(variable).distinct(distinct);
    }
  }

  private class DefaultVariableSummaryCachedService extends
      AbstractVariableSummaryCachedService<DefaultVariableSummary, DefaultVariableSummaryFactory, DefaultVariableSummary.Builder> {

    @NotNull
    @Override
    protected Cache getCache() {
      return cacheManager.getCache("opal-variable-summary-default");
    }

    @NotNull
    @Override
    protected DefaultVariableSummary.Builder newVariableSummaryBuilder(@NotNull Variable variable) {
      return new DefaultVariableSummary.Builder(variable);
    }
  }

}