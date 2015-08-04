package de.fau.cs.mad.kwikshop.android.restclient;

import java.util.List;

import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;
import retrofit.client.Response;

public interface ListClient<TListServer extends DomainListObjectServer> {


    List<TListServer> getListSynchronously();

    TListServer getListSynchronously(int listId);

    TListServer createListSynchronously(TListServer list);

    TListServer updateListSynchronously(int listId, TListServer list);

    Response deleteListSynchronously(int listId);

    List<DeletionInfo> getDeletedListsSynchronously();

    Item getListItemSynchronously(int listId, int itemId);

    Item createItemSynchronously(int listId, Item newItem);

    Item updateItemSynchronously(int listId, int itemId, Item item);

    Response deleteListItemSynchronously(int listId, int itemId);

    List<Item> getListItemsSynchronously(int listId);

    List<DeletionInfo> getDeletedListItemsSynchronously(int listId);

}
