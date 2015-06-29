package de.fau.cs.mad.kwikshop.android.model;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.android.model.exceptions.ListNotFoundException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;

public class RecipeManager extends AbstractListManager<Recipe> {

    @Inject
    public RecipeManager(ListStorage<Recipe> listStorage) {
        super(listStorage);
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
    protected ListType getListType() {
        return null;
    }
}
