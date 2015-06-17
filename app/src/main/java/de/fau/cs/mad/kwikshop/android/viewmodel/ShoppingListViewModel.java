package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.os.AsyncTask;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Group;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.DefaultDataProvider;
import de.fau.cs.mad.kwikshop.android.model.ItemParser;
import de.fau.cs.mad.kwikshop.android.model.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemLoadedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.MoveAllItemsEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListLoadedEvent;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.view.ItemSortType;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.LoadItemTask;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.LoadShoppingListTask;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.SaveItemTask;
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

        void onItemSortTypeChanged();
    }


    private boolean initialized = false;

    private final ViewLauncher viewLauncher;
    private final ListStorage listStorage;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<Group> groupStorage;
    private final DefaultDataProvider defaultDataProvider;
    private final ItemParser itemParser;
    private EventBus privateBus = EventBus.builder().build();

    private int shoppingListId;

    private Listener listener;
    private final ObservableArrayList<Item, Integer> items = new ObservableArrayList<Item, Integer>(new ItemIdExtractor());
    private final ObservableArrayList<Item, Integer> boughtItems = new ObservableArrayList<Item, Integer>(new ItemIdExtractor());
    private String quickAddText = "";
    private ItemSortType itemSortType;

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
    public ShoppingListViewModel(ViewLauncher viewLauncher, ListStorage listStorage,
                                 SimpleStorage<Unit> unitStorage, SimpleStorage<Group> groupStorage,
                                 DefaultDataProvider defaultDataProvider, ItemParser itemParser) {

        if(viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }
        if(listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }
        if(unitStorage == null) {
            throw new IllegalArgumentException("'unitStorage' must not be null");
        }
        if(groupStorage == null) {
            throw new IllegalArgumentException("'groupStorage' must not be null");
        }
        if(defaultDataProvider == null) {
            throw new IllegalArgumentException("'defaultDataProvider' must not be null");
        }
        if(itemParser == null) {
            throw new IllegalArgumentException("'itemParser' must not be null");
        }

        this.viewLauncher = viewLauncher;
        this.listStorage = listStorage;
        this.unitStorage = unitStorage;
        this.groupStorage = groupStorage;
        this.defaultDataProvider = defaultDataProvider;
        this.itemParser = itemParser;
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


    public ItemSortType getItemSortType() {
        return this.itemSortType;
    }

    public void setItemSortType(ItemSortType value) {
        if(value != itemSortType) {
            itemSortType = value;

            if(listener != null) {
                listener.onItemSortTypeChanged();
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


    public void swapItems(final int id1, final int id2) {

        if(items.containsById(id1) && items.containsById(id2)) {

            int position1 = items.indexOfById(id1);
            int position2 = items.indexOfById(id2);

            Item item1 = items.remove(position1);
            Item item2 = items.remove(position2);

            items.add(position1, item2);
            items.add(position2, item2);

            item1.setOrder(position2);
            item2.setOrder(position1);

            new SaveItemTask(listStorage, shoppingListId, item1, item2).execute();
        }
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

    public void onEventBackgroundThread(MoveAllItemsEvent event) {

        boolean isBoughtNew = event.isMoveAllToBought();
        ShoppingList list = listStorage.loadList(shoppingListId);

        List<Item> changedItems = new LinkedList<>();

        for(Item item : list.getItems()) {
            if(item.isBought() != isBoughtNew) {
                item.setBought(isBoughtNew);
                changedItems.add(item);
            }
        }
        list.save();

        for(Item item : changedItems) {
            EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.PropertiesModified, shoppingListId, item.getId()));
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
        final Item item = items.getById(id);

        if(item != null) {
            new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... params) {

                    ShoppingList shoppingList = listStorage.loadList(shoppingListId);
                    shoppingList.removeItem(item);
                    shoppingList.save();
                    EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.PropertiesModified, shoppingListId, item.getId()));
                    return null;

                }

            }.execute();
        }
    }

    private void quickAddCommandExecute() {
        ensureIsInitialized();


        final String text = getQuickAddText();
        //reset quick add text
        setQuickAddText("");

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                Item newItem = new Item();
                newItem.setName(text);
                newItem.setUnit(unitStorage.getDefaultValue());
                newItem = itemParser.parseAmountAndUnit(newItem);
                newItem.setGroup(groupStorage.getDefaultValue());

                ShoppingList shoppingList = listStorage.loadList(shoppingListId);
                shoppingList.addItem(newItem);

                listStorage.saveList(shoppingList);

                EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.ItemsAdded, shoppingList.getId()));
                EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.Added, shoppingList.getId(), newItem.getId()));

                return null;
            }
        }.execute();


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
            boughtItems.notifyItemModifiedById(id);

        } else {

            boughtItems.removeById(id);
            items.setOrAddById(item);
            boughtItems.notifyItemModifiedById(id);

        }
    }
}
