package de.fau.cs.mad.kwikshop.android.viewmodel.tasks;

import android.os.AsyncTask;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.RecipeStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListChangeType;
import de.greenrobot.event.EventBus;

public class RecipeSaveItemTask extends AsyncTask<Void, Void, Void> {

    private final RecipeStorage recipeStorage;
    private final int recipeId;
    private final Item[] items;


    public RecipeSaveItemTask(RecipeStorage recipeStorage, int recipeId, Item... items) {

        if (recipeStorage == null) {
            throw new IllegalArgumentException("'recipeStorage' must not be null");
        }

        if (items == null) {
            throw new IllegalArgumentException("'items' must not be null");
        }

        this.recipeStorage = recipeStorage;
        this.recipeId = recipeId;
        this.items = items;
    }


    @Override
    protected Void doInBackground(Void[] params) {

        if (items.length > 0) {
            Recipe recipe = recipeStorage.loadList(recipeId);
            if (recipe != null) {

                for (Item i : items) {
                    if (recipe.removeItem(i)) {
                        recipe.addItem(i);
                        EventBus.getDefault().post(new RecipeItemChangedEvent(ItemChangeType.PropertiesModified, recipeId, i.getId()));
                    } else {
                        recipe.addItem(i);
                        EventBus.getDefault().post(new RecipeChangedEvent(ListChangeType.ItemsAdded, recipeId));
                        EventBus.getDefault().post(new RecipeItemChangedEvent(ItemChangeType.Added, recipeId, i.getId()));
                    }
                }
            }
        }

        return null;
    }
}

