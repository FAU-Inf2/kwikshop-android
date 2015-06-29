package de.fau.cs.mad.kwikshop.android.viewmodel.tasks;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemLoadedEvent;
import de.greenrobot.event.EventBus;

public class LoadItemTask extends AsyncTask<Integer, Object, Collection<Item>> {


    private ListStorage<ShoppingList> listStorage;
    private EventBus resultBus;
    private int shoppingListId;


    public LoadItemTask(ListStorage<ShoppingList> listStorage, EventBus resultBus, int shoppingListId) {

        if (listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        if (resultBus == null) {
            throw new IllegalArgumentException("'resultBus' must not be null");
        }

        this.listStorage = listStorage;
        this.resultBus = resultBus;

        this.shoppingListId = shoppingListId;

    }


    @Override
    protected Collection<Item> doInBackground(Integer... itemIds) {


        ShoppingList list = listStorage.loadList(shoppingListId);

        //load all the items from the list
        if (itemIds.length == 0) {
            return list.getItems();

        //load only specified items
        } else {

            List<Item> result = new ArrayList<>();

            for (int id : itemIds) {

                //TODO: reimplement this if we can load single items directly from the database
                Item item = list.getItem(id);
                if (item != null) {
                    result.add(item);
                }
            }

            if (result.size() > 0) {
                resultBus.post(new ItemLoadedEvent(shoppingListId, result.toArray(new Item[result.size()])));
            }

            return result;
        }
    }
}
