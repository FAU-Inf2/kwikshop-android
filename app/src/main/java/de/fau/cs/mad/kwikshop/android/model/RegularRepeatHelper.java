package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.ItemRepeatData;

public class RegularRepeatHelper {

    private SimpleStorage<ItemRepeatData> repeatStorage;
    private ArrayList<ItemRepeatData> repeatArrayList;

    private static volatile RegularRepeatHelper instance = null; //singleton

    private RegularRepeatHelper(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        try {
            //create local autocompletion storage
            repeatStorage = new SimpleStorage<>(databaseHelper.getItemRepeatDao());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        initializeArrayList();
    }

    private void initializeArrayList() {
        repeatArrayList = new ArrayList<>(repeatStorage.getItems());
    }

    public static RegularRepeatHelper getRegularRepeatHelper(Context context) {
        if (instance == null) {
            synchronized (RegularRepeatHelper.class) { // double checked looking
                if (instance == null) {
                    instance = new RegularRepeatHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * Gets the instance of RegularRepeatHelper, if it was already created (via getRegularRepeatHelper)
     * @return the instance of RegularRepeatHelper, or null if it doesn't exist
     */
    public static RegularRepeatHelper getInstance() {
        return instance;
    }

    public void offerRepeatData (ItemRepeatData data) {
        if (!(repeatArrayList.contains(data))){
            repeatArrayList.add(data);
            repeatStorage.addItem(data);
        } else {
            repeatStorage.updateItem(data);
        }
    }

    public List<ItemRepeatData> getAll() {
        return new ArrayList<>(repeatArrayList);
    }

    public void delete(ItemRepeatData data) {
        if(!repeatArrayList.contains(data)){
            return;
        }
        repeatArrayList.remove(data);
        repeatStorage.deleteSingleItem(data);
    }

}
