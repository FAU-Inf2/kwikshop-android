package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.os.AsyncTask;

import java.util.*;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.*;
import de.fau.cs.mad.kwikshop.android.model.*;
import de.fau.cs.mad.kwikshop.android.model.messages.*;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.view.ItemSortType;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.*;
import de.greenrobot.event.EventBus;

public class ShoppingListViewModel extends ShoppingListViewModelBase {

    public interface Listener extends ShoppingListViewModelBase.Listener {

        void onQuickAddTextChanged();

        void onItemSortTypeChanged();
    }

    private class CompositeListener implements Listener {

        @Override
        public void onQuickAddTextChanged() {
            for(Listener listener : listeners) {
                listener.onQuickAddTextChanged();
            }
        }

        @Override
        public void onItemSortTypeChanged() {
            for(Listener listener : listeners) {
                listener.onItemSortTypeChanged();
            }
        }

        @Override
        public void onNameChanged(String value) {
            for(Listener listener : listeners) {
                listener.onNameChanged(value);
            }
        }

        @Override
        public void onFinish() {
            for(Listener listener : listeners) {
                listener.onFinish();
            }
        }
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

    private final List<Listener> listeners = new ArrayList<>();
    private final Listener listener = new CompositeListener();

    private final ObservableArrayList<Item, Integer> items = new ObservableArrayList<Item, Integer>(new ItemIdExtractor());
    private final ObservableArrayList<Item, Integer> boughtItems = new ObservableArrayList<Item, Integer>(new ItemIdExtractor());
    private String quickAddText = "";
    private ItemSortType itemSortType = ItemSortType.MANUAL;

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


    /**
     * Initializes the view model. You need to call this before an instance can be used
     * @param shoppingListId The id of the shopping list to be displayed by the view model
     */
    public void initialize(int shoppingListId) {

        if(!initialized) {
            this.shoppingListId = shoppingListId;
            privateBus.register(this);
            EventBus.getDefault().register(this);

            new LoadShoppingListTask(this.listStorage, privateBus, this.shoppingListId).execute();

            initialized = true;
        }
    }


    /**
     * Adds a listener to be notified when changes in the view model occcur
     */
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    /**
     * Gets the shopping list items that have not yet been bought
     */
    public ObservableArrayList<Item, Integer> getItems() {
        return items;
    }

    /**
     * Gets the shopping list items that have already been bought
     */
    public ObservableArrayList<Item, Integer> getBoughtItems() {
        return boughtItems;
    }

    /**
     * Gets the current value for the quick-add text field
     */
    public String getQuickAddText() {
        return quickAddText;
    }

    /**
     * Sets the value of the quick-add text field
     */
    public void setQuickAddText(String value) {

        if(value == null) {
            value = "";
        }

        if(!value.equals(quickAddText)) {
            quickAddText = value;
            quickAddCommand.setCanExecute(!StringHelper.isNullOrWhiteSpace(quickAddText));

            listener.onQuickAddTextChanged();
        }
    }

    /**
     * Gets how items are supposed to be sorted for the current shopping list
     */
    public ItemSortType getItemSortType() {
        return this.itemSortType;
    }

    /**
     * Sets how items are supposed to be sorted for the current shopping list
     */
    public void setItemSortType(ItemSortType value) {
        if(value != itemSortType) {
            itemSortType = value;

            listener.onItemSortTypeChanged();
        }
    }

    /**
     * Gets the command to be executed when the view's add button is pressed
     */
    public Command getAddItemCommand() {
        return addItemCommand;
    }

    /**
     * Gets the command to be executed when the view's quick-add button is pressed
     */
    public Command getQuickAddCommand() {
        return quickAddCommand;
    }

    /**
     * Gets the command to be executed when a shopping list item in the view is selected
     */
    public Command<Integer> getSelectItemCommand() {
        return selectItemCommand;
    }

    /**
     * Gets the command to be executed when a item in the view is swiped
     */
    public Command<Integer> getToggleIsBoughtCommand() {
        return  toggleIsBoughtCommand;
    }

    /**
     * Gets the command to be executed when an item's delete-button is pressed
     */
    public Command<Integer> getDeleteItemCommand() {
        return deleteItemCommand;
    }


    /**
     * Swaps the positions of the specified items in the shopping list
     */
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

        //on the main thread, update the displayed shopping list after is has been (re-) loaded

        ShoppingList shoppingList = event.getShoppingList();

        if(shoppingList.getId() == this.shoppingListId) {

            int  sortTypeInt = shoppingList.getSortTypeInt();
            switch (sortTypeInt){
                case 1:
                    setItemSortType(ItemSortType.GROUP);
                    break;
                case 2:
                    setItemSortType(ItemSortType.ALPHABETICALLY);
                    break;
                default:
                    setItemSortType(ItemSortType.MANUAL);
                    break;
            }


            this.setName(shoppingList.getName());

            for(Item item : event.getShoppingList().getItems()) {
               updateItem(item);
            }
        }
    }

    public void onEventMainThread(ItemLoadedEvent event) {

        // on the main thread, update an displayed item after it has been loaded

        if(event.getShoppingListId() == this.shoppingListId) {
            updateItem(event.getItem());
        }

    }

    public void onEventMainThread(ShoppingListChangedEvent event) {

        if(event.getListId() == this.shoppingListId) {

            if(event.getChangeType() == ShoppingListChangeType.Deleted) {
                finish();
            }
        }
    }

    public void onEventBackgroundThread(ShoppingListChangedEvent event) {

        if(event.getListId() == this.shoppingListId) {

            if(event.getChangeType() == ShoppingListChangeType.PropertiesModified) {
                new LoadShoppingListTask(listStorage, privateBus, shoppingListId).execute();

            }

            //other change types should already be covered by other event handlers

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
                    // ignore (handled on background thread)
                    break;

            }
        }
    }

    public void onEventMainThread(ItemChangedEvent event) {

        if(event.getShoppingListId() == this.shoppingListId) {

            switch (event.getChangeType()) {

                case  Added:
                case PropertiesModified:
                    // ignore (handled on background thread)
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

        if(item != null) {

            item.setBought(!item.isBought());
            new SaveItemTask(listStorage, shoppingListId, item).execute();
        }
    }

    private void deleteItemCommandExecute(int id) {
        final Item item = items.getById(id);

        if(item != null) {
            new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... params) {

                    ShoppingList shoppingList = listStorage.loadList(shoppingListId);
                    if(shoppingList.removeItem(item)) {
                        shoppingList.save();
                        EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.ItemsRemoved, shoppingListId));
                        EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.Deleted, shoppingListId, item.getId()));
                    }
                    return null;
                }

            }.execute();
        }
    }

    private synchronized void quickAddCommandExecute() {

        ensureIsInitialized();

        final String text = getQuickAddText();
        //reset quick add text
        setQuickAddText("");


        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                if(!StringHelper.isNullOrWhiteSpace(text)) {

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
                }
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
