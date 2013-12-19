/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.support.DatasourceFileType;
import org.obiba.opal.web.gwt.app.client.presenter.CharacterSetDisplay;
import org.obiba.opal.web.gwt.app.client.support.DatasourceParsingErrorDtos;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.ui.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.CharacterSetEncodingValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.LocaleValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.FileViewDto;
import org.obiba.opal.web.model.client.magma.FileViewDto.FileViewType;
import org.obiba.opal.web.model.client.magma.SpssDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.StaticDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_OK;

public class VariablesImportPresenter extends WizardPresenterWidget<VariablesImportPresenter.Display>
    implements VariablesImportUiHandlers {


  public static final WizardType WIZARD_TYPE = new WizardType();

  public static class Wizard extends WizardProxy<VariablesImportPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<VariablesImportPresenter> wizardProvider) {
      super(eventBus, WIZARD_TYPE, wizardProvider);
    }

  }

  private static final String EXCEL_TEMPLATE = "/opalVariableTemplate.xls";

  private final ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter;

  private final ConclusionStepPresenter conclusionPresenter;

  private final FileSelectionPresenter fileSelectionPresenter;

  private String transientDatasourceName;

  private String datasourceName;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public VariablesImportPresenter(Display display, EventBus eventBus,
      ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter,
      ConclusionStepPresenter conclusionPresenter, FileSelectionPresenter fileSelectionPresenter) {
    super(eventBus, display);
    this.comparedDatasourcesReportPresenter = comparedDatasourcesReportPresenter;
    this.conclusionPresenter = conclusionPresenter;
    this.fileSelectionPresenter = fileSelectionPresenter;
    init();
  }

  private void init() {
    getView().setUiHandlers(this);
    setDefaultCharset();

    getEventBus().addHandler(FileSelectionUpdatedEvent.getType(), new FileSelectionUpdatedEvent.Handler() {
      @Override
      public void onFileSelectionUpdated(FileSelectionUpdatedEvent event) {
        String selectedFile = ((FileSelectionPresenter) event.getSource()).getSelectedFile();
        getView().showSpssSpecificPanel(DatasourceFileType.isSpssFile(selectedFile));
      }
    });
  }

  @Override
  public void onCancel() {
    super.onCancel();
    deleteTransientDatasource();
  }

  @Override
  public void onModalHidden() {
    deleteTransientDatasource();
  }

  @Override
  public void processVariablesFile() {
    getView().clearErrors();
    if (!new ViewValidator().validate()) return;
    createTransientDatasource();
    getView().gotoPreview();
  }

  @Override
  public void createTable() {
    if (!new ViewImportValidator().validate()) return;
    conclusionPresenter.sendResourceRequests();
    getView().hide();
  }

  @Override
  public void downExcelTemplate() {
    fireEvent(new FileDownloadRequestEvent("/templates" + EXCEL_TEMPLATE));
  }

  @Override
  protected void onBind() {
    super.onBind();
    comparedDatasourcesReportPresenter.bind();
    getView().setComparedDatasourcesReportDisplay(comparedDatasourcesReportPresenter.getView());

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);
    fileSelectionPresenter.bind();
    getView().setFileSelectionDisplay(fileSelectionPresenter.getView());
    addHandler(FileSelectionEvent.getType(), new FileSelectionEvent.Handler() {
      @Override
      public void onFileSelection(FileSelectionEvent event) {
        getView().clearErrors();
      }
    });

    conclusionPresenter.bind();
    getView().setConclusionDisplay(conclusionPresenter.getView());
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    datasourceName = null;
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length > 0) {
      datasourceName = (String) event.getEventParameters()[0];
    }
  }

  private void setDefaultCharset() {
    ResourceRequestBuilderFactory.<JsArrayString>newBuilder().forResource("/files/charsets/default").get()
        .withCallback(new ResourceCallback<JsArrayString>() {

          @Override
          public void onResource(Response response, JsArrayString resource) {
            String charset = resource.get(0);
            getView().setDefaultCharset(charset);
          }
        }).send();
  }

  private void createTransientDatasource() {
    if (!Strings.isNullOrEmpty(transientDatasourceName)) deleteTransientDatasource();

    DatasourceFactoryDto factory = createDatasourceFactoryDto(getView().getSelectedFile());
    ResponseCodeCallback errorCallback = new TransientDatasourceFailureCallback();

    ResourceRequestBuilderFactory.<DatasourceDto>newBuilder() //
        .forResource(UriBuilders.PROJECT_TRANSIENT_DATASOURCE.create().build(datasourceName)) //
        .post() //
        .withResourceBody(DatasourceFactoryDto.stringify(factory)) //
        .withCallback(new TransientDatasourceSuccessCallback(factory)) //
        .withCallback(SC_BAD_REQUEST, errorCallback) //
        .withCallback(SC_INTERNAL_SERVER_ERROR, errorCallback).send();
  }

  private void deleteTransientDatasource() {
    if (Strings.isNullOrEmpty(transientDatasourceName)) return;

    UriBuilder builder = UriBuilder.create().segment("datasource", transientDatasourceName);
    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(builder.build()) //
        .withCallback(SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {}
        }).delete().send();
    transientDatasourceName = null;
  }

  private DatasourceFactoryDto createDatasourceFactoryDto(String tmpFilePath) {
    DatasourceFileType type = DatasourceFileType.getFileType(tmpFilePath);

    switch(type) {
      case XLS:
      case XLSX:
        return createExcelDatasourceFactoryDto(tmpFilePath);

      case SAV:
        return createSpssDatasourceFactoryDto(tmpFilePath);
    }

    return createStaticDatasourceFactoryDto(tmpFilePath);
  }

  private DatasourceFactoryDto createExcelDatasourceFactoryDto(String tmpFilePath) {
    ExcelDatasourceFactoryDto excelDto = ExcelDatasourceFactoryDto.create();
    excelDto.setFile(tmpFilePath);
    excelDto.setReadOnly(true);

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(ExcelDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, excelDto);

    return dto;
  }

  private DatasourceFactoryDto createSpssDatasourceFactoryDto(String tmpFilePath) {
    SpssDatasourceFactoryDto spssDto = SpssDatasourceFactoryDto.create();
    spssDto.setFile(tmpFilePath);
    spssDto.setCharacterSet(getView().getCharsetText().getText());
    spssDto.setEntityType(getView().getSpssEntityType().getText());
    spssDto.setLocale(getView().getLocale());

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(SpssDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, spssDto);

    return dto;
  }

  private DatasourceFactoryDto createStaticDatasourceFactoryDto(String tmpFilePath) {
    StaticDatasourceFactoryDto staticDto = StaticDatasourceFactoryDto.create();
    ViewDtoBuilder viewDtoBuilder = ViewDtoBuilder.newBuilder();
    String name = tmpFilePath.substring(tmpFilePath.lastIndexOf('/') + 1, tmpFilePath.lastIndexOf('.'));
    viewDtoBuilder.setName(name);

    FileViewDto fileView = FileViewDto.create();
    fileView.setFilename(tmpFilePath);
    fileView.setType(FileViewType.SERIALIZED_XML);

    viewDtoBuilder.fileView(fileView);
    JsArray<ViewDto> views = JsArrays.create();
    views.push(viewDtoBuilder.build());
    staticDto.setViewsArray(views);

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(StaticDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, staticDto);

    return dto;
  }

  private final class ViewImportValidator extends ViewValidationHandler {
    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new ImportableValidator());
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(null, message);
    }
  }

  private final class ViewValidator extends ViewValidationHandler {
    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      DatasourceFileType fileType = DatasourceFileType.getFileType(getView().getSelectedFile());
      validators.add(new FileTypeValidator(fileType, Display.FormField.FILE_SELECTION.name()));

      if (DatasourceFileType.SAV == fileType) {
        validators.add(createLocaleValidator());
        validators.add(createCharacterSetEncodingValidator());
      }

      return validators;
    }

    private FieldValidator createLocaleValidator() {
      String localeName = getView().getLocale();
      LocaleValidator localeValidator = new LocaleValidator(localeName, "InvalidLocaleName",
          Display.FormField.LOCALE.name());
      localeValidator.setArgs(Arrays.asList(localeName));

      return localeValidator;
    }

    private FieldValidator createCharacterSetEncodingValidator() {
      String encoding = getView().getCharsetText().getText();
      CharacterSetEncodingValidator encodingValidator = new CharacterSetEncodingValidator(encoding,
          "InvalidCharacterSetName", Display.FormField.CHARSET.name());
      encodingValidator.setArgs(Arrays.asList(encoding));

      return encodingValidator;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  private final class ImportableValidator extends AbstractFieldValidator {

    ImportableValidator() {
      super("");
    }

    @Override
    protected boolean hasError() {
      if(comparedDatasourcesReportPresenter.getSelectedTables().isEmpty()) {
        setErrorMessageKey("TableSelectionIsRequired");
        return true;
      }
      if(!comparedDatasourcesReportPresenter.canBeSubmitted()) {
        setErrorMessageKey("NotIgnoredConflicts");
        return true;
      }

      conclusionPresenter.clearResourceRequests();
      comparedDatasourcesReportPresenter.addUpdateVariablesResourceRequests(conclusionPresenter);
      if(conclusionPresenter.getResourceRequestCount() == 0) {
        setErrorMessageKey("NoVariablesToBeImported");
        return true;
      }

      return false;
    }
  }

  private final static class FileTypeValidator extends AbstractFieldValidator {

    private final DatasourceFileType fileType;

    FileTypeValidator(DatasourceFileType fileType, String id) {
      super("InvalidFileType", id);
      this.fileType = fileType;
    }

    @Override
    protected boolean hasError() {
      return DatasourceFileType.INVALID == fileType;
    }
  }

  private final class TransientDatasourceSuccessCallback implements ResourceCallback<DatasourceDto> {

    private final DatasourceFactoryDto factory;

    TransientDatasourceSuccessCallback(DatasourceFactoryDto factory) {
      this.factory = factory;
    }

    @Override
    public void onResource(Response response, DatasourceDto resource) {
      if(response.getStatusCode() == SC_CREATED) {
        comparedDatasourcesReportPresenter.compare(resource.getName(), datasourceName,
            new DatasourceComparisonSuccessCallback(), factory, resource);
      }
    }

    private final class DatasourceComparisonSuccessCallback implements DatasourceCreatedCallback {

      @Override
      public void onSuccess(DatasourceFactoryDto factoryDto, DatasourceDto datasource) {
        transientDatasourceName = datasource.getName();
        getView().enableCompletion();
      }

      @Override
      public void onFailure(DatasourceFactoryDto factoryDto, ClientErrorDto errorDto) {
        // show client error
        Collection<String> errors = DatasourceParsingErrorDtos.getErrors(errorDto);
        for (String error : errors) {
          getView().showError(null, error);
        }
        getView().disableCompletion();
      }
    }

  }

  private final class TransientDatasourceFailureCallback implements ResponseCodeCallback {

    TransientDatasourceFailureCallback() {
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());

      Collection<String> errors = DatasourceParsingErrorDtos.getErrors(errorDto);
      for (String error : errors) {
        getView().showError(null, error);
      }
      getView().disableCompletion();
    }
  }


  public interface Display extends WizardView, CharacterSetDisplay, HasUiHandlers<VariablesImportUiHandlers> {

    enum FormField {
      FILE_SELECTION,
      LOCALE,
      CHARSET
    }

    void gotoPreview();

    void enableCompletion();

    void disableCompletion();

    void setFileSelectionDisplay(FileSelectionPresenter.Display display);

    void setComparedDatasourcesReportDisplay(ComparedDatasourcesReportStepPresenter.Display display);

    void showSpssSpecificPanel(boolean show);

    String getSelectedFile();

    void clearErrors();

    void hideErrors();

    void showError(@Nullable FormField formField, String message);

    HasText getSpssEntityType();

    String getLocale();

    void setConclusionDisplay(ConclusionStepPresenter.Display display);

  }


}
