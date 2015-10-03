package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.ShoppingList;

import android.content.Intent;
import android.widget.ArrayAdapter;

public interface ViewLauncher {

    void showAddShoppingListView();

    void showShoppingListDetailsView(int shoppingListId);

    void showShoppingList(int shoppingListId);

    void returnToShoppingList(int shoppingListId);

    void showShoppingListInShoppingMode(int shoppingListId);

    void showShoppingListInEditMode(int shoppingListId);


    void showShoppingListWithSupermarketDialog(int shoppingListId);

    void showDatePicker(int year, int month, int day, int hour, int minute);

    void showYesNoDialog(String title, String message, Command<Void> positiveCommand, Command<Void> negativeCommand);

    void showTextInputDialog(String title, String value, Command<String> positiveCommand, Command<String> negativeCommand);

    void showTextInputDialog(String title, String description, String value, Command<String> positiveCommand, Command<String> negativeCommand);

    void showTextInputDialog(String title, String value,
                             String positiveText, Command<String> positiveCommand,
                             String neutralText, Command<String> neutralCommand,
                             String negativeText, Command<String> negativeCommand);

    void showNumberInputDialog(String title, String message, int value,
                               String positiveText, Command<String> positiveCommand,
                               String neutralText, Command<String> neutralCommand,
                               String negativeText, Command<String> negativeCommand);

    void showMessageDialog(String title, String message,
                           String positiveMessage, Command<Void> positiveCommand,
                           String neutralMessage, Command<Void> neutralCommand,
                           String negativeMessage, Command<Void> negativeCommand);

    void showMessageDialog(String title, String message,
                           String positiveMessage, Command<Void> positiveCommand,
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

    void showAddRecipeDialog(final ListManager<ShoppingList> listManager, final ListManager<Recipe> recipeManager, int listId, boolean fromShoppingList);

    //TODO: move somewhere else, this is not a responsibility of ViewLauncher
    @Deprecated
    void notifyUnitSpinnerChange(ArrayAdapter<String> adapter);

    //TODO: move somewhere else, this is not a responsibility of ViewLauncher
    @Deprecated
    void notifyGroupSpinnerChange(ArrayAdapter<String> adapter);

    void launchEmailChooser(String chooserTitle, String recipient, String subject, String body);

    void showMessageDialogWithCheckbox(String title, String message,
                                       String positiveMessage, final Command<Void> positiveCommand,
                                       String neutralMessage, final Command<Void> neutralCommand,
                                       String negativeMessage, final Command<Void> negativeCommand,
                                       String checkBoxMessage, boolean checkBoxDefaultValue,
                                       final Command<Void> checkedCommand, final Command<Void> uncheckedCommand);


    void showProgressDialog(String message,
                            String negativeMessage, boolean cancelable,
                            final Command<Void> negativeCommand);

    void showProgressDialogWithoutButton(String message, final Command<Void> command);


    void showMessageDialogWithRadioButtons(String title, CharSequence[] items,
                                           String positiveMessage, final Command<Integer> positiveCommand,
                                           String neutralMessage, final Command<Integer> neutralCommand,
                                           String negativeMessage, final Command<Integer> negativeCommand);


    void showListOfShoppingListsActivity();

    void showMultiplyChoiceDialog(String title, String[] typeNames, boolean[] checkedTypes,
                                  final Command<Integer> selectCommand,
                                  final Command<Integer> unSelectCommand,
                                  String positiveMessage, final Command<Void> positiveCommand,
                                  String negativeMessage, final Command<Void> negativeCommand);

    void showLocationActivity();

    void finishActivity();

    void dismissDialog();

    void startActivity(Intent intent);

    void showProgressDialogWithListID(String message, String negativeMessage, final int listId, boolean cancelable, final Command<Integer> negativeCommand);

    void restartActivity();


}