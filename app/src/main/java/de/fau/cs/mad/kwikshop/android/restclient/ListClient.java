package de.fau.cs.mad.kwikshop.android.restclient;

import java.util.List;

import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.ItemViewModel;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;
import de.fau.cs.mad.kwikshop.common.rest.responses.SharingResponse;
import de.fau.cs.mad.kwikshop.common.rest.responses.SharingCode;
import retrofit.client.Response;

public interface ListClient<TListServer extends DomainListObjectServer> {


    List<TListServer> getLists();

    TListServer getLists(int listId);

    TListServer createList(TListServer list);

    TListServer updateList(int listId, TListServer list);

    Response deleteList(int listId);

    List<DeletionInfo> getDeletedLists();

    ItemViewModel getListItem(int listId, int itemId);

    SharingCode getSharingCode(int listId);

    SharingResponse share(String sharingCode);

    ItemViewModel createItem(int listId, ItemViewModel newItem);

    ItemViewModel updateItem(int listId, int itemId, ItemViewModel item);

    Response deleteListItem(int listId, int itemId);

    List<ItemViewModel> getListItems(int listId);

    List<DeletionInfo> getDeletedListItems(int listId);

}
