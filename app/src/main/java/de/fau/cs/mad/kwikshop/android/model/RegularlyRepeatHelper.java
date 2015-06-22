package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ItemRepeatData;

public class RegularlyRepeatHelper {

    private SimpleStorage<ItemRepeatData> repeatStorage;
    private ArrayList<ItemRepeatData> repeatArrayList;

    private static volatile RegularlyRepeatHelper instance = null; //singleton

    private RegularlyRepeatHelper(Context context) {
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

    public void offerRepeatData (ItemRepeatData repeatData, Item item) {
        if (repeatData.getItem() == null)
            repeatData.setItem(item);
        if (!(repeatData.getItem().equals(item))) {
            throw new IllegalArgumentException("parameter item and repeatData.getItem() have to be equal objects");
        }
        if (!(repeatArrayList.contains(repeatData))){
            repeatArrayList.add(repeatData);
            repeatStorage.addItem(repeatData);
        } else {
            repeatStorage.updateItem(repeatData);
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
