package de.cs.fau.mad.kwikshop.android.model.messages;

public class ItemDeleteEvent {

    //region Fields

    private final int m_ShoppingListId;
    private final int m_ItemId;

    //endregion


    //region Constructor

    public ItemDeleteEvent(int shoppingListId, int itemId) {
        this.m_ShoppingListId = shoppingListId;
        this.m_ItemId = itemId;
    }

    //endregion


    //region Getters

    public int getShoppingListId() {
        return m_ShoppingListId;
    }

    public int getItemId() {
        return m_ItemId;
    }

    //endregion

}
