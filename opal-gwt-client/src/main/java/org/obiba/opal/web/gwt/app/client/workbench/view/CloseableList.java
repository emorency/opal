/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.workbench.view;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A list of closeable items.
 */
public class CloseableList extends UList {

  public CloseableList() {
    super();
    addStyleName("closeables");
  }

  public void addItem(final String text) {
    if(Strings.isNullOrEmpty(text)) return;

    final ListItem item = new ListItem();

    item.add(new InlineLabel(text));
    Anchor close = new Anchor("x");
    close.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        CloseableList.this.remove(item);
      }
    });
    item.add(close);

    add(item);
  }

  public void removeItem(String text) {
    ListItem it = getItem(text);

    if(it != null) {
      remove(it);
    }
  }

  private ListItem getItem(String text) {
    ListItem it = null;
    for(int i = 0; i < getWidgetCount(); i++) {
      ListItem item = (ListItem) getWidget(i);
      Widget label = item.getWidget(0);
      if(label instanceof HasText && ((HasText) label).getText().equals(text)) {
        it = item;
        break;
      }
    }
    return it;
  }

  public List<String> getItemTexts() {
    ImmutableList.Builder<String> builder = ImmutableList.<String> builder();
    for(int i = 0; i < getWidgetCount(); i++) {
      ListItem item = (ListItem) getWidget(i);
      Widget label = item.getWidget(0);
      if(label instanceof HasText) {
        builder.add(((HasText) label).getText());
      }
    }
    return builder.build();
  }
}
