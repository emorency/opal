/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import java.util.List;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.FunctionalUnitDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.HasClickHandlers;

/**
 * Presenter elements common between the export and import dialog.
 */
public abstract class DataCommonPresenter {

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    /** Display inline form errors. */
    void showErrors(List<String> errors);

    /** Hide inline form errors. */
    void hideErrors();

    /** Set a collection of Opal datasources retrieved from Opal. */
    void setDatasources(JsArray<DatasourceDto> datasources);

    /** Get the Opal datasource selected by the user. */
    String getSelectedDatasource();

    /** Set a collection of Opal units retrieved from Opal. */
    void setUnits(JsArray<FunctionalUnitDto> units);

    /** Get the Opal unit selected by the user. */
    String getSelectedUnit();

    /** Get the form submit button. */
    HasClickHandlers getSubmit();
  }

}
