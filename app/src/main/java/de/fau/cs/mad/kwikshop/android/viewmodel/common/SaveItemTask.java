package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import android.os.AsyncTask;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.greenrobot.event.EventBus;

/**
 * Task for saving one or more items into shopping list
 */
public class SaveItemTask extends AsyncTask<Item, Void, Void> {

    private final ListStorage listStorage;
    private final int shoppingListId;


    public SaveItemTask(ListStorage listStorage, int shoppingListId) {

        if (listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }


        this.listStorage = listStorage;
        this.shoppingListId = shoppingListId;
    }


    @Override
    protected Void doInBackground(Item[] items) {

        if(items.length > 0) {
            ShoppingList list = listStorage.loadList(shoppingListId);
            if(list != null) {

                for(Item i : items) {
                    if(list.removeItem(i)) {
                        list.addItem(i);
                        EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.PropertiesModified, shoppingListId, i.getId()));
                    } else {
                        list.addItem(i);
                        EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.ItemsAdded, shoppingListId));
                        EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.Added, shoppingListId, i.getId()));
                    }
                }
            }
        }

        return null;
    }
}
