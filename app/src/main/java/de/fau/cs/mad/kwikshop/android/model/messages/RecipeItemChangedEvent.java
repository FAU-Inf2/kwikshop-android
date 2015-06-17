package de.fau.cs.mad.kwikshop.android.model.messages;

public class RecipeItemChangedEvent {

    //region Fields
    private final int m_recipeId;
    private final int m_ItemId;
    private final ItemChangeType m_ChangeType;


    //endregion


    //region Constructor

    public RecipeItemChangedEvent(ItemChangeType changeType, int recipeId, int itemId) {

        this.m_ChangeType = changeType;
        this.m_recipeId = recipeId;
        this.m_ItemId = itemId;
    }

    //endregion


    //region Getters

    public ItemChangeType getChangeType() {
        return m_ChangeType;
    }

    public int getRecipeId() {
        return m_recipeId;
    }


    public int getItemId() {
        return m_ItemId;
    }

    //endregion


}
