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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.domain.security.SubjectProfile;

/**
 * Service to handle the profile of subjects.
 */
public interface SubjectProfileService extends SystemService {

  boolean supportProfile(@Nullable Object principal);

  /**
   * Add or check profile of the subject: check will fail if a subject from a different realm has already a profile entry.
   * @param principal
   * @param realm
   */
  void ensureProfile(@NotNull String principal, @NotNull String realm);

  /**
   * Add or check profile of the subject: check will fail if a subject from a different realm has already a profile entry.
   *
   * @param subject
   */
  void ensureProfile(@NotNull PrincipalCollection principalCollection);

  /**
   * Delete profile.
   *
   * @param principal
   */
  void deleteProfile(@NotNull String principal);

  /**
   * Get profile by principal.
   *
   * @param principal
   * @return
   */
  @Nullable
  SubjectProfile getProfile(@NotNull String principal);

  /**
   * Update profile timestamp.
   * @param principal
   */
  void updateProfile(@NotNull String principal);

  /**
   * Get all subject profiles.
   * @return
   */
  Iterable<SubjectProfile> getProfiles();
}
