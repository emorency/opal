/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.magma.ConflictDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;

/**
 *
 */
public class TableCompareConflictsTable extends Table<ConflictDto> {

  private static Translations translations = GWT.create(Translations.class);

  private Column<ConflictDto, String> variableNameColumn;

  public TableCompareConflictsTable() {
    super();
    initColumns();
  }

  public Column<ConflictDto, String> getVariableNameColumn() {
    return variableNameColumn;
  }

  private void initColumns() {
    addColumn(variableNameColumn = new ClickableColumn<ConflictDto>() {
      @Override
      public String getValue(ConflictDto conflict) {
        return conflict.getVariable().getName();
      }
    }, translations.nameLabel());

    addColumn(new TextColumn<ConflictDto>() {
      @Override
      public String getValue(ConflictDto conflict) {

        StringBuilder builder = new StringBuilder();
        JsArrayString arguments = (JsArrayString) (conflict.getArgumentsArray() != null ? conflict.getArgumentsArray() : JsArrayString.createArray());
        for(int i = 0; i < arguments.length(); i++) {
          builder.append(arguments.get(i).toString() + " ");
        }

        return translations.datasourceComparisonErrorMap().get(conflict.getCode()) + ", " + builder.toString();
      }
    }, translations.messageLabel());
  }

}