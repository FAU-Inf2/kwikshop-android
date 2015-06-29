package de.fau.cs.mad.kwikshop.android.viewmodel.tasks;

import android.os.AsyncTask;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeItemLoadedEvent;
import de.greenrobot.event.EventBus;

public class RecipeLoadItemTask extends AsyncTask<Object, Object, Item> {


    private ListStorage<Recipe> recipeStorage;
    private EventBus resultBus;
    private int recipeId;
    private int itemId;


    public RecipeLoadItemTask(ListStorage<Recipe> recipeStorage, EventBus resultBus, int recipeId, int itemId) {

        if (recipeStorage == null) {
            throw new IllegalArgumentException("'recipeStorage' must not be null");
        }

        if (resultBus == null) {
            throw new IllegalArgumentException("'resultBus' must not be null");
        }

        this.recipeStorage = recipeStorage;
        this.resultBus = resultBus;

        this.recipeId = recipeId;
        this.itemId = itemId;

    }


    @Override
    protected Item doInBackground(Object... params) {

        //TODO: reimplement this if we can load single items directly from the database
        Recipe recipe = recipeStorage.loadList(recipeId);
        Item item = recipe.getItem(itemId);
        if (item != null) {
            resultBus.post(new RecipeItemLoadedEvent(recipeId, item));
        }

        return null;
    }
}