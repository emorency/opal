/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.unit;

import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.opal.core.service.UnitKeyStoreService;

/**
 * Represents an organization that fulfils the role of a "functional unit" of a Biobank.
 */
public class FunctionalUnit {
  //
  // Constants
  //

  public static final String OPAL_INSTANCE = "OpalInstance";

  //
  // Instance Variables
  //

  private String name;

  private String keyVariableName;

  private DatasourceEncryptionStrategy datasourceEncryptionStrategy;

  private UnitKeyStoreService unitKeyStoreService;

  //
  // Constructors
  //

  public FunctionalUnit() {
    super();
  }

  public FunctionalUnit(String name, String keyVariableName) {
    this.name = name;
    this.keyVariableName = keyVariableName;
  }

  //
  // Methods
  //

  public void setUnitKeyStoreService(UnitKeyStoreService unitKeyStoreService) {
    this.unitKeyStoreService = unitKeyStoreService;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getKeyVariableName() {
    return keyVariableName;
  }

  public void setKeyVariableName(String keyVariableName) {
    this.keyVariableName = keyVariableName;
  }

  public DatasourceEncryptionStrategy getDatasourceEncryptionStrategy() {
    return datasourceEncryptionStrategy;
  }

  public void setDatasourceEncryptionStrategy(DatasourceEncryptionStrategy datasourceEncryptionStrategy) {
    this.datasourceEncryptionStrategy = datasourceEncryptionStrategy;
  }

  public UnitKeyStore getKeyStore() {
    return unitKeyStoreService.getOrCreateUnitKeyStore(getName());
  }
}
