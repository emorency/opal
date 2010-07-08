/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.model.client.TableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class TableListView extends Composite implements TableListPresenter.Display {

  @UiTemplate("TableListView.ui.xml")
  interface TableListViewUiBinder extends UiBinder<Panel, TableListView> {
  }

  //
  // Constants
  //

  private static final String TABLE_LIST_WIDTH = "25em";

  private static final int VISIBLE_COUNT = 10;

  private static TableListViewUiBinder uiBinder = GWT.create(TableListViewUiBinder.class);

  @UiField
  ListBox tableList;

  @UiField
  Image addImage;

  @UiField
  Image removeImage;

  //
  // Constructors
  // 

  /**
   * 
   */
  public TableListView() {
    super();
    initWidget(uiBinder.createAndBindUi(this));

    // default dimensions
    setListWidth(TABLE_LIST_WIDTH);
    setListVisibleItemCount(VISIBLE_COUNT);
  }

  //
  // TableListPresenter.Display methods
  //

  @Override
  public void setListVisibleItemCount(int count) {
    tableList.setVisibleItemCount(count);
  }

  @Override
  public void setListWidth(String width) {
    tableList.setWidth(width);
  }

  @Override
  public HasClickHandlers getAddWidget() {
    return addImage;
  }

  @Override
  public HasClickHandlers getRemoveWidget() {
    return removeImage;
  }

  @Override
  public void addTable(TableDto table) {
    tableList.addItem(getDisplayName(table), getFullyQualifiedName(table));
  }

  @Override
  public List<Integer> getSelectedIndices() {
    List<Integer> indices = new ArrayList<Integer>();

    for(int i = 0; i < tableList.getItemCount(); i++) {
      if(tableList.isItemSelected(i)) {
        indices.add(i);
      }
    }

    return indices;
  }

  @Override
  public void removeTable(int index) {
    tableList.removeItem(index);
  }

  @Override
  public void unselectAll() {
    for(int i = 0; i < tableList.getItemCount(); i++) {
      tableList.setItemSelected(i, false);
    }
  }

  //
  // Methods
  //

  private String getDisplayName(TableDto table) {
    return table.getDatasourceName() + " - " + table.getName();
    // return table.getName() + " (" + table.getDatasourceName() + ")";
  }

  private String getFullyQualifiedName(TableDto table) {
    return table.getDatasourceName() + "." + table.getName();
  }

  //
  // GWT methods
  //

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
    // TODO Auto-generated method stub

  }

  @Override
  public void stopProcessing() {
    // TODO Auto-generated method stub

  }

}
