package de.fau.cs.mad.kwikshop.android.model.messages;

public class ItemChangedEvent {

    //region Fields

    private final ListType listType;
    private final ItemChangeType changeType;
    private final int listId;
    private final int itemId;

    //endregion


    //region Constructor

    public ItemChangedEvent(ListType listType, ItemChangeType changeType, int listId, int itemId) {

        this.listType = listType;
        this.changeType = changeType;
        this.listId = listId;
        this.itemId = itemId;
    }

    //endregion


    //region Getters

    public ListType getListType() {
        return this.listType;
    }

    public ItemChangeType getChangeType() {
        return changeType;
    }

    public int getListId() {
        return listId;
    }

    public int getItemId() {
        return itemId;
    }

    //endregion

}
