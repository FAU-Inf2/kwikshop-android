package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.fau.cs.mad.kwikshop.android.model.messages.ListType;

@DatabaseTable(tableName = "deletedList")
public class DeletedList {

    @DatabaseField
    private ListType listType;

    @DatabaseField
    private int listId;

    @DatabaseField
    private int serverVersion;



    public DeletedList() {

    }

    public DeletedList(ListType listType, int listId, int serverVersion) {

        this.listType = listType;
        this.listId = listId;
        this.serverVersion = serverVersion;
    }



    public ListType getListType() {
        return this.listType;
    }

    public int getListId() {
        return this.listId;
    }

    public int getServerVersion() {
        return this.serverVersion;
    }

}
