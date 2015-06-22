package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.Item;
//import de.fau.cs.mad.kwikshop.android.common.ItemRepeatData;

public class RegularlyRepeatHelper {

    //private SimpleStorage<Item> repeatStorage;
    private LinkedList<Item> repeatList;

    private static volatile RegularlyRepeatHelper instance = null; //singleton

    private RegularlyRepeatHelper(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        try {
            //create local autocompletion storage
            //repeatStorage = new SimpleStorage<>(databaseHelper.getItemRepeatDao());
            List<Item> items = databaseHelper.getItemDao().queryForAll();
            repeatList = new LinkedList<>();
            for (Item item : items) {
                if(item.isRegularlyRepeatItem() && item.getRemindAtDate() != null) {
                    repeatList.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*private void initializeArrayList() {
        repeatArrayList = new ArrayList<>(repeatStorage.getItems());
    }*/

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
        }
    }

    public List<Item> getAll() {
        return new ArrayList<>(repeatList);
    }

    public void delete(Item data) {
        if(!repeatList.contains(data)){
            return;
        }
        repeatList.remove(data);
    }

}
