package org.obiba.opal.web.gwt.app.client.bookmark.list;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.BookmarkHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.BookmarkDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import static org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter.Mode;

public class BookmarkListView extends ViewWithUiHandlers<BookmarkListUiHandlers>
    implements BookmarkListPresenter.Display {

  private static final int SORTABLE_COLUMN_RESOURCE = 0;

  private static final int SORTABLE_COLUMN_CREATED = 2;

  private final PlaceManager placeManager;

  private final TranslationMessages translationMessages;

  private ColumnSortEvent.ListHandler<BookmarkDto> typeSortHandler;

  private CheckboxColumn<BookmarkDto> checkColumn;

  interface Binder extends UiBinder<Widget, BookmarkListView> {}

  @UiField
  OpalSimplePager pager;

  @UiField
  Table<BookmarkDto> table;

  @UiField
  Alert selectAllAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  IconAnchor clearSelectionAnchor;

  @UiField
  IconAnchor removeBookmark;

  private final Translations translations;

  private final ListDataProvider<BookmarkDto> dataProvider = new ListDataProvider<BookmarkDto>();

  private CreateColumn createColumn;

  private DeleteActionColumn deleteActionColumn;

  @Inject
  public BookmarkListView(Binder uiBinder, Translations translations, PlaceManager placeManager,
      TranslationMessages translationMessages) {
    this.translations = translations;
    this.placeManager = placeManager;
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  @Override
  public void renderRows(List<BookmarkDto> rows) {
    dataProvider.setList(rows);
    pager.firstPage();
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
    typeSortHandler.setList(dataProvider.getList());
    ColumnSortEvent.fire(table, table.getColumnSortList());
  }

  @Override
  public HasActionHandler<BookmarkDto> getActions() {
    return deleteActionColumn == null ? null : deleteActionColumn;
  }

  @Override
  public void setMode(Mode mode) {
    switch(mode) {
      case VIEW_ONLY:
        if(createColumn != null) {
          table.removeColumn(createColumn);
          createColumn = null;
        }
        if(deleteActionColumn != null) {
          table.removeColumn(deleteActionColumn);
          deleteActionColumn = null;
        }
        break;
      case VIEW_AND_DELETE:
        deleteActionColumn = new DeleteActionColumn();
        deleteActionColumn.setActionHandler(new ActionHandler<BookmarkDto>() {
          @Override
          public void doAction(BookmarkDto bookmarkDto, String actionName) {
            getUiHandlers().onDelete(Arrays.asList(bookmarkDto));
          }
        });
        table.addColumn(deleteActionColumn, translations.actionsLabel());
        break;
    }
  }

  private void initTable() {
    checkColumn = new CheckboxColumn<BookmarkDto>(new BookmarkDtoDisplay());

    table.setVisibleRange(0, 10);
    table.addColumn(checkColumn, checkColumn.getCheckColumnHeader());
    table.addColumn(new BookmarkColumn(placeManager), translations.resourceLabel());
    table.addColumn(new TypeColumn(translations), translations.typeLabel());
    table.addColumn(createColumn = new CreateColumn(), translations.createdLabel());
    dataProvider.addDataDisplay(table);
    //table.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
    pager.setDisplay(table);
    typeSortHandler = new ColumnSortEvent.ListHandler<BookmarkDto>(dataProvider.getList());
    typeSortHandler.setComparator(table.getColumn(SORTABLE_COLUMN_RESOURCE), new ResourceComparator());
    typeSortHandler.setComparator(table.getColumn(SORTABLE_COLUMN_CREATED), new LastUpdateComparator());
    table.getHeader(SORTABLE_COLUMN_RESOURCE).setHeaderStyleNames("sortable-header-column");
    table.getHeader(SORTABLE_COLUMN_CREATED).setHeaderStyleNames("sortable-header-column");
    table.getColumnSortList().push(table.getColumn(SORTABLE_COLUMN_CREATED));
    table.getColumnSortList().push(table.getColumn(SORTABLE_COLUMN_RESOURCE));
    table.addColumnSortHandler(typeSortHandler);
  }

  @UiHandler("removeBookmark")
  public void onRemove(ClickEvent event) {
    getUiHandlers().onDelete(checkColumn.getSelectedItems());
  }

  private static class BookmarkColumn extends Column<BookmarkDto, BookmarkDto> {

    private BookmarkColumn(PlaceManager placeManager) {
      super(new PlaceRequestCell<BookmarkDto>(placeManager) {
        @Override
        public PlaceRequest getPlaceRequest(BookmarkDto bookmarkDto) {
          return BookmarkHelper.createPlaceRequest(bookmarkDto.getResource());
        }

        @Override
        public String getText(BookmarkDto bookmarkDto) {
          return BookmarkHelper.createMagmaPath(bookmarkDto.getResource());
        }
      });

      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public BookmarkDto getValue(BookmarkDto object) {
      return object;
    }
  }

  private static final class ResourceComparator implements Comparator<BookmarkDto> {
    @Override
    public int compare(BookmarkDto o1, BookmarkDto o2) {
      return o1.getResource().compareTo(o2.getResource());
    }
  }

  private static final class LastUpdateComparator implements Comparator<BookmarkDto> {
    @Override
    public int compare(BookmarkDto o1, BookmarkDto o2) {
      Moment m1 = Moment.create(o1.getCreated());
      Moment m2 = Moment.create(o2.getCreated());
      if(m1 == null) {
        return m2 == null ? 0 : 1;
      }
      return m2 == null ? -1 : m2.unix() - m1.unix();
    }
  }

  private static final class TypeColumn extends TextColumn<BookmarkDto> {

    private final Translations translations;

    private TypeColumn(Translations translations) {
      this.translations = translations;
    }

    @Override
    public String getValue(BookmarkDto bookmarkDto) {
      return translations.bookmarkTypeMap().get(bookmarkDto.getType().getName());
    }
  }

  private static final class CreateColumn extends TextColumn<BookmarkDto> {

    private CreateColumn() {
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public String getValue(BookmarkDto bookmarkDto) {
      return bookmarkDto.hasCreated()//
          ? Moment.create(bookmarkDto.getCreated()).fromNow() //
          : "";

    }
  }

  private static final class DeleteActionColumn extends ActionsColumn<BookmarkDto> {

    private DeleteActionColumn() {
      super(new ActionsProvider<BookmarkDto>() {
        @Override
        public String[] allActions() {
          return new String[] { ActionsColumn.REMOVE_ACTION };
        }

        @Override
        public String[] getActions(BookmarkDto value) {
          return allActions();
        }
      });
    }
  }

  private class BookmarkDtoDisplay implements CheckboxColumn.Display<BookmarkDto> {

    @Override
    public Table<BookmarkDto> getTable() {
      return table;
    }

    @Override
    public Object getItemKey(BookmarkDto item) {
      return item.getResource();
    }

    @Override
    public IconAnchor getClearSelection() {
      return clearSelectionAnchor;
    }

    @Override
    public IconAnchor getSelectAll() {
      return selectAllAnchor;
    }

    @Override
    public HasText getSelectAllStatus() {
      return selectAllStatus;
    }

    @Override
    public ListDataProvider<BookmarkDto> getDataProvider() {
      return dataProvider;
    }

    @Override
    public String getNItemLabel(int nb) {
      return translationMessages.nBookmarksLabel(nb).toLowerCase();
    }

    @Override
    public Alert getAlert() {
      return selectAllAlert;
    }
  }
}
