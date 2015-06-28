package de.fau.cs.mad.kwikshop.android.model.tasks;

import android.os.AsyncTask;

import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.ListStorage;

public class SaveShoppingListTask extends AsyncTask<ShoppingList, Void, Void> {


    private final ListStorage listStorage;

    public SaveShoppingListTask(ListStorage listStorage) {

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
