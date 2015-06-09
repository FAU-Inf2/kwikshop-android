package de.cs.fau.mad.kwikshop.android.viewmodel.common;

public interface ViewLauncher {

    void showAddShoppingListView();

    void showShoppingListDetailsView(int shoppingListId);

    void showShoppingList(int shoppingListId);

    void showDatePicker(int year, int month, int day, int hour, int minute);

    void showYesNoDialog(String title, String message, Command positiveCommand, Command negativeCommand);
}
