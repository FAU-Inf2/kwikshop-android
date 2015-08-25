package de.fau.cs.mad.kwikshop.android.model;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.common.ItemViewModel;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ReminderTimeIsOverEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.greenrobot.event.EventBus;

public class RegularlyRepeatHelper {

    //list of items repeating on a schedule
    private PriorityQueue<ItemViewModel> scheduleRepeatList;
    //list of items to be added to every new shopping list
    private List<ItemViewModel> listCreationRepeatList;


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

            List<ItemViewModel> items = databaseHelper.getItemDao().queryForAll();
            databaseHelper.refreshItemsRecursively(items);

            scheduleRepeatList = new PriorityQueue<>(Math.max(1, items.size()), new RepeatDateItemComparator());
            listCreationRepeatList = new LinkedList<>();


            for (ItemViewModel item : items) {

                switch (item.getRepeatType()) {

                    case Schedule:
                        scheduleRepeatList.add(item);
                        break;

                    case ListCreation:
                        listCreationRepeatList.add(item);
                        break;

                    case None:
                        //nothing to do
                        break;

                    default:
                        throw new UnsupportedOperationException("Unimplemented case in switch statement");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void offerRepeatData(ItemViewModel item) {

        switch (item.getRepeatType()) {

            case None:
                break;

            case Schedule:
                if (!(scheduleRepeatList.contains(item))){
                    scheduleRepeatList.add(item);
                } else {
                    // delete the item and add it again, in order to re-sort the list
                    scheduleRepeatList.remove(item);
                    scheduleRepeatList.add(item);
                }
                break;

            case ListCreation:
                if(!listCreationRepeatList.contains(item)) {
                    listCreationRepeatList.add(item);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unimplemented case in switch-statment");
        }


    }

    public List<ItemViewModel> getAll() {
        return new ArrayList<>(scheduleRepeatList);
    }

    public ItemViewModel getItemForId(int id) {
        ItemViewModel item = getItem(id);
        if (item != null) return item;

        // item was not found
        // -> reload from db and retry
        loadFromDatabase();
        item = getItem(id);
        return item;
    }

    private ItemViewModel getItem(int id) {
        for (ItemViewModel item : scheduleRepeatList) {
            if (item.getId() == id)
                return item;
        }
        return null;
    }

    public void delete(ItemViewModel data) {
        if(!scheduleRepeatList.contains(data)){
            return;
        }
        scheduleRepeatList.remove(data);
    }

    public void checkIfReminderIsOver() {

        Calendar now = Calendar.getInstance();
        ItemViewModel item = scheduleRepeatList.peek();
        if (item == null || item.getRemindAtDate() == null)
            return;

        if (now.getTime().after(item.getRemindAtDate())) {
            EventBus.getDefault().post(new ReminderTimeIsOverEvent(item.getShoppingList().getId(), item.getId()));
        }


    }

    public void addListCreationRepeatingItems(ListManager<ShoppingList> shoppingListManager, int shoppingListId) {

        for(ItemViewModel i : listCreationRepeatList) {
            ItemViewModel newItem = new ItemViewModel(i);
            shoppingListManager.addListItem(shoppingListId, newItem);
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
            ItemViewModel item = getItem(event.getItemId());
            if (item != null)
                scheduleRepeatList.remove(item);
        }
    }



    private static class RepeatDateItemComparator implements Comparator<ItemViewModel> {

        @Override
        public int compare(ItemViewModel lhs, ItemViewModel rhs) {

            if (lhs != null && lhs.getRemindAtDate() != null && rhs != null && rhs.getRemindAtDate() != null)
                return lhs.getRemindAtDate().compareTo(rhs.getRemindAtDate());
            if (lhs != null && lhs.getRemindAtDate() != null)
                return -1;
            if (rhs != null && rhs.getRemindAtDate() != null)
                return 1;
            return 0;
        }
    }
}
