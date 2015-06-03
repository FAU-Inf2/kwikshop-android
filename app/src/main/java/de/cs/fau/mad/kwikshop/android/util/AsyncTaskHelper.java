package de.cs.fau.mad.kwikshop.android.util;

import android.os.AsyncTask;

import de.cs.fau.mad.kwikshop.android.common.ShoppingList;

public class AsyncTaskHelper {

    public static class ShoppingListSaveTask extends AsyncTask<ShoppingList, Void, Void> {
        @Override
        protected Void doInBackground(ShoppingList... params) {
            params[0].save();
            return null;
        }
    }
}
