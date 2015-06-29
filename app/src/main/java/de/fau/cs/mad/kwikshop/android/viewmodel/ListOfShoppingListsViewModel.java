package de.fau.cs.mad.kwikshop.android.viewmodel;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ReminderTimeIsOverEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ShoppingListIdExtractor;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewModelBase;
import de.greenrobot.event.EventBus;

public class ListOfShoppingListsViewModel extends ViewModelBase {

    // listener interface
    public interface Listener extends ViewModelBase.Listener {

        void onShoppingListsChanged(final ObservableArrayList<ShoppingList, Integer> oldValue,
                                    final ObservableArrayList<ShoppingList, Integer> newValue);
    }

    // infrastructure references
    private final ViewLauncher viewLauncher;
    private final ListManager<ShoppingList> shoppingListManager;
    private final EventBus privateBus = EventBus.builder().build();


    private Listener listener;

    // backing fields for properties
    private ObservableArrayList<ShoppingList, Integer> shoppingLists;
    private final Command addShoppingListCommand = new Command<Object>() {
        @Override
        public void execute(Object parameter) {
            viewLauncher.showAddShoppingListView();
        }
    };
    private final Command<Integer> selectShoppingListCommand = new Command<Integer>() {
        @Override
        public void execute(Integer shoppingListId) {
            viewLauncher.showShoppingList(shoppingListId);
        }
    };
    private final Command selectShoppingListDetailsCommand = new Command<Integer>() {
        @Override
        public void execute(Integer shoppingListId) {
            viewLauncher.showShoppingListDetailsView(shoppingListId);
        }
    };


    @Inject
    public ListOfShoppingListsViewModel(ViewLauncher viewLauncher, ListManager<ShoppingList> shoppingListManager) {

        if (viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }

        if (shoppingListManager == null) {
            throw new IllegalArgumentException("'shoppingListManager' must not be null");
        }

        this.viewLauncher = viewLauncher;
        this.shoppingListManager = shoppingListManager;

        setShoppingLists(new ObservableArrayList<>(new ObservableArrayList.IdExtractor<ShoppingList, Integer>() {
            @Override
            public Integer getId(ShoppingList object) {
                return object.getId();
            }
        }));

        EventBus.getDefault().register(this);
        this.shoppingLists = new ObservableArrayList<>(new ShoppingListIdExtractor(), shoppingListManager.getLists());
    }


    public void setListener(final Listener listener) {
        this.listener = listener;
    }


    // Getters / Setters

    public ObservableArrayList<ShoppingList, Integer> getShoppingLists() {
        return this.shoppingLists;
    }

    private void setShoppingLists(final ObservableArrayList<ShoppingList, Integer> value) {
        if (value != shoppingLists) {
            ObservableArrayList<ShoppingList, Integer> oldValue = this.shoppingLists;
            this.shoppingLists = value;
            if (listener != null) {
                listener.onShoppingListsChanged(oldValue, value);
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

    @SuppressWarnings("unused")
    public void onEventMainThread(ShoppingListChangedEvent ev) {

        switch (ev.getChangeType()) {

            case Deleted:
                shoppingLists.removeById(ev.getListId());
                break;

            case PropertiesModified:
            case Added:
                reloadList(ev.getListId());
                break;

            default:
                break;
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent ev) {


        switch (ev.getChangeType()) {

            case Deleted:
            case Added:
                reloadList(ev.getListId());
                break;

            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
    }


    private void reloadList(int listId) {
        ShoppingList list = shoppingListManager.getList(listId);
        synchronized (this) {

            int index = shoppingLists.indexOfById(list.getId());
            if (index >= 0) {
                shoppingLists.set(index, list);
            } else {
                shoppingLists.add(list);
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(ReminderTimeIsOverEvent e) {
        int itemId = e.getItemId();
        int listId = e.getListId();
        viewLauncher.showReminderView(listId, itemId);
    }

}
