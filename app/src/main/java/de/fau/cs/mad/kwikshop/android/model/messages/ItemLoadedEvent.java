package de.fau.cs.mad.kwikshop.android.model.messages;

import de.fau.cs.mad.kwikshop.android.common.Item;

public class ItemLoadedEvent {

    private final int shoppingListId;
    private final Item item;


    public ItemLoadedEvent(int shoppingListId, Item item) {

        if (item == null) {
            throw new IllegalArgumentException("'item' must not be null");
        }

        this.shoppingListId = shoppingListId;
        this.item = item;

    }


    public int getShoppingListId() {
        return shoppingListId;
    }

    public Item getItem() {
        return this.item;
    }


}
