/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.presenter;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.dashboard.presenter.DashboardPresenter;
import org.obiba.opal.web.gwt.app.client.event.UserMessageEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.DatasourceSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateViewStepPresenter extends WidgetPresenter<CreateViewStepPresenter.Display> {
  //
  // Instance Variables
  //

  @Inject
  private Provider<ApplicationPresenter> applicationPresenter;

  @Inject
  private Provider<DashboardPresenter> dashboardPresenter;

  @Inject
  private DatasourceSelectorPresenter datasourceSelectorPresenter;

  @Inject
  private TableListPresenter tableListPresenter;

  @Inject
  private ConclusionStepPresenter conclusionStepPresenter;

  //
  // Constructors
  //

  @Inject
  public CreateViewStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    datasourceSelectorPresenter.bind();
    getDisplay().setDatasourceSelector(datasourceSelectorPresenter.getDisplay());

    tableListPresenter.bind();
    getDisplay().setTableSelector(tableListPresenter.getDisplay());

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    datasourceSelectorPresenter.unbind();
    tableListPresenter.unbind();
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(getDisplay().addCreateClickHandler(new CreateClickHandler()));
    super.registerHandler(getDisplay().addSelectExistingDatasourceClickHandler(new SelectExistingDatasourceClickHandler()));
    super.registerHandler(getDisplay().addCreateNewDatasourceClickHandler(new CreateNewDatasourceClickHandler()));
  }

  @Override
  public void revealDisplay() {
    refreshDisplay();
  }

  @Override
  public void refreshDisplay() {
    datasourceSelectorPresenter.refreshDisplay();
    getDisplay().setDatasourceSelectorEnabled(false);
    getDisplay().setNewDatasourceInputEnabled(false);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void setDatasourceSelector(DatasourceSelectorPresenter.Display datasourceSelector);

    void setDatasourceSelectorEnabled(boolean enabled);

    void setNewDatasourceInputEnabled(boolean enabled);

    void setTableSelector(TableListPresenter.Display tableSelector);

    String getViewName();

    boolean isAttachingToExistingDatasource();

    boolean isAttachingToNewDatasource();

    String getExistingDatasourceName();

    String getNewDatasourceName();

    boolean isApplyingGlobalVariableFilter();

    boolean isAddingVariablesOneByOne();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCreateClickHandler(ClickHandler handler);

    HandlerRegistration addSelectExistingDatasourceClickHandler(ClickHandler handler);

    HandlerRegistration addCreateNewDatasourceClickHandler(ClickHandler handler);
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new WorkbenchChangeEvent(dashboardPresenter.get()));
      ApplicationPresenter.Display appDisplay = applicationPresenter.get().getDisplay();
      appDisplay.setCurrentSelection(appDisplay.getDashboardItem());
    }
  }

  class CreateClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      if(getDisplay().getViewName() == null) {
        eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, "ViewNameRequired", null));
      } else if(tableListPresenter.getTables().isEmpty()) {
        eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, "TableSelectionRequired", null));
      } else if(tableEntityTypesDoNotMatch(tableListPresenter.getTables())) {
        eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, "TableEntityTypesDoNotMatch", null));
      } else if(!getDisplay().isApplyingGlobalVariableFilter() && !getDisplay().isAddingVariablesOneByOne()) {
        eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, "VariableDefinitionMethodRequired", null));
      } else { // OK -- go ahead and create the view
        createView();
      }
    }

    private void createView() {
      setCreateViewResourceRequest();
      conclusionStepPresenter.showConfigureViewWidgets(false);
      conclusionStepPresenter.sendResourceRequest();

      eventBus.fireEvent(new WorkbenchChangeEvent(conclusionStepPresenter));
    }

    private void setCreateViewResourceRequest() {
      // Get the view name and datasource name.
      String viewName = getDisplay().getViewName();
      String datasourceName = getDisplay().isAttachingToExistingDatasource() ? getDisplay().getExistingDatasourceName() : getDisplay().getNewDatasourceName();

      // Build the ViewDto for the request.
      ViewDtoBuilder viewDtoBuilder = ViewDtoBuilder.newBuilder().fromTables(tableListPresenter.getTables());
      if(getDisplay().isApplyingGlobalVariableFilter()) {
        viewDtoBuilder.defaultJavaScriptView();
      } else if(getDisplay().isAddingVariablesOneByOne()) {
        viewDtoBuilder.defaultVariableListView();
      }
      ViewDto viewDto = viewDtoBuilder.build();

      // Add the resource request to the ConclusionStepPresenter.
      conclusionStepPresenter.setResourceRequest(viewName, "/datasource/" + datasourceName + "/view/" + viewName, ResourceRequestBuilderFactory.newBuilder().put().forResource("/datasource/" + datasourceName + "/view/" + viewName).accept("application/x-protobuf+json").withResourceBody(stringify(viewDto)));
    }

    private boolean tableEntityTypesDoNotMatch(List<TableDto> tableDtos) {
      String entityType = tableDtos.get(0).getEntityType();

      for(int i = 1; i < tableDtos.size(); i++) {
        TableDto tableDto = tableDtos.get(i);
        if(!tableDto.getEntityType().equals(entityType)) {
          return true;
        }
      }

      return false;
    }
  }

  class SelectExistingDatasourceClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      getDisplay().setDatasourceSelectorEnabled(true);
      getDisplay().setNewDatasourceInputEnabled(false);
    }
  }

  class CreateNewDatasourceClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      getDisplay().setDatasourceSelectorEnabled(false);
      getDisplay().setNewDatasourceInputEnabled(true);
    }
  }

  public static native String stringify(JavaScriptObject obj)
  /*-{
  return $wnd.JSON.stringify(obj);
  }-*/;
}
