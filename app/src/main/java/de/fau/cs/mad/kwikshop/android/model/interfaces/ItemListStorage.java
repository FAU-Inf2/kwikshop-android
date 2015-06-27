package de.fau.cs.mad.kwikshop.android.model.interfaces;

import java.util.Collection;

import de.fau.cs.mad.kwikshop.android.common.*;

/**
 * Interface for storage to access lists of items (Shopping lists, recipes etc)
 */
public interface ItemListStorage<TList> {

    Collection<TList> getLists();

    TList getList(int listId);

    Collection<Item> getListItems(int listId);

    Item getListItem(int listId, int itemId);

    TList saveList(TList list);

    Item saveListItem(Item item);

    boolean deleteItem(int itemId);

    boolean deleteList(int listId);

}
