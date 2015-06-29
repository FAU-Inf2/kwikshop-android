package de.fau.cs.mad.kwikshop.android.model;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.exceptions.ItemNotFoundException;
import de.fau.cs.mad.kwikshop.android.model.exceptions.ListNotFoundException;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;

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
        return null;
    }

    @Override
    protected Object getDeletedListChangedEvent(int id) {
        return null;
    }

    @Override
    protected Object getPropertiesModifiedListChangedEvent(int id) {
        return null;
    }

    @Override
    protected ListType getListType() {
        return null;
    }
}
