package de.fau.cs.mad.kwikshop.android.viewmodel.tasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListLoadedEvent;
import de.greenrobot.event.EventBus;

public class LoadShoppingListTask extends AsyncTask<Integer, Object, Collection<ShoppingList>> {

    private ListStorage<ShoppingList> listStorage;
    private EventBus resultBus;


    public LoadShoppingListTask(ListStorage<ShoppingList> listStorage, EventBus resultBus) {

        if (listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        if (resultBus == null) {
            throw new IllegalArgumentException("'resultBus' must not be null");
        }

        this.listStorage = listStorage;
        this.resultBus = resultBus;
    }


    @Override
    protected Collection<ShoppingList> doInBackground(Integer... params) {

        //load all lists
        if (params.length == 0) {

            List<ShoppingList> lists = listStorage.getAllLists();
            for (ShoppingList list : lists) {
                resultBus.post(new ShoppingListLoadedEvent(list));
            }
            return lists;

        //load specified lists
        } else {

            List<ShoppingList> result = new ArrayList<>();
            for (int listId : params) {

                ShoppingList list = listStorage.loadList(listId);
                resultBus.post(new ShoppingListLoadedEvent(list));
                result.add(list);
            }
            return result;
        }
    }

}
