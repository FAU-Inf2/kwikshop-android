package de.fau.cs.mad.kwikshop.android.model.messages;

public class ItemChangedEvent {

    //region Fields

    private final ItemChangeType m_ChangeType;
    private final int m_ShoppingListId;
    private final int m_ItemId;

    //endregion


    //region Constructor

    public ItemChangedEvent(ItemChangeType changeType, int shoppingListId, int itemId) {

        this.m_ChangeType = changeType;
        this.m_ShoppingListId = shoppingListId;
        this.m_ItemId = itemId;
    }

    //endregion


    //region Getters

    public ItemChangeType getChangeType() {
        return m_ChangeType;
    }

    public int getShoppingListId() {
        return m_ShoppingListId;
    }


    public int getItemId() {
        return m_ItemId;
    }

    //endregion

}
