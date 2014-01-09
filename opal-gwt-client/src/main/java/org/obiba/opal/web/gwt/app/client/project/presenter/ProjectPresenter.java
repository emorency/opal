/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.presenter;

import java.util.Arrays;

import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.MagmaPathSelectionEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.MagmaPresenter;
import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportsPresenter;
import org.obiba.opal.web.gwt.app.client.support.MagmaPath;
import org.obiba.opal.web.gwt.app.client.support.PlaceRequestHelper;
import org.obiba.opal.web.gwt.app.client.task.presenter.TasksPresenter;
import org.obiba.opal.web.gwt.app.client.ui.HasTabPanel;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.ProjectSummaryDto;

import com.google.common.base.Strings;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class ProjectPresenter extends Presenter<ProjectPresenter.Display, ProjectPresenter.Proxy>
    implements ProjectUiHandlers, FolderUpdatedEvent.Handler {

  public interface Display extends View, HasUiHandlers<ProjectUiHandlers>, HasTabPanel {

    enum ProjectTab {
      HOME,
      TABLES,
      FILES,
      REPORTS,
      TASKS,
      PERMISSIONS,
      ADMINISTRATION
    }

    void setProject(ProjectDto project);

    void setProjectSummary(ProjectSummaryDto projectSummary);

    HasAuthorization getPermissionsAuthorizer();
  }

  @ProxyStandard
  @NameToken(Places.PROJECT)
  public interface Proxy extends ProxyPlace<ProjectPresenter> {}

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> TABLES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> FILES_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> REPORTS_PANE
      = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> TASKS_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> ADMIN_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> PERMISSION_PANE
      = new GwtEvent.Type<RevealContentHandler<?>>();

  private final Provider<MagmaPresenter> magmaPresenterProvider;

  private final Provider<FileExplorerPresenter> fileExplorerPresenterProvider;

  private final Provider<ReportsPresenter> reportsPresenterProvider;

  private final Provider<TasksPresenter> tasksPresenterProvider;

  private final Provider<ProjectPermissionsPresenter> projectResourcePermissionsProvider;



  private final Provider<ProjectAdministrationPresenter> projectAdministrationPresenterProvider;

  private final PlaceManager placeManager;

  private String projectName;

  private Display.ProjectTab tab = Display.ProjectTab.TABLES;

  private ProjectDto project;

  private MagmaPresenter magmaPresenter;

  private FileExplorerPresenter fileExplorerPresenter;

  private ReportsPresenter reportsPresenter;

  private TasksPresenter tasksPresenter;

  private ProjectPermissionsPresenter projectPermissionsPresenter;

  private ProjectAdministrationPresenter projectAdministrationPresenter;

  @Inject
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "ConstructorWithTooManyParameters" })
  public ProjectPresenter(EventBus eventBus, Display display, Proxy proxy, PlaceManager placeManager,
      Provider<MagmaPresenter> magmaPresenterProvider, Provider<FileExplorerPresenter> fileExplorerPresenterProvider,
      Provider<ReportsPresenter> reportsPresenterProvider, Provider<TasksPresenter> tasksPresenterProvider,
      Provider<ProjectAdministrationPresenter> projectAdministrationPresenterProvider,
      Provider<ProjectPermissionsPresenter> projectResourcePermissionsProvider) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    this.placeManager = placeManager;
    this.magmaPresenterProvider = magmaPresenterProvider;
    this.fileExplorerPresenterProvider = fileExplorerPresenterProvider;
    this.reportsPresenterProvider = reportsPresenterProvider;
    this.tasksPresenterProvider = tasksPresenterProvider;
    this.projectAdministrationPresenterProvider = projectAdministrationPresenterProvider;
    this.projectResourcePermissionsProvider = projectResourcePermissionsProvider;
  }

  @Override
  protected void onBind() {
    super.onBind();
    addRegisteredHandler(FolderUpdatedEvent.getType(), this);
  }

  @Override
  public void prepareFromRequest(PlaceRequest request) {
    super.prepareFromRequest(request);
    String oldProject = projectName;
    projectName = request.getParameter(ParameterTokens.TOKEN_NAME, null);
    tab = validateTab(request.getParameter(ParameterTokens.TOKEN_TAB, null));
    if (oldProject == null || projectName.equals(oldProject)) {
      getView().setTabData(tab.ordinal(), tab == Display.ProjectTab.TABLES ? validatePath(projectName,
          request.getParameter(ParameterTokens.TOKEN_PATH, null)) : null);
    } else {
      if(fileExplorerPresenter != null) fileExplorerPresenter.reset();
      getView().clearTabsData();
    }

    refresh();
  }

  private void authorize() {
    if(projectName == null) return;

    // permissions tab
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_PERMISSIONS_ALL.create().build(projectName)).get()//
        .authorize(getView().getPermissionsAuthorizer())//
        .send();
  }

  public void refresh() {
    // TODO handle wrong or missing project name
    if(projectName == null) return;
    // reset
    ResourceRequestBuilderFactory.<ProjectDto>newBuilder().forResource(UriBuilders.PROJECT.create().build(projectName))
        .withCallback(new ResourceCallback<ProjectDto>() {
          @Override
          public void onResource(Response response, ProjectDto resource) {
            project = resource;
            // TODO check project is null
            getView().setProject(project);
            onTabSelected(tab.ordinal());
            getView().selectTab(tab.ordinal());
          }
        }).get().send();
    refreshSummary();
    authorize();
  }

  private void refreshSummary() {
    // TODO handle wrong or missing project name
    if(projectName == null) return;
    ResourceRequestBuilderFactory.<ProjectSummaryDto>newBuilder()
        .forResource(UriBuilders.PROJECT_SUMMARY.create().build(projectName))
        .withCallback(new ResourceCallback<ProjectSummaryDto>() {
          @Override
          public void onResource(Response response, ProjectSummaryDto resource) {
            getView().setProjectSummary(resource);
          }
        }).get().send();
  }

  @Override
  public void onProjectsSelection() {
    placeManager.revealPlace(new PlaceRequest.Builder().nameToken(Places.PROJECTS).build());
  }

  @Override
  @SuppressWarnings("PMD.NcssMethodCount")
  public void onTabSelected(int index) {
    String queryPathParam = (String) getView().getTabData(index);
    selectTab(index, queryPathParam);

    PlaceRequest.Builder builder = PlaceRequestHelper
        .createRequestBuilderWithParams(placeManager.getCurrentPlaceRequest(),
            Arrays.asList(ParameterTokens.TOKEN_NAME)) //
        .with(ParameterTokens.TOKEN_TAB, tab.toString());
    if(!Strings.isNullOrEmpty(queryPathParam)) {
      builder.with(ParameterTokens.TOKEN_PATH, queryPathParam);
    }
    placeManager.updateHistory(builder.build(), true);
  }

  private void selectTab(int index, String queryPathParam) {
    tab = Display.ProjectTab.values()[index];
    switch(tab) {
      case TABLES:
        onTablesTabSelected(queryPathParam);
        break;
      case FILES:
        onFilesTabSelected(queryPathParam);
        break;
      case REPORTS:
        onReportsTabSelected();
        break;
      case TASKS:
        onTasksTabSelected();
        break;
      case PERMISSIONS:
        onPermissionsTabSelected();
        break;
      case ADMINISTRATION:
        onAdminTabSelected();
        break;
    }
  }

  private void onTablesTabSelected(String path) {
    if(magmaPresenter == null) {
      magmaPresenter = magmaPresenterProvider.get();
      setInSlot(TABLES_PANE, magmaPresenter);
    }
    fireEvent(Strings.isNullOrEmpty(path)
        ? new MagmaPathSelectionEvent(this, project.getDatasource().getName())
        : new MagmaPathSelectionEvent(this, path));
  }

  private void onFilesTabSelected(String path) {
    if(fileExplorerPresenter == null) {
      fileExplorerPresenter = fileExplorerPresenterProvider.get();
      setInSlot(FILES_PANE, fileExplorerPresenter);
    }
    fileExplorerPresenter.showProject(projectName);
    fireEvent(Strings.isNullOrEmpty(path)
        ? new FolderRequestEvent(FileDtos.project(projectName))
        : new FolderRequestEvent(FileDtos.create(path.split("/"))));
  }

  private void onReportsTabSelected() {
    if(reportsPresenter == null) {
      reportsPresenter = reportsPresenterProvider.get();
      setInSlot(REPORTS_PANE, reportsPresenter);
    }
    reportsPresenter.showProject(projectName);
  }

  private void onTasksTabSelected() {
    if(tasksPresenter == null) {
      tasksPresenter = tasksPresenterProvider.get();
      setInSlot(TASKS_PANE, tasksPresenter);
    }
    tasksPresenter.showProject(projectName);
  }

  private void onPermissionsTabSelected() {
    if(projectPermissionsPresenter == null) {
      projectPermissionsPresenter = projectResourcePermissionsProvider.get();
      setInSlot(PERMISSION_PANE, projectPermissionsPresenter);
    }
    projectPermissionsPresenter.initialize(project);
  }

  private void onAdminTabSelected() {
    if(projectAdministrationPresenter == null) {
      projectAdministrationPresenter = projectAdministrationPresenterProvider.get();
      setInSlot(ADMIN_PANE, projectAdministrationPresenter);
    }
    projectAdministrationPresenter.setProject(project);
  }

  @Override
  public void onFolderUpdated(FolderUpdatedEvent event) {
    if(fileExplorerPresenter == null || !fileExplorerPresenter.isVisible()) return;
    updateHistory(event.getFolder().getPath());
  }

  private void updateHistory(String queryPathParam) {
    PlaceRequest.Builder builder = PlaceRequestHelper
        .createRequestBuilderWithParams(placeManager.getCurrentPlaceRequest(),
            Arrays.asList(ParameterTokens.TOKEN_NAME, ParameterTokens.TOKEN_TAB));

    if(!Strings.isNullOrEmpty(queryPathParam)) {
      builder.with(ParameterTokens.TOKEN_PATH, queryPathParam).build();
    }

    placeManager.updateHistory(builder.build(), true);
    getView().setTabData(tab.ordinal(), queryPathParam);
  }

  private Display.ProjectTab validateTab(String tabName) {
    if(!Strings.isNullOrEmpty(tabName)) {
      try {
        return Display.ProjectTab.valueOf(tabName);
      } catch(IllegalArgumentException ignored) {
      }
    }
    return Display.ProjectTab.HOME;
  }

  private String validatePath(String name, String path) {
    if(tab == Display.ProjectTab.TABLES && !Strings.isNullOrEmpty(path)) {
      String datasourceName = MagmaPath.Parser.parse(path).getDatasource();
      if(!Strings.isNullOrEmpty(datasourceName) && name.equals(datasourceName)) {
        return path;
      }
    }
    updateHistory(null);
    return null;
  }
}
