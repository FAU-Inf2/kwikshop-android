package de.fau.cs.mad.kwikshop.android.model.messages;

import de.fau.cs.mad.kwikshop.android.common.Item;

public class ItemLoadedEvent {

    private final int shoppingListId;
    private final Item[] items;


    public ItemLoadedEvent(int shoppingListId, Item... items) {

        if (items == null) {
            throw new IllegalArgumentException("'item' must not be null");
        }

        this.shoppingListId = shoppingListId;
        this.items = items;

    }


    public int getShoppingListId() {
        return shoppingListId;
    }

    public Item[] getItems() {
        return this.items;
    }


}
