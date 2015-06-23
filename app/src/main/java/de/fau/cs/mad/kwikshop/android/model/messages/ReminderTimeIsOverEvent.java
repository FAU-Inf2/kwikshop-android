package de.fau.cs.mad.kwikshop.android.model.messages;

import de.fau.cs.mad.kwikshop.android.common.Item;

public class ReminderTimeIsOverEvent {

    private int itemId;
    private int listId;

    public ReminderTimeIsOverEvent(int listId, int itemId) {
        this.listId = listId;
        this.itemId = itemId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getListId() {
        return listId;
    }
}
