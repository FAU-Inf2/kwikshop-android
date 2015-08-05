package de.fau.cs.mad.kwikshop.android.model.messages;

public class DeleteItemEvent {

    private int listId;
    private int itemId;

    public DeleteItemEvent(int listId, int itemId){
        this.listId = listId;
        this.itemId = itemId;
    }

    public int getListId(){ return listId; }

    public int getItemId(){ return itemId; }
}
