package de.cs.fau.mad.kwikshop.android.viewmodel.common;

import android.os.AsyncTask;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.cs.fau.mad.kwikshop.android.common.ShoppingList;
import de.cs.fau.mad.kwikshop.android.model.ListStorage;
import de.cs.fau.mad.kwikshop.android.model.messages.ShoppingListLoadedEvent;
import de.greenrobot.event.EventBus;

public class LoadShoppingListTask extends AsyncTask<Object, Object, Collection<ShoppingList>> {

    private ListStorage listStorage;
    private EventBus resultBus;
    private int shoppingListId;
    private final boolean loadAll;

    public LoadShoppingListTask(ListStorage listStorage, EventBus resultBus) {
        this(listStorage, resultBus, -1);
    }

    public LoadShoppingListTask(ListStorage listStorage, EventBus resultBus, int shoppingListId) {

        if (listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        if (resultBus == null) {
            throw new IllegalArgumentException("'resultBus' must not be null");
        }

        this.listStorage = listStorage;
        this.resultBus = resultBus;
        this.shoppingListId = shoppingListId;
        this.loadAll = shoppingListId == -1;
    }


    @Override
    protected Collection<ShoppingList> doInBackground(Object... params) {

        if (loadAll) {

            List<ShoppingList> lists = listStorage.getAllLists();
            for (ShoppingList list : lists) {
                resultBus.post(new ShoppingListLoadedEvent(list));
            }
            return lists;

        } else {

            ShoppingList list = listStorage.loadList(shoppingListId);
            resultBus.post(new ShoppingListLoadedEvent(list));
            return Arrays.asList(list);
        }
    }

}
