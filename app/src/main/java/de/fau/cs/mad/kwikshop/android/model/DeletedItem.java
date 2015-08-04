package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.fau.cs.mad.kwikshop.android.model.messages.ListType;

@DatabaseTable(tableName = "deletedItem")
public class DeletedItem extends DeletedList {

    @DatabaseField
    private int itemId;

    @DatabaseField
    private int itemIdServer;


    public DeletedItem() {

    }

    public DeletedItem(ListType listType, int listId, int listIdServer, int itemId, int itemIdServer, int serverVersion) {
        super(listType, listId, listIdServer, serverVersion);

        this.itemId = itemId;
        this.itemIdServer = itemIdServer;
    }



    public int getItemId() {
        return this.itemId;
    }

    public int getItemIdServer() {
        return this.itemIdServer;
    }
}
