package de.fau.cs.mad.kwikshop.android.model.tasks;

import android.os.AsyncTask;

import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;

public class SaveShoppingListTask extends AsyncTask<ShoppingList, Void, Void> {


    private final ListStorage<ShoppingList> listStorage;

    public SaveShoppingListTask(ListStorage<ShoppingList> listStorage) {

        if(listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        this.listStorage = listStorage;
    }


    @Override
    protected Void doInBackground(ShoppingList... params) {

        for(ShoppingList list : params) {
            listStorage.saveList(list);
        }

        return null;
    }
}
