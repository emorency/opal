/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * An Opal implementation of {@code PrivateVariableEntityMap}, on top of a Magma {@code ValueTable}.
 */
public class OpalPrivateVariableEntityMap implements PrivateVariableEntityMap {

  private static final Logger log = LoggerFactory.getLogger(OpalPrivateVariableEntityMap.class);

  @NotNull
  private final ValueTable keysValueTable;

  @NotNull
  private final Variable ownerVariable;

  @NotNull
  private final IdentifierGenerator participantIdentifier;

  @NotNull
  private final BiMap<VariableEntity, VariableEntity> publicToPrivate = HashBiMap.create();

  public OpalPrivateVariableEntityMap(@NotNull ValueTable keysValueTable, @NotNull Variable ownerVariable,
      @NotNull IdentifierGenerator participantIdentifier) {
    Assert.notNull(keysValueTable, "keysValueTable cannot be null");
    Assert.notNull(ownerVariable, "ownerVariable cannot be null");
    Assert.notNull(participantIdentifier, "participantIdentifier cannot be null");

    this.keysValueTable = keysValueTable;
    this.ownerVariable = ownerVariable;
    this.participantIdentifier = participantIdentifier;

    constructCache();
  }

  @Override
  public VariableEntity publicEntity(@NotNull VariableEntity privateEntity) {
    Assert.notNull(privateEntity, "privateEntity cannot be null");
    VariableEntity entity = publicToPrivate.inverse().get(privateEntity);
    log.debug("({}) <--> {}", privateEntity.getIdentifier(), entity == null ? null : entity.getIdentifier());
    return entity;
  }

  @Override
  public VariableEntity privateEntity(@NotNull VariableEntity publicEntity) {
    Assert.notNull(publicEntity, "publicEntity cannot be null");
    VariableEntity entity = publicToPrivate.get(publicEntity);
    log.debug("{} <--> ({})", publicEntity.getIdentifier(), entity == null ? null : entity.getIdentifier());
    return entity;
  }

  @Override
  public boolean hasPrivateEntity(@NotNull VariableEntity privateEntity) {
    Assert.notNull(privateEntity, "privateEntity cannot be null");
    return publicToPrivate.inverse().containsKey(privateEntity);
  }

  @Override
  public boolean hasPublicEntity(@NotNull VariableEntity publicEntity) {
    Assert.notNull(publicEntity, "publicEntity cannot be null");
    return publicToPrivate.containsKey(publicEntity);
  }

  @Override
  public VariableEntity createPrivateEntity(@NotNull VariableEntity publicEntity) {
    Assert.notNull(publicEntity, "publicEntity cannot be null");
    for(int i = 0; i < 100; i++) {
      VariableEntity privateEntity = entityFor(participantIdentifier.generateIdentifier());
      if(!publicToPrivate.inverse().containsKey(privateEntity)) {
        writeEntities(keysValueTable, publicEntity, privateEntity);
        log.debug("{} <--> ({}) added", publicEntity.getIdentifier(), privateEntity.getIdentifier());
        publicToPrivate.put(entityFor(publicEntity.getIdentifier()), privateEntity);
        return privateEntity;
      }
    }
    throw new IllegalStateException(
        "Unable to generate a unique private entity for the owner [" + ownerVariable + "] and public entity [" +
            publicEntity.getIdentifier() + "]. One hundred attempts made.");
  }

  @Override
  public VariableEntity createPublicEntity(@NotNull VariableEntity privateEntity) {
    Assert.notNull(privateEntity, "privateEntity cannot be null");
    for(int i = 0; i < 100; i++) {
      VariableEntity publicEntity = entityFor(participantIdentifier.generateIdentifier());
      if(!publicToPrivate.containsKey(publicEntity)) {
        writeEntities(keysValueTable, publicEntity, privateEntity);
        publicToPrivate.put(publicEntity, entityFor(privateEntity.getIdentifier()));
        return publicEntity;
      }
    }
    throw new IllegalStateException(
        "Unable to generate a unique public entity for the owner [" + ownerVariable + "] and private entity [" +
            privateEntity.getIdentifier() + "]. One hundred attempts made.");
  }

  private void writeEntities(ValueTable keyTable, VariableEntity publicEntity, VariableEntity privateEntity) {
    ValueTableWriter vtw = keyTable.getDatasource().createWriter(keyTable.getName(), keyTable.getEntityType());
    ValueSetWriter vsw = vtw.writeValueSet(publicEntity);
    vsw.writeValue(ownerVariable, TextType.get().valueOf(privateEntity.getIdentifier()));
    vsw.close();
    vtw.close();
    log.debug("{} <--> ({}) added", publicEntity.getIdentifier(), privateEntity.getIdentifier());
  }

  private VariableEntity entityFor(String identifier) {
    return new VariableEntityBean(keysValueTable.getEntityType(), identifier);
  }

  private void constructCache() {
    log.info("Constructing participant identifier cache for keysValueTable: {}, ownerVariable: {}",
        keysValueTable.getName(), ownerVariable.getName());
    VariableValueSource ownerVariableSource = keysValueTable.getVariableValueSource(ownerVariable.getName());
    if(ownerVariableSource.supportVectorSource()) {
      constructCacheFromVector(keysValueTable, ownerVariableSource.asVectorSource());
    } else {
      constructCacheFromTable(keysValueTable, ownerVariableSource);
    }
    log.debug("Cache constructed: {}", publicToPrivate);
  }

  private void constructCacheFromTable(@NotNull ValueTable keyTable, @NotNull ValueSource ownerVariableSource) {
    for(ValueSet valueSet : keyTable.getValueSets()) {
      Value value = ownerVariableSource.getValue(valueSet);
      // OPAL-619: The value could be null, in which case don't cache it. Whenever new participant
      // data are imported, the key variable is written first and its corresponding VariableValueSource
      // is added *without a value* (see DefaultImportService.createKeyVariable()). The key variable's
      // value is written afterwards, in the process of copying the participant data to the destination
      // datasource. Also, more obviously, it is not necessarily the case that all value sets have a value
      // for all key variables.
      if(!value.isNull()) {
        log.debug("{} <--> ({}) cached", valueSet.getVariableEntity().getIdentifier(), value);
        publicToPrivate.put(entityFor(valueSet.getVariableEntity().getIdentifier()), entityFor(value.toString()));
      }
    }
  }

  private void constructCacheFromVector(@NotNull ValueTable keyTable, @NotNull VectorSource vs) {
    SortedSet<VariableEntity> entities = new TreeSet<>(keyTable.getVariableEntities());
    Iterator<Value> values = vs.getValues(entities).iterator();
    for(VariableEntity opalEntity : entities) {
      Value value = values.next();
      if(!value.isNull()) {
        log.debug("{} <--> ({}) cached", opalEntity.getIdentifier(), value);
        publicToPrivate.put(entityFor(opalEntity.getIdentifier()), entityFor(value.toString()));
      }
    }
  }
}
