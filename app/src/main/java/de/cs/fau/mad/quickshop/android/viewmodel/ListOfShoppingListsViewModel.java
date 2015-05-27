package de.cs.fau.mad.quickshop.android.viewmodel;

import java.util.List;

import javax.inject.Inject;

import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.ListStorage;
import de.cs.fau.mad.quickshop.android.model.messages.ShoppingListChangedEvent;
import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ObservableArrayList;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ViewLauncher;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ViewModelBase;
import de.greenrobot.event.EventBus;

public class ListOfShoppingListsViewModel extends ViewModelBase {

    // listener interface
    public interface Listener extends ViewModelBase.Listener {

        void onShoppingListsChanged(final List<ShoppingList> newValue);
    }

    // infrastructure references
    private final ViewLauncher viewLauchner;
    private final ListStorage listStorage;

    private Listener listener;

    // backing fields for properties
    private ObservableArrayList<ShoppingList> shoppingLists;
    private final Command addShoppingListCommand = new Command<Object>() {
        @Override
        public void execute(Object parameter) {
            viewLauchner.showAddShoppingListView();
        }
    };
    private final Command<Integer> selectShoppingListCommand = new Command<Integer>() {
        @Override
        public void execute(Integer shoppingListId) {
            viewLauchner.showShoppingList(shoppingListId);
        }
    };
    private final Command selectShoppingListDetailsCommand = new Command<Integer>() {
        @Override
        public void execute(Integer shoppingListId) {
            viewLauchner.showShoppingListDetailsView(shoppingListId);
        }
    };

    @Inject
    public ListOfShoppingListsViewModel(ViewLauncher viewLauncher, ListStorage listStorage) {

        this.viewLauchner = viewLauncher;
        this.listStorage = listStorage;

        setShoppingLists(new ObservableArrayList<ShoppingList>(listStorage.getAllLists()));

        EventBus.getDefault().register(this);
    }


    public void setListener(final Listener listener) {
        this.listener = listener;
    }


    // Getters / Setters

    public ObservableArrayList<ShoppingList> getShoppingLists() {
        return this.shoppingLists;
    }

    private void setShoppingLists(final ObservableArrayList<ShoppingList> value) {
        if (value != shoppingLists) {
            this.shoppingLists = value;
            if (listener != null) {
                listener.onShoppingListsChanged(value);
            }
        }
    }

    public Command getAddShoppingListCommand() {
        return this.addShoppingListCommand;
    }

    public Command<Integer> getSelectShoppingListCommand() {
        return this.selectShoppingListCommand;
    }

    public Command<Integer> getSelectShoppingListDetailsCommand() {
        return this.selectShoppingListDetailsCommand;
    }

    @Override
    protected Listener getListener() {
        return listener;
    }

    public void onEvent(ShoppingListChangedEvent ev) {

        //TODO: only update list entries that were changed, instead of recreating the entire list
        setShoppingLists(new ObservableArrayList<>(listStorage.getAllLists()));
    }

    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
    }
}
