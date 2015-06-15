package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.os.AsyncTask;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemLoadedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListLoadedEvent;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.LoadItemTask;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.LoadShoppingListTask;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;

public class ShoppingListViewModel extends ShoppingListViewModelBase {


    private static class ItemIdExtractor implements ObservableArrayList.IdExtractor<Item, Integer> {

        @Override
        public Integer getId(Item object) {
            return object.getId();
        }
    }

    public interface Listener extends ShoppingListViewModelBase.Listener {

        void onQuickAddTextChanged();

    }


    private boolean initialized = false;

    private final ViewLauncher viewLauncher;
    private final ListStorage listStorage;
    private EventBus privateBus = EventBus.builder().build();

    private int shoppingListId;

    private Listener listener;
    private final ObservableArrayList<Item, Integer> items = new ObservableArrayList<Item, Integer>(new ItemIdExtractor());
    private final ObservableArrayList<Item, Integer> boughtItems = new ObservableArrayList<Item, Integer>(new ItemIdExtractor());
    private String quickAddText = "";


    private final Command addItemCommand = new Command() {
        @Override
        public void execute(Object parameter) {
            addItemCommandExecute();
        }
    };
    private final Command quickAddCommand = new Command() {
        @Override
        public void execute(Object parameter) {quickAddCommandExecute();
        }
    };
    private final Command<Integer> selectItemCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) { selectItemCommandExecute(parameter); }
    };
    private final Command<Integer> toggleIsBoughtCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) { toggleIsBoughtCommandExecute(parameter); }
    };
    private final Command<Integer> deleteItemCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) { deleteItemCommandExecute(parameter);}
    };


    @Inject
    public ShoppingListViewModel(ViewLauncher viewLauncher, ListStorage listStorage) {

        if(viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }
        if(listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        this.viewLauncher = viewLauncher;
        this.listStorage = listStorage;
    }

    public void initialize(int shoppingListId) {
        if(!initialized) {
            this.shoppingListId = shoppingListId;
            privateBus.register(this);
            EventBus.getDefault().register(this);

            new LoadShoppingListTask(this.listStorage, privateBus, this.shoppingListId).execute();

            initialized = true;
        }
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public ObservableArrayList<Item, Integer> getItems() {
        return items;
    }

    public ObservableArrayList<Item, Integer> getBoughtItems() {
        return boughtItems;
    }

    public String getQuickAddText() {
        return quickAddText;
    }

    public void setQuickAddText(String value) {
        if(value == null) {
            value = "";
        }

        if(!value.equals(quickAddText)) {
            quickAddText = value;
            quickAddCommand.setCanExecute(!StringHelper.isNullOrWhiteSpace(quickAddText));
            if(listener != null) {
                listener.onQuickAddTextChanged();
            }
        }
    }

    public Command getAddItemCommand() {
        return addItemCommand;
    }

    public Command getQuickAddCommand() {
        return quickAddCommand;
    }

    public Command<Integer> getSelectItemCommand() {
        return selectItemCommand;
    }

    public Command<Integer> getToggleIsBoughtCommand() {
        return  toggleIsBoughtCommand;
    }

    public Command<Integer> getDeleteItemCommand() {
        return deleteItemCommand;
    }


    public void onEventMainThread(ShoppingListLoadedEvent event) {

        ShoppingList shoppingList = event.getShoppingList();
        if(shoppingList.getId() == this.shoppingListId) {

            this.setName(shoppingList.getName());

            for(Item item : event.getShoppingList().getItems()) {
               updateItem(item);
            }
        }
    }

    public void onEventMainThread(ItemLoadedEvent event) {

        if(event.getShoppingListId() == this.shoppingListId) {
            updateItem(event.getItem());
        }

    }

    public void onEventBackgroundThread(ShoppingListChangedEvent event) {

        if(event.getListId() == this.shoppingListId) {

            switch (event.getChangeType()) {

                case PropertiesModified:
                case ItemsAdded:
                case ItemsRemoved:
                    new LoadShoppingListTask(listStorage, privateBus, shoppingListId).execute();
                    break;

                case Deleted:
                    finish();
                    break;

                case Added:
                    //ignore (should not happen anyways)
                    break;

            }

        }
    }

    public void onEventBackgroundThread(ItemChangedEvent event) {

        if(event.getShoppingListId() == this.shoppingListId) {

            switch (event.getChangeType()) {

                case  Added:
                case PropertiesModified:
                    new LoadItemTask(listStorage, privateBus, shoppingListId, event.getItemId()).execute();
                    break;

                case  Deleted:
                    break;

            }
        }
    }

    public void onEventMainThread(ItemChangedEvent event) {

        if(event.getShoppingListId() == this.shoppingListId) {

            switch (event.getChangeType()) {

                case  Added:
                case PropertiesModified:
                    break;
                case  Deleted:
                    items.removeById(event.getItemId());
                    boughtItems.removeById(event.getItemId());
                    break;

            }
        }
    }


    @Override
    protected Listener getListener() {
        return this.listener;
    }

    @Override
    public void onDestroyView() {
        privateBus.unregister(this);
        EventBus.getDefault().unregister(this);
    }


    private void toggleIsBoughtCommandExecute(final int id) {
        final Item item = items.getById(id);
        if(item == null) {
            boughtItems.getById(id);
        }

        item.setBought(!item.isBought());

        if(item != null) {

            new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... params) {

                    ShoppingList shoppingList = listStorage.loadList(shoppingListId);
                    shoppingList.removeItem(item);
                    shoppingList.addItem(item);

                    shoppingList.save();

                    EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.PropertiesModified, shoppingListId, id));
                    return null;
                }

            }.execute();

        }
    }

    private void deleteItemCommandExecute(int id) {
        //TODO
    }

    private void quickAddCommandExecute() {
        ensureIsInitialized();
        //TODO
    }

    private void addItemCommandExecute() {
        ensureIsInitialized();
        viewLauncher.showItemDetailsView(this.shoppingListId);
    }

    private void selectItemCommandExecute(int itemId) {
        ensureIsInitialized();
        viewLauncher.showItemDetailsView(this.shoppingListId, itemId);
    }


    private void ensureIsInitialized() {
        if(!initialized) {
            throw new UnsupportedOperationException("You need to call initialized before view model can perform commands");
        }
    }


    private void updateItem(Item item) {

        int id = item.getId();

        if(item.isBought()) {

            items.removeById(id);
            boughtItems.setOrAddById(item);

        } else {

            boughtItems.removeById(id);
            items.setOrAddById(item);

        }
    }
}
