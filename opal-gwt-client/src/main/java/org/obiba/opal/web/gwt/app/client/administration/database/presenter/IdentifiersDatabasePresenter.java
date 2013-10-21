/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.database.presenter;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.database.event.DatabaseUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.MongoDbDatabaseDto;
import org.obiba.opal.web.model.client.database.SqlDatabaseDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class IdentifiersDatabasePresenter extends PresenterWidget<IdentifiersDatabasePresenter.Display>
    implements IdentifiersDatabaseUiHandlers, RequestAdministrationPermissionEvent.Handler {

  public static final String IDENTIFIERS_DATABASE_NAME = "_identifiers";

  private final ModalProvider<SqlDatabasePresenter> sqlDatabaseModalProvider;

  private final ModalProvider<MongoDatabasePresenter> mongoDatabaseModalProvider;

  private DatabaseDto databaseDto;

  @Inject
  public IdentifiersDatabasePresenter(Display display, EventBus eventBus,
      ModalProvider<SqlDatabasePresenter> sqlDatabaseModalProvider,
      ModalProvider<MongoDatabasePresenter> mongoDatabaseModalProvider) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.sqlDatabaseModalProvider = sqlDatabaseModalProvider.setContainer(this);
    this.mongoDatabaseModalProvider = mongoDatabaseModalProvider.setContainer(this);
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  protected void onReveal() {
    refresh();
  }

  @Override
  protected void onBind() {
    super.onBind();

    addRegisteredHandler(DatabaseCreatedEvent.getType(), new DatabaseCreatedEvent.DatabaseCreatedHandler() {
      @Override
      public void onDatabaseCreated(DatabaseCreatedEvent event) {
        databaseDto = event.getDto();
        getView().setDatabase(event.getDto());
      }
    });
    addRegisteredHandler(DatabaseUpdatedEvent.getType(), new DatabaseUpdatedEvent.DatabaseUpdatedHandler() {
      @Override
      public void onDatabaseUpdated(DatabaseUpdatedEvent event) {
        databaseDto = event.getDto();
        getView().setDatabase(event.getDto());
      }
    });
  }

  @Override
  public void createSql() {
    DatabaseDto dto = createDefaultIdentifiersDatabaseDto();
    SqlDatabaseDto sqlDatabaseDto = SqlDatabaseDto.create();
    sqlDatabaseDto.setSqlSchema(SqlDatabaseDto.SqlSchema.HIBERNATE);
    dto.setExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings, sqlDatabaseDto);
    sqlDatabaseModalProvider.get().createNewIdentifierDatabase(dto);
  }

  @Override
  public void createMongo() {
    DatabaseDto dto = createDefaultIdentifiersDatabaseDto();
    dto.setExtension(MongoDbDatabaseDto.DatabaseDtoExtensions.settings, MongoDbDatabaseDto.create());
    mongoDatabaseModalProvider.get().createNewIdentifierDatabase(dto);
  }

  @Override
  public void edit() {
    if(databaseDto.getExtension(SqlDatabaseDto.DatabaseDtoExtensions.settings) != null) {
      sqlDatabaseModalProvider.get().editDatabase(databaseDto);
    } else if(databaseDto.getExtension(MongoDbDatabaseDto.DatabaseDtoExtensions.settings) != null) {
      mongoDatabaseModalProvider.get().editDatabase(databaseDto);
    }
  }

  @Override
  public void testConnection() {
    DatabaseAdministrationPresenter.testConnection(getEventBus(), IDENTIFIERS_DATABASE_NAME);
  }

  private DatabaseDto createDefaultIdentifiersDatabaseDto() {
    DatabaseDto dto = DatabaseDto.create();
    dto.setUsedForIdentifiers(true);
    dto.setName(IDENTIFIERS_DATABASE_NAME);
    dto.setUsage(DatabaseDto.Usage.STORAGE);
    dto.setDefaultStorage(false);
    return dto;
  }

  private void refresh() {
    ResourceRequestBuilderFactory.<DatabaseDto>newBuilder() //
        .forResource(DatabaseResources.identifiersDatabase()) //
        .withCallback(new ResourceCallback<DatabaseDto>() {
          @Override
          public void onResource(Response response, @Nullable DatabaseDto dto) {
            databaseDto = dto;
            getView().setDatabase(dto);
          }
        }) //
        .withCallback(Response.SC_NOT_FOUND, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            databaseDto = null;
            getView().setDatabase(null);
          }
        })
        .get().send();
  }

  public interface Display extends View, HasUiHandlers<IdentifiersDatabaseUiHandlers> {

    void setDatabase(@Nullable DatabaseDto database);
  }

}
