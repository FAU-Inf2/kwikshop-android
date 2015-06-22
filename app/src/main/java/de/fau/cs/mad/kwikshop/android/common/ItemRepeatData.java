package de.fau.cs.mad.kwikshop.android.common;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "itemRepeatData")
public class ItemRepeatData {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true, foreignAutoCreate = true, columnName = "item", maxForeignAutoRefreshLevel = 2)
    private Item item;

    @DatabaseField(canBeNull = true)
    private Date remindAtDate;


    public ItemRepeatData() {
        // Default no-arg constructor, required for ORMLite
    }

    public ItemRepeatData(Item item, Date remindAtDate){
        if (item == null)
            throw new IllegalArgumentException("item must not be null");
        if (remindAtDate == null)
            throw new IllegalArgumentException("remindAtDate must not be null");
        this.item = item;
        this.remindAtDate = remindAtDate;
    }

    public int getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public Date getRemindAtDate() {
        return remindAtDate;
    }

    public void setRemindAtDate(Date remindAtDate) {
        this.remindAtDate = remindAtDate;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (obj.getClass() != this.getClass())
            return false;
        ItemRepeatData itemRepeatData = (ItemRepeatData)obj;
        return this.id == itemRepeatData.id;
    }

    public int hashCode() {
        return this.id;
    }
}

