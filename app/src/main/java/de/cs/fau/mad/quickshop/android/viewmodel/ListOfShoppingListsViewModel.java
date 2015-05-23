package de.cs.fau.mad.quickshop.android.viewmodel;

import java.util.List;

import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.ListStorage;
import de.cs.fau.mad.quickshop.android.model.messages.ShoppingListChangedEvent;
import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ObservableArrayList;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ViewManagerInterface;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ViewModelBase;
import de.greenrobot.event.EventBus;

public class ListOfShoppingListsViewModel extends ViewModelBase {

    // listener interface
    public interface Listener extends ViewModelBase.Listener {

        void onShoppingListsChanged(final List<ShoppingList> newValue);
    }

    // infrastructure references
    private ViewManagerInterface viewManager;
    private ListStorage listStorage = null; //TODO: initialize
    private Listener listener;

    // backing fields for properties
    private ObservableArrayList<ShoppingList> shoppingLists;
    private final Command addShoppingListCommand = new Command() {
        @Override
        public void execute() {
            viewManager.showView(new ShoppingListDetailsViewModel());
        }
    };


    public ListOfShoppingListsViewModel() {

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
