package de.fau.cs.mad.kwikshop.android.model.messages;

import de.fau.cs.mad.kwikshop.common.ItemViewModel;

public class RecipeItemLoadedEvent {

    private final int recipeId;
    private final ItemViewModel item;


    public RecipeItemLoadedEvent(int recipeId, ItemViewModel item) {

        if (item == null) {
            throw new IllegalArgumentException("'item' must not be null");
        }

        this.recipeId = recipeId;
        this.item = item;

    }


    public int getRecipeId() {
        return recipeId;
    }

    public ItemViewModel getItem() {
        return this.item;
    }


}

