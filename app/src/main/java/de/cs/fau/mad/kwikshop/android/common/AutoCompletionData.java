package de.cs.fau.mad.kwikshop.android.common;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "autoCompletionData")
public class AutoCompletionData {

    /**
     * unique id generated by storage
     */
    @DatabaseField(generatedId = true)
    private int id;

    /**
     * the text that was entered by a user
     */
    @DatabaseField(canBeNull = false)
    private String text;


    public AutoCompletionData() {
        // Default no-arg constructor for generating Groups, required for ORMLite
    }

    public AutoCompletionData(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getText(){
        return text;
    }
}
