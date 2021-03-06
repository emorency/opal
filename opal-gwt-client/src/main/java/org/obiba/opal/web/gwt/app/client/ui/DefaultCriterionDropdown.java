/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Widget;

public abstract class DefaultCriterionDropdown extends CriterionDropdown {

  private TextBox matches;

  private HelpBlock matchesHelp;

  public DefaultCriterionDropdown(VariableDto variableDto, String fieldName) {
    super(variableDto, fieldName, null);
  }

  @Override
  public Widget getSpecificControls() {
    // Update radio controls
    RadioButton like = getRadioButton(translations.criterionFiltersMap().get("like"), null);
    like.addClickHandler(new OperatorClickHandler());
    radioControls.add(like);

    RadioButton not_like = getRadioButton(translations.criterionFiltersMap().get("not_like"), null);
    not_like.addClickHandler(new OperatorClickHandler());
    radioControls.add(not_like);

    ListItem specificControls = new ListItem();
    matches = new TextBox();
    matchesHelp = new HelpBlock(translations.criterionFiltersMap().get("wildcards_help"));
    specificControls.addStyleName("controls");

    matches.setPlaceholder(translations.criterionFiltersMap().get("custom_match_query"));
    matches.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        updateMatchCriteriaFilter();
      }
    });
    matches.setVisible(false);
    matchesHelp.setVisible(false);

    specificControls.add(matches);
    specificControls.add(matchesHelp);
    return specificControls;
  }

  @Override
  public void resetSpecificControls() {
    matches.setVisible(false);
    matchesHelp.setVisible(false);
  }

  @Override
  public String getQueryString() {
    String emptyNotEmpty = super.getQueryString();
    if(emptyNotEmpty != null) return emptyNotEmpty;

    if(((CheckBox) radioControls.getWidget(3)).getValue() && !matches.getText().isEmpty()) {
      return fieldName + ":" + matches.getText();
    }

    if(((CheckBox) radioControls.getWidget(4)).getValue() && !matches.getText().isEmpty()) {
      return "NOT " + fieldName + ":" + matches.getText();
    }

    return null;
  }

  private void updateMatchCriteriaFilter() {
    setFilterText();
    doFilterValueSets();

  }

  private void setFilterText() {
    if(matches.getText().isEmpty()) {
      updateCriterionFilter("");
    } else {

      String prefix = ((CheckBox) radioControls.getWidget(3)).getValue()
          ? translations.criterionFiltersMap().get("like") + " "
          : translations.criterionFiltersMap().get("not_like") + " ";

      updateCriterionFilter(prefix + matches.getText());
    }
  }

  private class OperatorClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      matches.setVisible(true);
      matchesHelp.setVisible(true);
      setFilterText();
    }
  }
}
