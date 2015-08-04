package de.fau.cs.mad.kwikshop.android.model;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.exceptions.*;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.*;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.util.EqualityComparer;

public class ShoppingListManager extends AbstractListManager<ShoppingList> {

    private final RegularlyRepeatHelper repeatHelper;

    @Inject
    public ShoppingListManager(ListStorage<ShoppingList> listStorage, RegularlyRepeatHelper repeatHelper,
                               EqualityComparer equalityComparer,
                               SimpleStorage<DeletedList> deletedListStorage,
                               SimpleStorage<DeletedItem> deletedItemStorage) {

        super(listStorage, equalityComparer, deletedListStorage, deletedItemStorage);

        if(repeatHelper == null) {
            throw new ArgumentNullException("repeatHelper");
        }

        this.repeatHelper = repeatHelper;
    }

    @Override
    public int createList() {

        int listId = super.createList();
        repeatHelper.addListCreationRepeatingItems(this, listId);
        return listId;
    }


    @Override
    protected ListNotFoundException listNotFound(int listId) {
        return new ListNotFoundException(String.format("Shopping list (Id %s) not found", listId));
    }

    @Override
    protected ItemNotFoundException itemNotFound(int listId, int itemId) {
        return new ItemNotFoundException(String.format("Could not find item with id %s in shopping list %s", listId, itemId));
    }


    @Override
    protected Object getAddedListChangedEvent(int id) {
        return new ShoppingListChangedEvent(ListChangeType.Added, id);
    }

    @Override
    protected Object getDeletedListChangedEvent(int id) {
        return new ShoppingListChangedEvent(ListChangeType.Deleted, id);
    }

    @Override
    protected Object getPropertiesModifiedListChangedEvent(int id) {
        return new ShoppingListChangedEvent(ListChangeType.PropertiesModified, id);
    }

    @Override
    public ListType getListType() {
        return ListType.ShoppingList;
    }

}
