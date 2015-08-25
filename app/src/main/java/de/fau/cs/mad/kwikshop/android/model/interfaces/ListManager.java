package de.fau.cs.mad.kwikshop.android.model.interfaces;

import java.util.Collection;

import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.common.ItemViewModel;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;

/**
 * Interface for storage to access lists of items (Shopping lists, recipes etc)
 */
public interface ListManager<TList extends DomainListObject> {

    Collection<TList> getLists();

    TList getList(int listId);

    Collection<ItemViewModel> getListItems(int listId);

    ItemViewModel getListItem(int listId, int itemId);

    int createList();

    TList saveList(int listId);

    /**
     * Saves the specified list but does not set the list's modifiedSinceLastSync flag
     *
     * IMPORTANT: Only call this if you're sure this is what you want.
     *
     * @param listId The is of the list to save
     * @return Returns the updated list object
     */
    TList saveListWithoutModificationFlag(int listId);

    ItemViewModel addListItem(int listId, ItemViewModel item);

    ItemViewModel saveListItem(int listId, ItemViewModel item);

    /**
     * Saves the specified list item witout setting its modifiedSinceLastSync flag
     *
     * IMPORTANT: Only call this if you're sure this is what you want.
     *
     */
    ItemViewModel saveListItemWithoutModificationFlag(int listId, ItemViewModel item);

    boolean deleteItem(int listId, int itemId);

    boolean deleteList(int listId);

    /**
     * Clears all the data only held for sync
     * In particular it sets back all the modifiedSinceLastSync flags for lists and items
     * and deletes all DeletedList and DeletedItem entries
     */
    void clearSyncData();

    ListType getListType();
}
