package de.fau.cs.mad.kwikshop.android.common;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import de.fau.cs.mad.kwikshop.android.common.interfaces.DomainListObject;

@DatabaseTable (tableName = "recipe")
public class Recipe implements DomainListObject {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String name;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Item> items;

    @DatabaseField
    private int scaleFactor = 1;

    @DatabaseField
    private String scaleName;

    @DatabaseField (canBeNull = true)
    private Date lastModifiedDate;


    public Recipe(int id) {
        this.id = id;
    }

    public Recipe() {
        // Default no-arg constructor for generating Items, required for ORMLite

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getScaleFactor(){ return scaleFactor; }

    public String getScaleName(){
        return scaleName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScaleFactor(int scaleFactor){
        this.scaleFactor = scaleFactor;
    }

    public void setScaleName( String scaleName){
        this.scaleName = scaleName;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate != null ? lastModifiedDate : new Date(0);
    }

    public void setLastModifiedDate(Date value) {
        lastModifiedDate = value;
    }

    public void addItem(Item item) {
        this.items.add(item);
    }

    public boolean removeItem(int id) {
        Iterator<Item> iterator = items.iterator();

        while (iterator.hasNext()) {
            Item currentItem = iterator.next();
            if (currentItem.getId() == id) {
                items.remove(currentItem);
                return true;
            }
        }
        return false;
    }

    public boolean removeItem(Item item) {
        return removeItem(item.getId());
    }

    public int size() {
        return items.size();
    }

    public Collection<Item> getItems() {
        return Collections.unmodifiableCollection(this.items);
    }

    public Item getItem(int id) {
        for (Item item : items) {
            if (item.getId() == id) {
                return item;
            }
        }
        //TODO: returning null is a bad idea
        return null;
    }

}
