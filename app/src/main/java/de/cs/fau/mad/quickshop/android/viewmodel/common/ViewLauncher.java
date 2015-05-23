package de.cs.fau.mad.quickshop.android.viewmodel.common;

import de.cs.fau.mad.quickshop.android.viewmodel.ShoppingListDetailsViewModel;

public interface ViewLauncher {

    void showAddShoppingListView();

    void showShoppingListDetailsView(int shoppingListId);

    void showShoppingList(int shoppingListId);

}
