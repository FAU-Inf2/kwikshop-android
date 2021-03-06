package de.fau.cs.mad.kwikshop.android.restclient;

import java.util.List;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.rest.responses.SharingResponse;
import de.fau.cs.mad.kwikshop.common.rest.responses.SharingCode;
import de.fau.cs.mad.kwikshop.common.sorting.ItemOrderWrapper;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;
import retrofit.client.Response;

/**
 * Wrapper around ShoppingListResource implementing the ListClient<T> interface
 */
public class ShoppingListClient implements ListClient<ShoppingListServer> {

    private final ShoppingListResource shoppingListResource;



    public ShoppingListClient(ShoppingListResource shoppingListResource) {

        if(shoppingListResource == null) {
            throw new ArgumentNullException("shoppingListResource");
        }

        this.shoppingListResource = shoppingListResource;
    }



    @Override
    public List<ShoppingListServer> getLists() {
        return shoppingListResource.getListSynchronously();
    }

    @Override
    public ShoppingListServer getLists(int listId) {
        return shoppingListResource.getListSynchronously(listId);
    }

    @Override
    public ShoppingListServer createList(ShoppingListServer list) {
        return shoppingListResource.createListSynchronously(list);
    }

    @Override
    public ShoppingListServer updateList(int listId, ShoppingListServer list) {
        return shoppingListResource.updateListSynchronously(listId, list);
    }

    @Override
    public Response deleteList(int listId) {
        return shoppingListResource.deleteListSynchronously(listId);
    }

    @Override
    public List<DeletionInfo> getDeletedLists() {
        return shoppingListResource.getDeletedListsSynchronously();
    }

    @Override
    public Item getListItem(int listId, int itemId) {
        return shoppingListResource.getListItemSynchronously(listId, itemId);
    }

    @Override
    public SharingCode getSharingCode(int listId) {
        return shoppingListResource.getSharingCodeSynchronously(listId);
    }

    @Override
    public SharingResponse share(String sharingCode) {
        return shoppingListResource.shareSynchronously(sharingCode);
    }

    @Override
    public Item createItem(int listId, Item newItem) {
        return shoppingListResource.createItemSynchronously(listId, newItem);
    }

    @Override
    public Item updateItem(int listId, int itemId, Item item) {
        return shoppingListResource.updateItemSynchronously(listId, itemId, item);
    }

    @Override
    public Response deleteListItem(int listId, int itemId) {
        return shoppingListResource.deleteListItemSynchronously(listId, itemId);
    }

    @Override
    public List<Item> getListItems(int listId) {
        return shoppingListResource.getListItemsSynchronously(listId);
    }

    @Override
    public List<DeletionInfo> getDeletedListItems(int listId) {
        return shoppingListResource.getDeletedListItemsSynchronously(listId);
    }

    @Override
    public void postItemOrder(ItemOrderWrapper itemOrder) {
        shoppingListResource.postBoughtItemsSynchronously(itemOrder);
    }

    @Override
    public void sortList(int listId, SortingRequest sortingRequest) {
        shoppingListResource.sortSynchronously(listId, sortingRequest);
    }
}
