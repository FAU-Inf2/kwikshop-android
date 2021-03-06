package de.fau.cs.mad.kwikshop.android.restclient;

import java.util.List;

import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;
import de.fau.cs.mad.kwikshop.common.rest.responses.SharingResponse;
import de.fau.cs.mad.kwikshop.common.rest.responses.SharingCode;
import de.fau.cs.mad.kwikshop.common.sorting.ItemOrderWrapper;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;
import retrofit.client.Response;

public interface ListClient<TListServer extends DomainListObjectServer> {


    List<TListServer> getLists();

    TListServer getLists(int listId);

    TListServer createList(TListServer list);

    TListServer updateList(int listId, TListServer list);

    Response deleteList(int listId);

    List<DeletionInfo> getDeletedLists();

    Item getListItem(int listId, int itemId);

    SharingCode getSharingCode(int listId);

    SharingResponse share(String sharingCode);

    Item createItem(int listId, Item newItem);

    Item updateItem(int listId, int itemId, Item item);

    Response deleteListItem(int listId, int itemId);

    List<Item> getListItems(int listId);

    List<DeletionInfo> getDeletedListItems(int listId);

    void postItemOrder(ItemOrderWrapper itemOrder);

    void sortList(int listId, SortingRequest sortingRequest);
}
