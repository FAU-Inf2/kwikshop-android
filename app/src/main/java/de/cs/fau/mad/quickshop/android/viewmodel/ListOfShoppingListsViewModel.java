package de.cs.fau.mad.quickshop.android.viewmodel;

import java.util.List;

import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ObservableList;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ViewManagerInterface;

public class ListOfShoppingListsViewModel extends ShoppingListViewModelBase {

    // listener interface
    public interface Listener extends ShoppingListViewModelBase.Listener {

        void onShoppingListsChanged(final List<ShoppingList> newValue);
    }

    // infrastructure references
    private ViewManagerInterface viewManager;
    private Listener listener;

    // backing fields for properties
    private ObservableList<ShoppingList> shoppingLists;
    private final Command addShoppingListCommand = new Command() {
        @Override
        public void execute() {
            viewManager.showView(new ShoppingListDetailsViewModel());
        }
    };


    public void setListener(final Listener listener) {
        this.listener = listener;
    }


    // Getters / Setters

    public ObservableList<ShoppingList> getShoppingLists() {
        return this.shoppingLists;
    }

    private void setShoppingLists(final ObservableList<ShoppingList> value) {
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
    protected ShoppingListViewModelBase.Listener getListener() {
        return listener;
    }

}
