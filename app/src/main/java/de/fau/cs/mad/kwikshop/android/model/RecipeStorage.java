package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.Recipe;

public class RecipeStorage {

    public int createRecipe() {
        Recipe newRecipe = new Recipe();
        try {
            ListStorageFragment.getDatabaseHelper().getRecipeDao().create(newRecipe);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return newRecipe.getId();
    }

    public List<Recipe> getAllRecipes() {
        try {
            ArrayList<Recipe> recipes = new ArrayList<>();
            for(Recipe recipe : ListStorageFragment.getDatabaseHelper().getRecipeDao()) {
                recipes.add(recipe);
            }
            return Collections.unmodifiableList(recipes);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public Recipe loadRecipe(Integer recipeId) {
        Recipe loadedRecipe;
        try {
            loadedRecipe = ListStorageFragment.getDatabaseHelper().getRecipeDao().queryForId(recipeId);

            for (Item i : loadedRecipe.getItems()) {

                if (i.getUnit() != null) {
                    ListStorageFragment.getDatabaseHelper().getUnitDao().refresh(i.getUnit());
                }

                if (i.getGroup() != null) {
                    ListStorageFragment.getDatabaseHelper().getGroupDao().refresh(i.getGroup());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return loadedRecipe;
    }

    public Boolean saveRecipe(Recipe recipe) {
        try {
            // Update all Items first
            for (Item item : recipe.getItems()) {
                ListStorageFragment.getDatabaseHelper().getItemDao().update(item);
            }
            ListStorageFragment.getDatabaseHelper().getRecipeDao().update(recipe);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean deleteRecipe(Integer id) {
        try {
            deleteRecipeItems(id);
            ListStorageFragment.getDatabaseHelper().getRecipeDao().deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void deleteItem(int id) {
        try {
            ListStorageFragment.getDatabaseHelper().getItemDao().deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteRecipeItems(Integer listid) {
        DeleteBuilder db;
        try {
            db = ListStorageFragment.getDatabaseHelper().getItemDao().deleteBuilder();
            db.where().eq(Item.FOREIGN_RECIPE_FIELD_NAME, listid); // Delete all items that belong to this list
            ListStorageFragment.getDatabaseHelper().getItemDao().delete(db.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
