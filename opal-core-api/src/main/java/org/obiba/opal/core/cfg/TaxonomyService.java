/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.cfg;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.obiba.opal.core.service.SystemService;

/**
 * Create, update and delete {@link Taxonomy}.
 */
public interface TaxonomyService extends SystemService {

  Iterable<Taxonomy> getTaxonomies();

  @Nullable
  Taxonomy getTaxonomy(@NotNull String name);

  void saveTaxonomy(@Nullable Taxonomy template, @NotNull Taxonomy taxonomy);

  void deleteTaxonomy(@NotNull String name);

  Iterable<Vocabulary> getVocabularies(@NotNull String taxonomy);

  @Nullable
  Vocabulary getVocabulary(@NotNull String taxonomy, @NotNull String name);

  void saveVocabulary(@Nullable Vocabulary template, @NotNull Vocabulary vocabulary);

  void deleteVocabulary(@NotNull Vocabulary vocabulary);

}
