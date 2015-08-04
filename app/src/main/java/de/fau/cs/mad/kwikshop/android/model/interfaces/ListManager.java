package de.fau.cs.mad.kwikshop.android.model.interfaces;

import java.util.Collection;

import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;

/**
 * Interface for storage to access lists of items (Shopping lists, recipes etc)
 */
public interface ListManager<TList extends DomainListObject> {

    Collection<TList> getLists();

    TList getList(int listId);

    Collection<Item> getListItems(int listId);

    Item getListItem(int listId, int itemId);

    int createList();

    TList saveList(int listId);

    Item addListItem(int listId, Item item);

    Item saveListItem(int listId, Item item);

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
