package de.fau.cs.mad.kwikshop.android.viewmodel.tasks;

import android.os.AsyncTask;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.RecipeStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeLoadedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListLoadedEvent;
import de.greenrobot.event.EventBus;

public class LoadRecipeTask extends AsyncTask<Object, Object, Collection<Recipe>> {

    private RecipeStorage recipeStorage;
    private EventBus resultBus;
    private int recipeId;
    private final boolean loadAll;

    public LoadRecipeTask(RecipeStorage recipeStorage, EventBus resultBus) {
        this(recipeStorage, resultBus, -1);
    }

    public LoadRecipeTask(RecipeStorage recipeStorage, EventBus resultBus, int recipeId) {

        if (recipeStorage == null) {
            throw new IllegalArgumentException("'recipeStorage' must not be null");
        }

        if (resultBus == null) {
            throw new IllegalArgumentException("'resultBus' must not be null");
        }

        this.recipeStorage = recipeStorage;
        this.resultBus = resultBus;
        this.recipeId = recipeId;
        this.loadAll = recipeId == -1;
    }


    @Override
    protected Collection<Recipe> doInBackground(Object... params) {

        if (loadAll) {

            List<Recipe> recipes = recipeStorage.getAllRecipes();
            for (Recipe recipe : recipes) {
                resultBus.post(new RecipeLoadedEvent(recipe));
            }
            return recipes;

        } else {

            Recipe recipe = recipeStorage.loadRecipe(recipeId);
            resultBus.post(new RecipeLoadedEvent(recipe));
            return Arrays.asList(recipe);
        }
    }

}

