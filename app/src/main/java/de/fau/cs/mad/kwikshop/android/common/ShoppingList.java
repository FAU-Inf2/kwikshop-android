package de.fau.cs.mad.kwikshop.android.common;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import de.fau.cs.mad.kwikshop.android.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.android.model.LocalListStorage;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;

@DatabaseTable(tableName = "shoppingList")
public class ShoppingList implements DomainListObject {

    /**
     * unique id generated by storage
     */
    @DatabaseField(generatedId = true)
    private int id;

    /**
     * not unique, can be set by user
     */
    @DatabaseField
    private String name;

    @DatabaseField
    private int sortTypeInt;

    /**
     * Date of an Event for Calendar Usage
     */
    @DatabaseField(foreign = true, canBeNull = true)
    private CalendarEventDate eventDate = new CalendarEventDate();

    /**
     * type: Account.id
     */
    @ForeignCollectionField
    private ForeignCollection<AccountID> sharedWith;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Item> items; // TODO: is Collection fine for everyone?

    private Account owner;

    private enum visibility {
        PRIVATE
    }

    @DatabaseField(unknownEnumName = "PRIVATE")
    private visibility vis;


    public ShoppingList(int id) {
        this.id = id;
    }

    public ShoppingList() {
        // Default no-arg constructor for generating Items, required for ORMLite
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSortTypeInt(){
        return this.sortTypeInt;
    }

    public void setSortTypeInt(int sortTypeInt){
        this.sortTypeInt = sortTypeInt;
    }

    public CalendarEventDate getCalendarEventDate() {
        return eventDate;
    }

    public void setCalendarEventDate(CalendarEventDate eventDate) {
        this.eventDate = eventDate;
    }

    public Collection getSharedWith() {
        return sharedWith;
    }

    public visibility getVisibility() {
        return vis;
    }

    public void setVisibility(visibility visibility) {
        this.vis = visibility;
    }



    public void addItem(Item item) {
        this.items.add(item);
    }

    public boolean removeItem(int id) {
        Iterator<Item> iterator = items.iterator(); // TODO: again: I hope a normal Iterator instead of a ListIterator is fine for everyone

        while (iterator.hasNext()) {
            Item currentItem = iterator.next();
            if (currentItem.getId() == id) {
                items.remove(currentItem);
                return true;
            }
        }
        return false;
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
