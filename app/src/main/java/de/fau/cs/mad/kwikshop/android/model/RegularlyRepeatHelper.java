package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.Item;

public class RegularlyRepeatHelper {

    private LinkedList<Item> repeatList;

    private static volatile RegularlyRepeatHelper instance = null; //singleton

    private RegularlyRepeatHelper(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        try {
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