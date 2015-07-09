package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import de.fau.cs.mad.kwikshop.common.Recipe;

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
}
