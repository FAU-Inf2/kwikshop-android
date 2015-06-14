package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.os.AsyncTask;

import java.util.Collection;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListLoadedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.LoadShoppingListTask;
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
    private final ListStorage listStorage;
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
    public ListOfShoppingListsViewModel(ViewLauncher viewLauncher, ListStorage listStorage) {

        this.viewLauncher = viewLauncher;
        this.listStorage = listStorage;

        setShoppingLists(new ObservableArrayList<>(new ObservableArrayList.IdExtractor<ShoppingList, Integer>() {
            @Override
            public Integer getId(ShoppingList object) {
                return object.getId();
            }
        }));

        EventBus.getDefault().register(this);
        privateBus.register(this);

        new LoadShoppingListTask(listStorage, privateBus).execute();
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

    public void onEventMainThread(ShoppingListChangedEvent ev) {

        switch (ev.getChangeType()) {

            case Deleted:
                shoppingLists.removeById(ev.getListId());
                break;

            case PropertiesModified:
            case ItemsAdded:
            case ItemsRemoved:
            case Added:
                loadShoppingListAsync(ev.getListId());
                break;

            default:
                break;
        }
    }

    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
    }


    private void loadShoppingListAsync(int id) {

        AsyncTask<Object, Object, Collection<ShoppingList>> task = new LoadShoppingListTask(listStorage, privateBus, id);
        task.execute();
    }

    public void onEventMainThread(ShoppingListLoadedEvent event) {

        ShoppingList loadedList = event.getShoppingList();

        synchronized (this) {

            int index = shoppingLists.indexOfById(loadedList.getId());
            if (index >= 0) {
                shoppingLists.set(index, loadedList);
            } else {
                shoppingLists.add(loadedList);
            }
        }

    }

}
