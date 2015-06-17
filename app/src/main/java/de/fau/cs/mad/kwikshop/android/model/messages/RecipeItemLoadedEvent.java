package de.fau.cs.mad.kwikshop.android.model.messages;

import de.fau.cs.mad.kwikshop.android.common.Item;

public class RecipeItemLoadedEvent {

    private final int recipeId;
    private final Item item;


    public RecipeItemLoadedEvent(int recipeId, Item item) {

        if (item == null) {
            throw new IllegalArgumentException("'item' must not be null");
        }

        this.recipeId = recipeId;
        this.item = item;

    }


    public int getRecipeId() {
        return recipeId;
    }

    public Item getItem() {
        return this.item;
    }


}

