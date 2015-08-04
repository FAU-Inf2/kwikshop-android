package de.fau.cs.mad.kwikshop.android.model;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.android.model.exceptions.ListNotFoundException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.common.util.EqualityComparer;

public class RecipeManager extends AbstractListManager<Recipe> {

    @Inject
    public RecipeManager(ListStorage<Recipe> listStorage,EqualityComparer equalityComparer,
                         SimpleStorage<DeletedList> deletedListStorage,
                         SimpleStorage<DeletedItem> deletedItemStorage) {

        super(listStorage,equalityComparer, deletedListStorage, deletedItemStorage);
    }

    @Override
    protected ListNotFoundException listNotFound(int listId) {
        return new ListNotFoundException(String.format("Recipe (Id %s) not found", listId));
    }

    @Override
    protected ItemNotFoundException itemNotFound(int listId, int itemId) {
        return new ItemNotFoundException(String.format("Could not find item with id %s in Recipe %s", listId, itemId));
    }

    @Override
    protected Object getAddedListChangedEvent(int id) {
        return new RecipeChangedEvent(ListChangeType.Added, id);
    }

    @Override
    protected Object getDeletedListChangedEvent(int id) {
        return new RecipeChangedEvent(ListChangeType.Deleted, id);
    }

    @Override
    protected Object getPropertiesModifiedListChangedEvent(int id) {
        return new RecipeChangedEvent(ListChangeType.PropertiesModified, id);
    }

    @Override
    public ListType getListType() {
        return ListType.Recipe;
    }
}
