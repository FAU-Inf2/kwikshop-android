package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;

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
        try {
            ArrayList<ShoppingList> lists = new ArrayList<>();
            for(ShoppingList list : ListStorageFragment.getDatabaseHelper().getShoppingListDao()) {
                lists.add(list);

                if(list.getCalendarEventDate() != null) {
                    ListStorageFragment.getDatabaseHelper().getCalendarDao().refresh(list.getCalendarEventDate());
                }

                for (Item i : list.getItems()) {

                    if (i.getUnit() != null) {
                        ListStorageFragment.getDatabaseHelper().getUnitDao().refresh(i.getUnit());
                    }

                    if (i.getGroup() != null) {
                        ListStorageFragment.getDatabaseHelper().getGroupDao().refresh(i.getGroup());
                    }
                }
            }
            return Collections.unmodifiableList(lists);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public ShoppingList loadList(int listID) {
        ShoppingList loadedList;
        try {
            loadedList = ListStorageFragment.getDatabaseHelper().getShoppingListDao().queryForId(listID);

            if(loadedList.getCalendarEventDate() != null) {
                ListStorageFragment.getDatabaseHelper().getCalendarDao().refresh(loadedList.getCalendarEventDate());
            }

            for (Item i : loadedList.getItems()) {

                if (i.getUnit() != null) {
                    ListStorageFragment.getDatabaseHelper().getUnitDao().refresh(i.getUnit());
                }

                if (i.getGroup() != null) {
                    ListStorageFragment.getDatabaseHelper().getGroupDao().refresh(i.getGroup());
                }
            }

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
                ListStorageFragment.getDatabaseHelper().getItemDao().update(item);
            }
            ListStorageFragment.getDatabaseHelper().getShoppingListDao().update(list);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
