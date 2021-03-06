/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.obiba.opal.core.cfg.TaxonomyService;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.api.Assertions.assertThat;

@ContextConfiguration(classes = TaxonomyServiceImplTest.Config.class)
public class TaxonomyServiceImplTest extends AbstractJUnit4SpringContextTests {

  private static final Logger log = LoggerFactory.getLogger(TaxonomyServiceImplTest.class);

  @Autowired
  private TaxonomyService taxonomyService;

  @Autowired
  private OrientDbService orientDbService;

  @Rule
  public TestWatcher watchman = new TestWatcher() {
    @Override
    protected void starting(Description description) {
      log.info(">>> Run test {}", description.getMethodName());
    }
  };

  @Before
  public void clear() {
    orientDbService.deleteAll(Taxonomy.class);
    orientDbService.deleteAll(Vocabulary.class);
  }

  @Test
  public void test_create_new_taxonomy() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    List<Taxonomy> taxonomies = newArrayList(taxonomyService.getTaxonomies());
    assertThat(taxonomies).hasSize(1);

    assertTaxonomyEquals(taxonomy, taxonomies.get(0));
    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));

    List<Vocabulary> vocabularies = newArrayList(taxonomyService.getVocabularies(taxonomy.getName()));
    assertThat(vocabularies).hasSize(1);

    Vocabulary expected = new Vocabulary(taxonomy.getName(), "vocabulary 1");
    assertVocabularyEquals(expected, vocabularies.get(0));

    Vocabulary found = taxonomyService.getVocabulary(taxonomy.getName(), expected.getName());
    assertVocabularyEquals(expected, found);
    assertVocabularyEquals(vocabularies.get(0), found);
  }

  @Test
  public void test_save_instead_of_update_taxonomy_name() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    taxonomy.setName("new name");
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    assertThat(taxonomyService.getTaxonomies()).hasSize(2);
  }

  @Test
  public void test_update_taxonomy_name() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    taxonomy.setName("new name");
    taxonomyService.saveTaxonomy(createTaxonomy(), taxonomy);

    assertThat(taxonomyService.getTaxonomies()).hasSize(1);

    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));
  }

  @Test
  public void test_add_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    taxonomy.addVocabulary("vocabulary 2");
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));

    assertThat(taxonomyService.getTaxonomies()).hasSize(1);
    assertThat(taxonomyService.getVocabularies(taxonomy.getName())).hasSize(2);

  }

  @Test
  public void test_delete_taxonomy() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);
    assertThat(taxonomyService.getTaxonomies()).hasSize(1);

    taxonomyService.deleteTaxonomy(taxonomy.getName());
    assertThat(taxonomyService.getTaxonomies()).isEmpty();
    assertThat(orientDbService.list(Taxonomy.class, "select from " + Taxonomy.class.getSimpleName())).isEmpty();
    assertThat(orientDbService.count(Taxonomy.class)).isEqualTo(0);
  }

  @Test
  public void test_save_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);
    assertThat(taxonomyService.getTaxonomies()).hasSize(1);

    Vocabulary vocabulary = createVocabulary(taxonomy);
    taxonomyService.saveVocabulary(null, vocabulary);

    Taxonomy foundTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    assertThat(foundTaxonomy).isNotNull();
    assertThat(foundTaxonomy.hasVocabulary(vocabulary.getName())).isTrue();

    Vocabulary foundVocabulary = taxonomyService.getVocabulary(taxonomy.getName(), vocabulary.getName());
    assertThat(foundVocabulary).isNotNull();
    assertVocabularyEquals(vocabulary, foundVocabulary);

    foundVocabulary.getTerms().clear();
    foundVocabulary.addTerm(createTerm("new term"));
    taxonomyService.saveVocabulary(foundVocabulary, foundVocabulary);

    Vocabulary foundVocabulary2 = taxonomyService.getVocabulary(taxonomy.getName(), foundVocabulary.getName());
    assertThat(foundVocabulary2).isNotNull();
    assertVocabularyEquals(foundVocabulary, foundVocabulary2);
  }

  @Test
  public void test_rename_taxonomy_with_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    Vocabulary vocabulary = createVocabulary(taxonomy);
    taxonomyService.saveVocabulary(null, vocabulary);

    Taxonomy foundTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    foundTaxonomy.setName("new name");
    taxonomyService.saveTaxonomy(foundTaxonomy, foundTaxonomy);

    foundTaxonomy = taxonomyService.getTaxonomy("new name");
    assertThat(foundTaxonomy).isNotNull();
    assertThat(foundTaxonomy.hasVocabulary(vocabulary.getName())).isTrue();
  }

  @Test
  public void test_save_vocabulary_without_taxonomy() {
    try {
      taxonomyService.saveVocabulary(null, new Vocabulary("none", "voc1"));
      Assert.fail("Should throw IllegalArgumentException");
    } catch(IllegalArgumentException e) {
    }
  }

  @Test
  public void test_delete_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    Vocabulary vocabulary = createVocabulary(taxonomy);
    taxonomyService.saveVocabulary(null, vocabulary);
    taxonomyService.deleteVocabulary(vocabulary);

    Taxonomy foundTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    assertThat(foundTaxonomy).isNotNull();
    assertThat(foundTaxonomy.hasVocabulary(vocabulary.getName())).isFalse();

    Vocabulary foundVocabulary = taxonomyService.getVocabulary(taxonomy.getName(), vocabulary.getName());
    assertThat(foundVocabulary).isNull();
  }

  @Test
  public void test_remove_vocabulary_from_taxonomy() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    taxonomy.removeVocabulary("vocabulary 1");
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    assertTaxonomyEquals(taxonomy, taxonomyService.getTaxonomy(taxonomy.getName()));
    assertThat(taxonomyService.getTaxonomies()).hasSize(1);
    assertThat(taxonomyService.getVocabularies(taxonomy.getName())).isEmpty();
  }

  @Test
  public void test_move_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    Vocabulary vocabulary = createVocabulary(taxonomy);
    taxonomyService.saveVocabulary(null, vocabulary);

    Taxonomy taxonomy1 = createTaxonomy();
    taxonomy1.setName("taxonomy 1");
    taxonomyService.saveTaxonomy(taxonomy1, taxonomy1);

    // Move vocabulary
    Vocabulary template = new Vocabulary(vocabulary.getTaxonomy(), vocabulary.getName());
    vocabulary.setTaxonomy(taxonomy1.getName());
    taxonomyService.saveVocabulary(template, vocabulary);

    Taxonomy foundTaxonomy = taxonomyService.getTaxonomy(taxonomy.getName());
    assertThat(foundTaxonomy).isNotNull();
    assertThat(foundTaxonomy.hasVocabulary(vocabulary.getName())).isFalse();

    Taxonomy foundTaxonomy1 = taxonomyService.getTaxonomy(taxonomy1.getName());
    assertThat(foundTaxonomy1).isNotNull();
    assertThat(foundTaxonomy1.hasVocabulary(vocabulary.getName())).isTrue();

  }

  @Test
  public void test_rename_vocabulary() {
    Taxonomy taxonomy = createTaxonomy();
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    Vocabulary vocabulary = createVocabulary(taxonomy);
    taxonomyService.saveVocabulary(null, vocabulary);

    Vocabulary vocabulary1 = createVocabulary(taxonomy);
    vocabulary1.setName("new name");
    taxonomyService.saveVocabulary(vocabulary, vocabulary1);

    Vocabulary foundVocabulary = taxonomyService.getVocabulary(taxonomy.getName(), vocabulary1.getName());
    assertThat(foundVocabulary).isNotNull();

    Taxonomy found = taxonomyService.getTaxonomy(taxonomy.getName());
    assertThat(found).isNotNull();
    assertThat(found.hasVocabulary(vocabulary.getName())).isFalse();
    assertThat(found.hasVocabulary(vocabulary1.getName())).isTrue();
    assertThat(found.getVocabularies()).hasSize(2);
  }

  @Test
  public void test_delete_remove_rename_vocabulary() {
    Taxonomy taxonomy = new Taxonomy("taxonomy");
    taxonomyService.saveTaxonomy(taxonomy, taxonomy);

    Vocabulary vocabulary = new Vocabulary(taxonomy.getName(), "vocabulary 1");
    taxonomyService.saveVocabulary(null, vocabulary);

    Vocabulary vocabulary2 = new Vocabulary(taxonomy.getName(), "vocabulary 2");
    vocabulary2.setName("vocabulary 2");
    taxonomyService.saveVocabulary(null, vocabulary2);

    taxonomyService.deleteVocabulary(vocabulary);

    assertThat(taxonomyService.getVocabulary(taxonomy.getName(), "vocabulary 1")).isNull();

    Vocabulary vocabulary3 = new Vocabulary(taxonomy.getName(), "vocabulary 1");
    taxonomyService.saveVocabulary(vocabulary2, vocabulary3);

    Vocabulary found = taxonomyService.getVocabulary(taxonomy.getName(), "vocabulary 1");
    assertThat(found).isNotNull();
  }

  private Taxonomy createTaxonomy() {
    return new Taxonomy("taxonomy test") //
        .addTitle(Locale.ENGLISH, "English title") //
        .addTitle(Locale.FRENCH, "Titre francais") //
        .addDescription(Locale.ENGLISH, "English description") //
        .addDescription(Locale.FRENCH, "Description francais") //
        .addVocabulary("vocabulary 1");
  }

  private Vocabulary createVocabulary(Taxonomy taxonomy) {
    return new Vocabulary(taxonomy.getName(), "vocabulary test") //
        .addTitle(Locale.ENGLISH, "English vocabulary title") //
        .addTitle(Locale.FRENCH, "Titre vocabulaire francais") //
        .addDescription(Locale.ENGLISH, "English vocabulary description") //
        .addDescription(Locale.FRENCH, "Description vocabulaire francais")//
        .addTerm(createTerm("1").addTerm(createTerm("1.1"))) //
        .addTerm(createTerm("2"));
  }

  private Term createTerm(String suffix) {
    return new Term("term " + suffix) //
        .addTitle(Locale.ENGLISH, "English title " + suffix) //
        .addTitle(Locale.FRENCH, "Titre francais " + suffix) //
        .addDescription(Locale.ENGLISH, "English description " + suffix) //
        .addDescription(Locale.FRENCH, "Description francais " + suffix);
  }

  private void assertTaxonomyEquals(Taxonomy expected, Taxonomy found) {
    assertThat(found).isNotNull();

    assertThat(expected).isEqualTo(found);
    assertThat(expected.getTitles()).isEqualTo(found.getTitles());
    assertThat(expected.getDescriptions()).isEqualTo(found.getDescriptions());
    assertThat(expected.getVocabularies()).isEqualTo(found.getVocabularies());
    Asserts.assertCreatedTimestamps(expected, found);
  }

  private void assertVocabularyEquals(Vocabulary expected, Vocabulary found) {
    assertThat(found).isNotNull();

    assertThat(expected).isEqualTo(found);
    assertThat(expected.getTaxonomy()).isEqualTo(found.getTaxonomy());
    assertThat(expected.isRepeatable()).isEqualTo(found.isRepeatable());
    assertThat(expected.getTitles()).isEqualTo(found.getTitles());
    assertThat(expected.getDescriptions()).isEqualTo(found.getDescriptions());
    assertThat(expected.getTerms()).isEqualTo(found.getTerms());
    Asserts.assertCreatedTimestamps(expected, found);
  }

  @Configuration
  public static class Config extends AbstractOrientDbTestConfig {

    @Bean
    public TaxonomyService taxonomyService() {
      return new TaxonomyServiceImpl();
    }

  }
}
