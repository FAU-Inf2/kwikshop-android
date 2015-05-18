package de.cs.fau.mad.quickshop.android.messages;

public class ShoppingListChangedEvent {

    final int m_ListId;
    final ChangeType m_ChangeType;


    public ShoppingListChangedEvent(int listId, ChangeType changeType) {

        this.m_ListId = listId;
        this.m_ChangeType = changeType;

    }


    public int getListId() {
        return m_ListId;
    }

    public ChangeType getChangeType() {
        return this.m_ChangeType;
    }

}
