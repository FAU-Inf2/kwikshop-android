package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ReminderTimeIsOverEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.fau.cs.mad.kwikshop.common.RepeatType;
import de.greenrobot.event.EventBus;

public class RegularlyRepeatHelper {

    private PriorityQueue<Item> repeatList;
    private DatabaseHelper databaseHelper;

    private static volatile RegularlyRepeatHelper instance = null; //singleton

    private RegularlyRepeatHelper(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }
        databaseHelper = new DatabaseHelper(context);

        EventBus.getDefault().register(this);

        loadFromDatabase();
    }

    private void loadFromDatabase() {
        try {
            List<Item> items = databaseHelper.getItemDao().queryForAll();
            repeatList = new PriorityQueue<>(items.size(), new Comparator<Item>() {
                @Override
                public int compare(Item lhs, Item rhs) {
                    if (lhs != null && lhs.getRemindAtDate() != null && rhs != null && rhs.getRemindAtDate() != null)
                        return lhs.getRemindAtDate().compareTo(rhs.getRemindAtDate());
                    if (lhs != null && lhs.getRemindAtDate() != null)
                        return -1;
                    if (rhs != null && rhs.getRemindAtDate() != null)
                        return 1;
                    return 0;
                }
            });
            for (Item item : items) {
                if(item.getRepeatType() == RepeatType.Schedule/* && item.getRemindAtDate() != null*/) {
                    repeatList.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static RegularlyRepeatHelper getRegularlyRepeatHelper(Context context) {
        if (instance == null) {
            synchronized (RegularlyRepeatHelper.class) { // double checked looking
                if (instance == null) {
                    instance = new RegularlyRepeatHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * Gets the instance of RegularlyRepeatHelper, if it was already created (via getRegularlyRepeatHelper)
     * @return the instance of RegularlyRepeatHelper, or null if it doesn't exist
     */
    public static RegularlyRepeatHelper getInstance() {
        return instance;
    }

    public void offerRepeatData (Item item) {
        if (!(repeatList.contains(item))){
            repeatList.add(item);
        } else {
            // delete the item and add it again, in order to re-sort the list
            repeatList.remove(item);
            repeatList.add(item);
        }
    }

    public List<Item> getAll() {
        return new ArrayList<>(repeatList);
    }

    public Item getItemForId(int id) {
        Item item = getItem(id);
        if (item != null) return item;

        // item was not found
        // -> reload from db and retry
        loadFromDatabase();
        item = getItem(id);
        return item;
    }

    private Item getItem(int id) {
        for (Item item : repeatList) {
            if (item.getId() == id)
                return item;
        }
        return null;
    }

    public void delete(Item data) {
        if(!repeatList.contains(data)){
            return;
        }
        repeatList.remove(data);
    }

    public void checkIfReminderIsOver() {

        Calendar now = Calendar.getInstance();
        Item item = repeatList.peek();
        if (item == null || item.getRemindAtDate() == null)
            return;

        if (now.getTime().after(item.getRemindAtDate())) {
            EventBus.getDefault().post(new ReminderTimeIsOverEvent(item.getShoppingList().getId(), item.getId()));
        }


    }

    public void onEventBackgroundThread(ShoppingListChangedEvent event) {
        if (event.getChangeType() == ListChangeType.Deleted) {
            if (repeatList.size() > 0) {
                // it is probably faster to reload every time a shopping list is deleted than checking all items if they are in the deleted shopping list
                loadFromDatabase();
            }
        }
    }

    public void onEventBackgroundThread(ItemChangedEvent event) {
        if (event.getChangeType() == ItemChangeType.Deleted) {
            Item item = getItem(event.getItemId());
            if (item != null)
                repeatList.remove(item);
        }
    }
}
