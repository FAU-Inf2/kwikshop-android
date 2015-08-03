package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.fau.cs.mad.kwikshop.android.model.messages.ListType;

@DatabaseTable(tableName = "deletedItem")
public class DeletedItem extends DeletedList {

    @DatabaseField
    private int itemId;



    public DeletedItem() {

    }

    public DeletedItem(ListType listType, int listId, int itemId, int serverVersion) {
        super(listType, listId, serverVersion);

        this.itemId = itemId;
    }



    public int getItemId() {
        return this.itemId;
    }

}
