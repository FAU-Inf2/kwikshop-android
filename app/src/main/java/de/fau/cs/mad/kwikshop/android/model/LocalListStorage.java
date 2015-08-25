package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;

public class LocalListStorage implements ListStorage<ShoppingList> {

    @Override
    public int createList() {
        ShoppingList newList = new ShoppingList();
        try {
            ListStorageFragment.getDatabaseHelper().getShoppingListDao().create(newList);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return newList.getId();
    }

    @Override
    public List<ShoppingList> getAllLists() {

        DatabaseHelper databaseHelper = ListStorageFragment.getDatabaseHelper();

        try {
            ArrayList<ShoppingList> lists = new ArrayList<>();
            for(ShoppingList list : databaseHelper.getShoppingListDao()) {
                lists.add(list);

                if(list.getCalendarEventDate() != null) {
                    databaseHelper.getCalendarDao().refresh(list.getCalendarEventDate());
                }

                databaseHelper.refreshItemsRecursively(list.getItems());
            }
            return Collections.unmodifiableList(lists);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public ShoppingList loadList(int listID) {

        DatabaseHelper databaseHelper = ListStorageFragment.getDatabaseHelper();

        ShoppingList loadedList;
        try {
            loadedList = databaseHelper.getShoppingListDao().queryForId(listID);

            if(loadedList.getCalendarEventDate() != null) {
                databaseHelper.getCalendarDao().refresh(loadedList.getCalendarEventDate());
            }

            databaseHelper.refreshItemsRecursively(loadedList.getItems());

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return loadedList;
    }

    @Override
    public boolean saveList(ShoppingList list) {
        try {
            // Update all Items first
            for (Item item : list.getItems()) {
                updateItem(item);
            }
            ListStorageFragment.getDatabaseHelper().getShoppingListDao().update(list);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void updateItem(Item item) throws SQLException {

        if(item.getLocation() != null) {
            ListStorageFragment.getDatabaseHelper().getLocationDao().createIfNotExists(item.getLocation());
        }
        ListStorageFragment.getDatabaseHelper().getItemDao().update(item);
    }

    @Override
    public boolean deleteList(int id) {
        try {
            deleteListItems(id);
            ListStorageFragment.getDatabaseHelper().getShoppingListDao().deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void deleteListItems(int listId) {
        DeleteBuilder db;
        try {
            db = ListStorageFragment.getDatabaseHelper().getItemDao().deleteBuilder();
            db.where().eq(Item.FOREIGN_SHOPPINGLIST_FIELD_NAME, listId); // Delete all items that belong to this list
            ListStorageFragment.getDatabaseHelper().getItemDao().delete(db.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
