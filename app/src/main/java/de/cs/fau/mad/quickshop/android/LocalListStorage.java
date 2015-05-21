package de.cs.fau.mad.quickshop.android;

import android.support.v4.app.FragmentManager;

import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.Vector;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;

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
    public Vector getAllLists() {
        Vector<ShoppingList> lists = new Vector<>();
        try {
            for(ShoppingList list : ListStorageFragment.getDatabaseHelper().getShoppingListDao()) {
                lists.add(list);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return lists;
    }

    @Override
    public ShoppingList loadList(Integer listID) {
        ShoppingList loadedList = null;
        try {
            loadedList = ListStorageFragment.getDatabaseHelper().getShoppingListDao().queryForId(listID);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return loadedList;
    }

    @Override
    public Boolean saveList(ShoppingList list) {
        try {
            //deleteList(list.getId());
            ListStorageFragment.getDatabaseHelper().getShoppingListDao().update(list);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
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
        DeleteBuilder db = null;
        try {
            db = ListStorageFragment.getDatabaseHelper().getItemDao().deleteBuilder();
            db.where().eq(Item.FOREIGN_SHOPPINGLIST_FIELD_NAME, listid); // Delete all items that belong to this list
            ListStorageFragment.getDatabaseHelper().getItemDao().delete(db.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
