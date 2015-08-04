package de.fau.cs.mad.kwikshop.android.restclient;

import java.util.List;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
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
    public List<RecipeServer> getListSynchronously() {
        return RecipeResource.getListSynchronously();
    }

    @Override
    public RecipeServer getListSynchronously(int listId) {
        return RecipeResource.getListSynchronously(listId);
    }

    @Override
    public RecipeServer createListSynchronously(RecipeServer list) {
        return RecipeResource.createListSynchronously(list);
    }

    @Override
    public RecipeServer updateListSynchronously(int listId, RecipeServer list) {
        return RecipeResource.updateListSynchronously(listId, list);
    }

    @Override
    public Response deleteListSynchronously(int listId) {
        return RecipeResource.deleteListSynchronously(listId);
    }

    @Override
    public List<DeletionInfo> getDeletedListsSynchronously() {
        return RecipeResource.getDeletedListsSynchronously();
    }

    @Override
    public Item getListItemSynchronously(int listId, int itemId) {
        return RecipeResource.getListItemSynchronously(listId, itemId);
    }

    @Override
    public Item createItemSynchronously(int listId, Item newItem) {
        return RecipeResource.createItemSynchronously(listId, newItem);
    }

    @Override
    public Item updateItemSynchronously(int listId, int itemId, Item item) {
        return RecipeResource.updateItemSynchronously(listId, itemId, item);
    }

    @Override
    public Response deleteListItemSynchronously(int listId, int itemId) {
        return RecipeResource.deleteListItemSynchronously(listId, itemId);
    }

    @Override
    public List<Item> getListItemsSynchronously(int listId) {
        return RecipeResource.getListItemsSynchronously(listId);
    }

    @Override
    public List<DeletionInfo> getDeletedListItemsSynchronously(int listId) {
        return RecipeResource.getDeletedListItemsSynchronously(listId);
    }
}
