package de.cs.fau.mad.quickshop.android.model.messages;

public class ShoppingListChangedEvent {


    //region Fields

    private final int m_ListId;
    private final ShoppingListChangeType m_ChangeType;

    //endregion


    //region Constructor

    public ShoppingListChangedEvent(int listId, ShoppingListChangeType changeType) {

        this.m_ListId = listId;
        this.m_ChangeType = changeType;

    }

    //endregion


    //region Getters

    public ShoppingListChangeType getChangeType() {
        return this.m_ChangeType;
    }

    public int getListId() {
        return m_ListId;
    }

    //endregion

}
