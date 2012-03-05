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

import org.obiba.opal.web.gwt.app.client.widgets.presenter.LabelListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.AttributeDialogPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AttributeDialogView extends Composite implements AttributeDialogPresenter.Display {

  @UiTemplate("AttributeDialogView.ui.xml")
  interface MyUiBinder extends UiBinder<DialogBox, AttributeDialogView> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  FlowPanel editableName;

  @UiField
  RadioButton predefinedAttributeNameRadioButton;

  @UiField
  RadioButton customAttributeNameRadioButton;

  @UiField
  InlineLabel uneditableName;

  private boolean nameEditable;

  @UiField
  ListBox labels;

  @UiField
  TextBox customAttributeName;

  @UiField
  SimplePanel simplePanel;

  private LabelListPresenter.Display inputField;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  public AttributeDialogView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);

    nameEditable = true;
    setAttributeNameEditable(nameEditable);
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

  @Override
  public void clear() {
    customAttributeName.setText("");

    if(inputField != null) {
      inputField.clearAttributes();
    }
  }

  @Override
  public void showDialog() {
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public Button getCancelButton() {
    return cancelButton;
  }

  @Override
  public Button getSaveButton() {
    return saveButton;
  }

  @SuppressWarnings("unchecked")
  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
  }

  @Override
  public void setAttributeNameEditable(boolean editable) {
    nameEditable = editable;

    uneditableName.setVisible(!editable);
    editableName.setVisible(editable);
  }

  @Override
  public HandlerRegistration addPredefinedAttributeNameRadioButtonClickHandler(ClickHandler handler) {
    return predefinedAttributeNameRadioButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCustomAttributeNameRadioButtonClickHandler(ClickHandler handler) {
    return customAttributeNameRadioButton.addClickHandler(handler);
  }

  @Override
  public void setLabelsEnabled(boolean enabled) {
    labels.setEnabled(enabled);
  }

  @Override
  public void setCustomAttributeNameEnabled(boolean enabled) {
    customAttributeName.setEnabled(enabled);
  }

  @Override
  public void selectPredefinedAttributeNameRadioButton() {
    predefinedAttributeNameRadioButton.setValue(true, true);
    customAttributeNameRadioButton.setValue(false, true);
  }

  public void selectCustomAttributeNameRadioButton() {
    predefinedAttributeNameRadioButton.setValue(false, true);
    customAttributeNameRadioButton.setValue(true, true);
  }

  @Override
  public HasText getCustomAttributeName() {
    return customAttributeName;
  }

  @Override
  public void addInputField(LabelListPresenter.Display inputField) {
    simplePanel.clear();
    simplePanel.add(inputField.asWidget());
    this.inputField = inputField;
  }

  @Override
  public void removeInputField() {
    simplePanel.clear();
    inputField = null;
  }

  @Override
  public void setNameDropdownList(List<String> labels) {
    this.labels.clear();
    for(String label : labels) {
      this.labels.addItem(label, label);
    }
  }

  @Override
  public HasText getCaption() {
    return dialog;
  }

  @Override
  public HasText getAttributeName() {
    return new HasText() {

      @Override
      public void setText(String arg0) {
      }

      @Override
      public String getText() {
        if(nameEditable) {
          if(predefinedAttributeNameRadioButton.getValue()) {
            return labels.getValue(labels.getSelectedIndex());
          } else {
            return customAttributeName.getText();
          }
        } else {
          return uneditableName.getText();
        }
      }
    };
  }

  @Override
  public void setAttributeName(String attributeName) {
    if(nameEditable) {
      int index = getLabelIndex(attributeName);
      if(index != -1) {
        labels.setItemSelected(index, true);
        this.customAttributeName.setText("");
      } else {
        setLabelsEnabled(false);
        setCustomAttributeNameEnabled(true);
        selectCustomAttributeNameRadioButton();
        this.customAttributeName.setText(attributeName);
      }
    } else {
      uneditableName.setText(attributeName);
    }
  }

  private int getLabelIndex(String name) {
    for(int i = 0; i < labels.getItemCount(); i++) {
      if(labels.getValue(i).equals(name)) return i;
    }
    return -1;
  }
}
