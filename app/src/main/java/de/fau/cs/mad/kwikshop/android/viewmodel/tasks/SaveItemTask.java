package de.fau.cs.mad.kwikshop.android.viewmodel.tasks;

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
    private final boolean postToEventBus;

    /**
     * Initializes a new instance of SaveItemTask
     *
     * @param listStorage    The ListStorage instance to use
     * @param shoppingListId The id of the shopping list to save the items to
     */
    public SaveItemTask(ListStorage listStorage, int shoppingListId) {
        this(listStorage, shoppingListId, true);
    }

    /**
     * Initializes a new instance of SaveItemTask
     *
     * @param listStorage    The ListStorage instance to use
     * @param shoppingListId The id of the shopping list to save the items to
     * @param postToEventBus Specifies whether appropriate events should be posted to EventBus
     *                       after the operation is complete (default: true)
     */
    public SaveItemTask(ListStorage listStorage, int shoppingListId, boolean postToEventBus) {

        if (listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }


        this.listStorage = listStorage;
        this.shoppingListId = shoppingListId;
        this.postToEventBus = postToEventBus;
    }


    @Override
    protected Void doInBackground(Item[] items) {

        if (items.length > 0) {
            ShoppingList list = listStorage.loadList(shoppingListId);
            if (list != null) {

                for (Item i : items) {
                    if (list.removeItem(i)) {
                        list.addItem(i);

                        if (postToEventBus) {
                            EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.PropertiesModified, shoppingListId, i.getId()));
                        }
                    } else {
                        list.addItem(i);
                        if (postToEventBus) {
                            EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.ItemsAdded, shoppingListId));
                            EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.Added, shoppingListId, i.getId()));
                        }
                    }
                }
                list.save();
            }
        }

        return null;
    }
}
