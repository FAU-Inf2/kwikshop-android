package de.fau.cs.mad.kwikshop.android.model;


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

    private PriorityQueue<Item> scheduleRepeatList;
    private final DatabaseHelper databaseHelper;


    public RegularlyRepeatHelper(DatabaseHelper databaseHelper) {

        if (databaseHelper== null) {
            throw new ArgumentNullException("databaseHelper");
        }
        this.databaseHelper = databaseHelper;
        EventBus.getDefault().register(this);

        loadFromDatabase();
    }

    private void loadFromDatabase() {
        try {
            List<Item> items = databaseHelper.getItemDao().queryForAll();
            scheduleRepeatList = new PriorityQueue<>(Math.max(1, items.size()), new Comparator<Item>() {
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
                if(item.getRepeatType() == RepeatType.Schedule) {
                    scheduleRepeatList.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void offerRepeatData (Item item) {
        if (!(scheduleRepeatList.contains(item))){
            scheduleRepeatList.add(item);
        } else {
            // delete the item and add it again, in order to re-sort the list
            scheduleRepeatList.remove(item);
            scheduleRepeatList.add(item);
        }
    }

    public List<Item> getAll() {
        return new ArrayList<>(scheduleRepeatList);
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
        for (Item item : scheduleRepeatList) {
            if (item.getId() == id)
                return item;
        }
        return null;
    }

    public void delete(Item data) {
        if(!scheduleRepeatList.contains(data)){
            return;
        }
        scheduleRepeatList.remove(data);
    }

    public void checkIfReminderIsOver() {

        Calendar now = Calendar.getInstance();
        Item item = scheduleRepeatList.peek();
        if (item == null || item.getRemindAtDate() == null)
            return;

        if (now.getTime().after(item.getRemindAtDate())) {
            EventBus.getDefault().post(new ReminderTimeIsOverEvent(item.getShoppingList().getId(), item.getId()));
        }


    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(ShoppingListChangedEvent event) {
        if (event.getChangeType() == ListChangeType.Deleted) {
            if (scheduleRepeatList.size() > 0) {
                // it is probably faster to reload every time a shopping list is deleted than checking all items if they are in the deleted shopping list
                loadFromDatabase();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(ItemChangedEvent event) {
        if (event.getChangeType() == ItemChangeType.Deleted) {
            Item item = getItem(event.getItemId());
            if (item != null)
                scheduleRepeatList.remove(item);
        }
    }
}
