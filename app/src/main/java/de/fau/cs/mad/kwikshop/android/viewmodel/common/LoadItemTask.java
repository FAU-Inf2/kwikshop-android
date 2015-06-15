package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import android.os.AsyncTask;

import java.util.Collection;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemLoadedEvent;
import de.greenrobot.event.EventBus;

public class LoadItemTask extends AsyncTask<Object, Object, Item> {


    private ListStorage listStorage;
    private EventBus resultBus;
    private int shoppingListId;
    private int itemId;


    public LoadItemTask(ListStorage listStorage, EventBus resultBus, int shoppingListId, int itemId) {

        if (listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        if (resultBus == null) {
            throw new IllegalArgumentException("'resultBus' must not be null");
        }

        this.listStorage = listStorage;
        this.resultBus = resultBus;

        this.shoppingListId = shoppingListId;
        this.itemId = itemId;

    }


    @Override
    protected Item doInBackground(Object... params) {

        //TODO: reimplement this if we can load single items directly from the database
        ShoppingList list = listStorage.loadList(shoppingListId);
        Item item = list.getItem(itemId);
        if(item != null) {
            resultBus.post(new ItemLoadedEvent(shoppingListId, item));
        }

        return null;
    }
}
