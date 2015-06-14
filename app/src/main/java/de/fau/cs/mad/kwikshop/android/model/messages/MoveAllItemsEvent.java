package de.fau.cs.mad.kwikshop.android.model.messages;

public class MoveAllItemsEvent {
    private boolean moveAllToBought;
    private boolean moveAllFromBought;

    public static MoveAllItemsEvent moveAllToBoughtEvent = new MoveAllItemsEvent(true);
    public static MoveAllItemsEvent moveAllFromBoughtEvent = new MoveAllItemsEvent(false);

    private MoveAllItemsEvent (boolean moveAllToBought) {
        this.moveAllToBought = moveAllToBought;
        this.moveAllFromBought = !moveAllToBought;
    }

    public boolean isMoveAllFromBought() {
        return moveAllFromBought;
    }

    public boolean isMoveAllToBought() {
        return moveAllToBought;
    }
}
