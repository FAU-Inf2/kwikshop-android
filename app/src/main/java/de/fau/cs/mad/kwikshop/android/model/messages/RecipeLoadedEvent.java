package de.fau.cs.mad.kwikshop.android.model.messages;

import de.fau.cs.mad.kwikshop.android.common.Recipe;

public class RecipeLoadedEvent {

    private final Recipe recipe;


    public RecipeLoadedEvent(Recipe recipe) {

        if (recipe == null) {
            throw new IllegalArgumentException("'recipe' must not be null");
        }

        this.recipe = recipe;

    }


    public Recipe getRecipe() {
        return this.recipe;
    }


}
