package de.fau.cs.mad.kwikshop.android.common;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import de.fau.cs.mad.kwikshop.common.Group;

@DatabaseTable(tableName = "autoCompletionData")
public class AutoCompletionData {

    /**
     * unique id generated by storage
     */
    @DatabaseField(generatedId = true)
    private int id;

    /**
     * the item name that was entered by a user
     */
    @DatabaseField(canBeNull = false)
    private String name;

    /**
     * the group that was set the last time for that item
     */
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Group group;


    public AutoCompletionData() {
        // Default no-arg constructor for generating Groups, required for ORMLite
    }

    public AutoCompletionData(String name) {
        this.name = name;
    }

    public AutoCompletionData(String name, Group group) {
        this.name = name;
        this.group = group;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Comparison, if another object is the same as this object.
     * Two AutoCompletionDatas are considered the same, if the name is identical
     */
    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (obj.getClass() != this.getClass())
            return false;
        AutoCompletionData autoCompletionData = (AutoCompletionData)obj;
        return this.name.equals(autoCompletionData.getName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
