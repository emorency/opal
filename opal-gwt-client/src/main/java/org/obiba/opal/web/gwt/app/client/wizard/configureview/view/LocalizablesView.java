/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.LabelValueColumn;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.LocalizablesPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.LocalizablesPresenter.Localizable;
import org.obiba.opal.web.model.client.opal.LocaleDto;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import static org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.LocalizablesPresenter.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.LocalizablesPresenter.EDIT_ACTION;

@Deprecated
public class LocalizablesView extends Composite implements LocalizablesPresenter.Display {

  public static final int PAGE_SIZE = 10;

  private String valueColumnName;

  @UiTemplate("LocalizablesView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, LocalizablesView> {
  }

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  Button addButton;

  @UiField
  ListBox localeListBox;

  @UiField
  CellTable<Localizable> localizablesTable;

  @UiField
  SimplePager pager;

  ListDataProvider<Localizable> dataProvider = new ListDataProvider<Localizable>();

  private HasActionHandler<Localizable> actionsColumn;

  //
  // Constructors
  //

  public LocalizablesView() {
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  //
  // LocalizableTabPresenter.Display Methods
  //

  @Override
  public void setAddButtonText(String addButtonText) {
    addButton.setText(addButtonText);
  }

  @Override
  public void setLocales(JsArray<LocaleDto> locales) {
    localeListBox.clear();

    for(int i = 0; i < locales.length(); i++) {
      LocaleDto locale = locales.get(i);
      localeListBox.addItem(locale.getDisplay(), locale.getName());
    }
  }

  @Override
  public String getSelectedLocale() {
    int selectedIndex = localeListBox.getSelectedIndex();
    return selectedIndex != -1 ? localeListBox.getValue(selectedIndex) : null;
  }

  @Override
  public void setTableData(List<Localizable> localizables) {
    dataProvider.setList(localizables);
    pager.setVisible(localizables.size() > localizablesTable.getPageSize());
    pager.firstPage();
    dataProvider.refresh();
  }

  @Override
  public HasActionHandler<Localizable> getActionsColumn() {
    return actionsColumn;
  }

  @Override
  public HandlerRegistration addLocaleChangeHandler(ChangeHandler handler) {
    return localeListBox.addChangeHandler(handler);
  }

  @Override
  public HandlerRegistration addAddButtonClickHandler(ClickHandler handler) {
    return addButton.addClickHandler(handler);
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  private void initTable() {
    addTableColumns();
    addPager();
    dataProvider.addDataDisplay(localizablesTable);
  }

  private void addPager() {
    localizablesTable.setPageSize(PAGE_SIZE);
    pager.setDisplay(localizablesTable);
  }

  @Override
  public void setValueColumnName(String valueColumnName) {
    this.valueColumnName = valueColumnName;
  }

  @SuppressWarnings("unchecked")
  private void addTableColumns() {

    localizablesTable.addColumn(new LabelValueColumn<Localizable>() {

      @Override
      public String getLabel(Localizable localizable) {
        return localizable.getNamespace();
      }

      @Override
      public String getContent(Localizable localizable) {
        return localizable.getName();
      }
    }, translations.nameLabel());

    Header<String> valueHeader = new Header<String>(new TextCell()) {
      @Override
      public String getValue() {
        return valueColumnName;
      }
    };
    TextColumn<Localizable> valueColumn = new TextColumn<Localizable>() {
      @Override
      public String getValue(Localizable object) {
        return object.getLabel();
      }
    };
    localizablesTable.addColumn(valueColumn, valueHeader);

    actionsColumn = new ActionsColumn<Localizable>(EDIT_ACTION, DELETE_ACTION);
    localizablesTable.addColumn((Column<Localizable, ?>) actionsColumn, translations.actionsLabel());
  }

  @Override
  public void formEnable(boolean enabled) {
    addButton.setEnabled(enabled);
    localeListBox.setEnabled(enabled);
  }

}
