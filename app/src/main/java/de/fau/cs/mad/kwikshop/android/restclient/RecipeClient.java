package de.fau.cs.mad.kwikshop.android.restclient;

import java.util.List;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.rest.responses.SharingCode;
import de.fau.cs.mad.kwikshop.common.rest.responses.SharingResponse;
import de.fau.cs.mad.kwikshop.common.sorting.ItemOrderWrapper;
import de.fau.cs.mad.kwikshop.common.sorting.SortingRequest;
import retrofit.client.Response;

/**
 * Wrapper around RecipeResource implementing the ListClient<T> interface
 */
public class RecipeClient implements ListClient<RecipeServer> {

    private final RecipeResource RecipeResource;



    public RecipeClient(RecipeResource RecipeResource) {

        if(RecipeResource == null) {
            throw new ArgumentNullException("RecipeResource");
        }

        this.RecipeResource = RecipeResource;
    }



    @Override
    public List<RecipeServer> getLists() {
        return RecipeResource.getListSynchronously();
    }

    @Override
    public RecipeServer getLists(int listId) {
        return RecipeResource.getListSynchronously(listId);
    }

    @Override
    public RecipeServer createList(RecipeServer list) {
        return RecipeResource.createListSynchronously(list);
    }

    @Override
    public RecipeServer updateList(int listId, RecipeServer list) {
        return RecipeResource.updateListSynchronously(listId, list);
    }

    @Override
    public Response deleteList(int listId) {
        return RecipeResource.deleteListSynchronously(listId);
    }

    @Override
    public List<DeletionInfo> getDeletedLists() {
        return RecipeResource.getDeletedListsSynchronously();
    }

    @Override
    public Item getListItem(int listId, int itemId) {
        return RecipeResource.getListItemSynchronously(listId, itemId);
    }

    @Override
    public SharingCode getSharingCode(int listId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SharingResponse share(String sharingCode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item createItem(int listId, Item newItem) {
        return RecipeResource.createItemSynchronously(listId, newItem);
    }

    @Override
    public Item updateItem(int listId, int itemId, Item item) {
        return RecipeResource.updateItemSynchronously(listId, itemId, item);
    }

    @Override
    public Response deleteListItem(int listId, int itemId) {
        return RecipeResource.deleteListItemSynchronously(listId, itemId);
    }

    @Override
    public List<Item> getListItems(int listId) {
        return RecipeResource.getListItemsSynchronously(listId);
    }

    @Override
    public List<DeletionInfo> getDeletedListItems(int listId) {
        return RecipeResource.getDeletedListItemsSynchronously(listId);
    }

    @Override
    public void postItemOrder(ItemOrderWrapper itemOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sortList(int listId, SortingRequest sortingRequest) {
        throw new UnsupportedOperationException();
    }
}
