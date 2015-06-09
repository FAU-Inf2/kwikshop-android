package de.cs.fau.mad.kwikshop.android.view;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

import de.cs.fau.mad.kwikshop.android.common.ShoppingList;
import de.cs.fau.mad.kwikshop.android.model.ListStorage;
import de.cs.fau.mad.kwikshop.android.model.ListStorageFragment;
import de.greenrobot.event.EventBus;

public class LoadShoppingListTask extends AsyncTask<Object, Object, ShoppingList> {

    private FragmentActivity context;
    private EventBus resultBus;
    private int shoppingListId;

    public LoadShoppingListTask(FragmentActivity fragmentActivity, EventBus resultBus, int shoppingListId) {

        if (fragmentActivity == null) {
            throw new IllegalArgumentException("'fragmentActivity' must not be null");
        }

        if (resultBus == null) {
            throw new IllegalArgumentException("'resultBus' must not be null");
        }

        this.context = fragmentActivity;
        this.resultBus = resultBus;
        this.shoppingListId = shoppingListId;
    }


    @Override
    protected ShoppingList doInBackground(Object... params) {

        new ListStorageFragment().SetupLocalListStorageFragment(context);

        ListStorage listStorage = ListStorageFragment.getLocalListStorage();
        ShoppingList list = listStorage.loadList(shoppingListId);

        resultBus.post(list);

        return list;
    }

}
