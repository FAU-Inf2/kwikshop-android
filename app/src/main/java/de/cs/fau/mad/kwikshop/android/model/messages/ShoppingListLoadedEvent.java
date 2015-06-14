package de.cs.fau.mad.kwikshop.android.model.messages;

import de.cs.fau.mad.kwikshop.android.common.ShoppingList;

public class ShoppingListLoadedEvent {

    private final ShoppingList shoppingList;


    public ShoppingListLoadedEvent(ShoppingList shoppingList) {

        if (shoppingList == null) {
            throw new IllegalArgumentException("'shoppingList' must not be null");
        }

        this.shoppingList = shoppingList;

    }


    public ShoppingList getShoppingList() {
        return this.shoppingList;
    }

}
