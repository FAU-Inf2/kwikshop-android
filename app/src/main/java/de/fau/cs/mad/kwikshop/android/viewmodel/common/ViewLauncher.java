package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;

import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.view.ManageUnitsFragment;

public interface ViewLauncher {

    void showAddShoppingListView();

    void showShoppingListDetailsView(int shoppingListId);

    void showShoppingList(int shoppingListId);

    void showDatePicker(int year, int month, int day, int hour, int minute);

    void showYesNoDialog(String title, String message, Command positiveCommand, Command negativeCommand);

    void showToast(String message, int duration);

    void showToast(int resId, int duration);

    void showItemDetailsView(int shoppingListId);

    void showItemDetailsView(int shoppingListId, int itemId);

    void showAddRecipeView();

    void showRecipe(int recipeId);

    void showRecipeDetailsView(int recipeId);

    void RecipeShowItemDetailsView(int recipeId);

    void RecipeShowItemDetailsView(int recipeId, int itemId);

    void showReminderView(int listId, int itemID);

    void showAddRecipeDialog(final Recipe recipe);

    void notifyUnitSpinnerChange(ArrayAdapter<String> adapter);

    void notifyGroupSpinnerChange(ArrayAdapter<String> adapter);
}
