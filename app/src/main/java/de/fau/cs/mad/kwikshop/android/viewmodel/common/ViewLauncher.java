package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import de.fau.cs.mad.kwikshop.common.Recipe;
import android.widget.ArrayAdapter;

public interface ViewLauncher {

    void showAddShoppingListView();

    void showShoppingListDetailsView(int shoppingListId);

    void showShoppingList(int shoppingListId);

    void showDatePicker(int year, int month, int day, int hour, int minute);

    void showYesNoDialog(String title, String message, Command<Void> positiveCommand, Command<Void> negativeCommand);

    void showTextInputDialog(String title, String value, Command<String> positiveCommand, Command<String> negativeCommand);

    void showTextInputDialog(String title, String value,
                             String positiveText, Command<String> positiveCommand,
                             String neutralText, Command<String> neutralCommand,
                             String negativeText, Command<String> negativeCommand);


    void showMessageDialog(String title, String message,
                           String positiveMessage, Command<Void> positiveCommand,
                           String neutralMessage, Command<Void> neutralCommand,
                           String negativeMessage, Command<Void> negativeCommand);

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

    //TODO: move somewhere else, this is not a responsibility of ViewLauncher
    @Deprecated
    void notifyUnitSpinnerChange(ArrayAdapter<String> adapter);

    //TODO: move somewhere else, this is not a responsibility of ViewLauncher
    @Deprecated
    void notifyGroupSpinnerChange(ArrayAdapter<String> adapter);
}
