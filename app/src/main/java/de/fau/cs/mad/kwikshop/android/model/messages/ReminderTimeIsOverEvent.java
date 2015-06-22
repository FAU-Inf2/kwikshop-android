package de.fau.cs.mad.kwikshop.android.model.messages;

import de.fau.cs.mad.kwikshop.android.common.Item;

public class ReminderTimeIsOverEvent {

    private int itemId;

    public ReminderTimeIsOverEvent(int itemId) {
        this.itemId = itemId;
    }

    public int getItemId() {
        return itemId;
    }
}
