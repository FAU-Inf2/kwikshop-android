package de.cs.fau.mad.kwikshop.android.model;

import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.cs.fau.mad.kwikshop.android.common.Item;
import de.cs.fau.mad.kwikshop.android.common.ShoppingList;

public class LocalListStorage extends ListStorage {

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
            }
            return Collections.unmodifiableList(lists);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public ShoppingList loadList(Integer listID) {
        ShoppingList loadedList;
        try {
            loadedList = ListStorageFragment.getDatabaseHelper().getShoppingListDao().queryForId(listID);

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
    public Boolean saveList(ShoppingList list) {
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
    public Boolean deleteList(Integer id) {
        try {
            deleteListItems(id);
            ListStorageFragment.getDatabaseHelper().getShoppingListDao().deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void deleteItem(int id) {
        try {
            ListStorageFragment.getDatabaseHelper().getItemDao().deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteListItems(Integer listid) {
        DeleteBuilder db;
        try {
            db = ListStorageFragment.getDatabaseHelper().getItemDao().deleteBuilder();
            db.where().eq(Item.FOREIGN_SHOPPINGLIST_FIELD_NAME, listid); // Delete all items that belong to this list
            ListStorageFragment.getDatabaseHelper().getItemDao().delete(db.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
