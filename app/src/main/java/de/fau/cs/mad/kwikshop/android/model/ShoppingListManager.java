package de.fau.cs.mad.kwikshop.android.model;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.*;
import de.fau.cs.mad.kwikshop.android.model.exceptions.*;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.*;

public class ShoppingListManager extends AbstractListManager<ShoppingList> {

    @Inject
    public ShoppingListManager(ListStorage<ShoppingList> listStorage) {
        super(listStorage);
    }

    @Override
    protected ListNotFoundException listNotFound(int listId) {
        return new ListNotFoundException(String.format("Shopping list (Id %s) not found", listId));
    }

    @Override
    protected Object getAddedListChangedEvent(int id) {
        return new ShoppingListChangedEvent(ShoppingListChangeType.Added, id);
    }

    @Override
    protected Object getDeletedListChangedEvent(int id) {
        return new ShoppingListChangedEvent(ShoppingListChangeType.Deleted, id);
    }

    @Override
    protected Object getPropertiesModifiedListChangedEvent(int id) {
        return new ShoppingListChangedEvent(ShoppingListChangeType.PropertiesModified, id);
    }

}
