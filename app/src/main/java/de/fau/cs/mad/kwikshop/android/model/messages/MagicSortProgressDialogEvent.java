package de.fau.cs.mad.kwikshop.android.model.messages;

public class MagicSortProgressDialogEvent {
    private final MagicSortProgressDialogType type;

    public MagicSortProgressDialogEvent(MagicSortProgressDialogType type) {
        this.type = type;
    }

    public MagicSortProgressDialogType getType() {
        return type;
    }
}
