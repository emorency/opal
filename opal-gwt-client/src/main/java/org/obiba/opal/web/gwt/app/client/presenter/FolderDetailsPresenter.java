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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.FileDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class FolderDetailsPresenter extends WidgetPresenter<FolderDetailsPresenter.Display> {

  public interface Display extends WidgetDisplay {

    void renderRows(FileDto rows);

    HasFieldUpdater getFileNameColumn();

  }

  public interface HasFieldUpdater {
    public void setFieldUpdater(FieldUpdater<FileDto, String> fieldUpdater);
  }

  @Inject
  public FolderDetailsPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {

    super.getDisplay().getFileNameColumn().setFieldUpdater(new FieldUpdater<FileDto, String>() {

      public void update(int index, FileDto file, String value) {
        if(isFolder(file)) {
          downloadFile(file.getPath());
        } else {
          updateTable(file.getPath());
        }
      }

      private boolean isFolder(FileDto file) {
        /* file.getType() == FileDto.FileType.FILE */
        return file.getSize() > 0;
      }

    });
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
    updateTable("/");
  }

  @Override
  public void revealDisplay() {
    updateTable("/");
  }

  private void downloadFile(String path) {
    GWT.log("Click to download file (" + path + ")");
  }

  private void updateTable(String path) {
    ResourceRequestBuilderFactory.<FileDto> newBuilder().forResource("/files" + path).get().withCallback(new ResourceCallback<FileDto>() {
      @Override
      public void onResource(Response response, FileDto resource) {
        getDisplay().renderRows(resource);
      }
    }).send();
  }

}
