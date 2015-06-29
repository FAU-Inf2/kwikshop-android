package de.fau.cs.mad.kwikshop.android.model.messages;

public class RecipeChangedEvent {


    //region Fields

    private final int m_ListId;
    private final ListChangeType m_ChangeType;

    //endregion


    //region Constructor

    public RecipeChangedEvent(ListChangeType changeType, int listId) {

        this.m_ListId = listId;
        this.m_ChangeType = changeType;

    }

    //endregion


    //region Getters

    public ListChangeType getChangeType() {
        return this.m_ChangeType;
    }

    public int getListId() {
        return m_ListId;
    }

    //endregion

}
