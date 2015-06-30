package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.os.AsyncTask;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.*;
import de.fau.cs.mad.kwikshop.android.model.*;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.*;
import de.fau.cs.mad.kwikshop.android.util.ItemComparator;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
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
    private final ListManager<ShoppingList> shoppingListManager;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<Group> groupStorage;
    private final ItemParser itemParser;
    protected final DisplayHelper displayHelper;
    private final AutoCompletionHelper autoCompletionHelper;
    private final LocationFinderHelper locationFinderHelper;
    private EventBus privateBus = EventBus.builder().build();
    private final ResourceProvider resourceProvider;

    private int shoppingListId;

    private final List<Listener> listeners = new ArrayList<>();
    private final Listener listener = new CompositeListener();

    private final ObservableArrayList<Item, Integer> items = new ObservableArrayList<>(new ItemIdExtractor());
    private final ObservableArrayList<Item, Integer> boughtItems = new ObservableArrayList<>(new ItemIdExtractor());
    private String quickAddText = "";
    private ItemSortType itemSortType = ItemSortType.MANUAL;

    private final Command<Void> addItemCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            addItemCommandExecute();
        }
    };
    private final Command<Void> quickAddCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {quickAddCommandExecute();
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
    public ShoppingListViewModel(ViewLauncher viewLauncher, ListManager<ShoppingList> shoppingListManager,
                                 SimpleStorage<Unit> unitStorage, SimpleStorage<Group> groupStorage,
                                 ItemParser itemParser, DisplayHelper displayHelper,
                                 AutoCompletionHelper autoCompletionHelper, LocationFinderHelper locationFinderHelper,
                                 final ResourceProvider resourceProvider) {


        if(viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }
        if(shoppingListManager == null) {
            throw new IllegalArgumentException("'shoppingListManager' must not be null");
        }
        if(unitStorage == null) {
            throw new IllegalArgumentException("'unitStorage' must not be null");
        }
        if(groupStorage == null) {
            throw new IllegalArgumentException("'groupStorage' must not be null");
        }
        if(itemParser == null) {
            throw new IllegalArgumentException("'itemParser' must not be null");
        }
        if(displayHelper == null) {
            throw new IllegalArgumentException("'displayHelper' must not be null");
        }
        if(autoCompletionHelper == null) {
            throw new IllegalArgumentException("'autoCompletionHelper' must not be null");
        }
        if(autoCompletionHelper == null) {
            throw new IllegalArgumentException("'locationFinderHelper' must not be null");
        }


        if (resourceProvider == null) {
            throw new IllegalArgumentException("'resourceProvider' must not be null");
        }


        this.viewLauncher = viewLauncher;
        this.shoppingListManager = shoppingListManager;
        this.unitStorage = unitStorage;
        this.groupStorage = groupStorage;
        this.itemParser = itemParser;
        this.displayHelper = displayHelper;
        this.autoCompletionHelper = autoCompletionHelper;
        this.locationFinderHelper = locationFinderHelper;
        this.resourceProvider = resourceProvider;
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

            loadShoppingList();
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
            Collections.sort(getItems(), new ItemComparator(displayHelper, value));
            listener.onItemSortTypeChanged();
        }
    }

    /**
     * Gets the command to be executed when the view's add button is pressed
     */
    public Command<Void> getAddItemCommand() {
        return addItemCommand;
    }

    /**
     * Gets the command to be executed when the view's quick-add button is pressed
     */
    public Command<Void> getQuickAddCommand() {
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
     * To be called by teh view after two items have been swapped
     */
    public void itemsSwapped(int position1, int position2) {

        Item item1 = items.get(position1);
        Item item2 = items.get(position2);

        item1.setOrder(position2);
        item2.setOrder(position1);

        shoppingListManager.saveListItem(shoppingListId, item1);
        shoppingListManager.saveListItem(shoppingListId, item2);
    }

    public void setLocationOnStartingShopping(){

        ShoppingList shoppingList = shoppingListManager.getList(this.shoppingListId);

        if(!shoppingList.getLocation().isVisited()){
            shoppingList.setLocation(locationFinderHelper.setLocation());
        }

    }


    public void boughtItemsSwapped(int position1, int position2) {

        Item item1 = boughtItems.get(position1);
        Item item2 = boughtItems.get(position2);

        item1.setOrder(position2);
        item2.setOrder(position1);

        shoppingListManager.saveListItem(shoppingListId, item1);
        shoppingListManager.saveListItem(shoppingListId, item2);
    }


    private void loadShoppingList() {

        ShoppingList shoppingList = shoppingListManager.getList(this.shoppingListId);

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

        for(Item item : shoppingList.getItems()) {
            updateItem(item);
        }
    }






    @SuppressWarnings("unused")
    public void onEventMainThread(ShoppingListChangedEvent event) {

        if(event.getListId() == this.shoppingListId) {

            if(event.getChangeType() == ListChangeType.Deleted) {
                finish();
            } else if(event.getChangeType() == ListChangeType.PropertiesModified) {
                loadShoppingList();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent event) {

        if(event.getListId() == this.shoppingListId) {

            switch (event.getChangeType()) {

                case  Added:
                case PropertiesModified:
                    Item item = shoppingListManager.getListItem(shoppingListId, event.getItemId());
                    updateItem(item);

                    break;
                case  Deleted:
                    items.removeById(event.getItemId());
                    boughtItems.removeById(event.getItemId());
                    break;

            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(MoveAllItemsEvent event) {

        boolean isBoughtNew = event.isMoveAllToBought();
        ShoppingList list = shoppingListManager.getList(shoppingListId);

        List<Item> changedItems = new LinkedList<>();

        for(Item item : list.getItems()) {
            if(item.isBought() != isBoughtNew) {
                item.setBought(isBoughtNew);
                changedItems.add(item);
            }
        }

        for(Item item : changedItems) {
            shoppingListManager.saveListItem(shoppingListId, item);
        }

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemSortType sortType) {
        setItemSortType(sortType);
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
        Item item = items.getById(id);
        if(item == null) {
            item = boughtItems.getById(id);
        }

        if(item != null) {

            item.setBought(!item.isBought());
            if (item.isBought()) {
                if (item.isRegularlyRepeatItem() && item.isRemindFromNextPurchaseOn() && item.getLastBought() == null) {
                    Calendar now = Calendar.getInstance();
                    item.setLastBought(now.getTime());
                    RegularlyRepeatHelper repeatHelper = RegularlyRepeatHelper.getInstance();

                    Calendar remindDate = Calendar.getInstance();
                    switch (item.getPeriodType()) {
                        case DAYS:
                            remindDate.add(Calendar.DAY_OF_MONTH, item.getSelectedRepeatTime());
                            break;
                        case WEEKS:
                            remindDate.add(Calendar.DAY_OF_MONTH, item.getSelectedRepeatTime() * 7);
                            break;
                        case MONTHS:
                            remindDate.add(Calendar.MONTH, item.getSelectedRepeatTime());
                            break;
                    }
                    item.setRemindAtDate(remindDate.getTime());
                    repeatHelper.offerRepeatData(item);

                    DateFormat dateFormat = new SimpleDateFormat(resourceProvider.getString(R.string.time_format));
                    String message = resourceProvider.getString(R.string.reminder_set_msg) + " " + dateFormat.format(remindDate.getTime());
                    viewLauncher.showToast(message, Toast.LENGTH_LONG);
                }
            }

            shoppingListManager.saveListItem(shoppingListId, item);
        }
    }

    private void deleteItemCommandExecute(final int id) {

        shoppingListManager.deleteItem(shoppingListId, id);
    }

    private synchronized void quickAddCommandExecute() {

        ensureIsInitialized();

        final String text = getQuickAddText();
        //reset quick add text
        setQuickAddText("");

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void[] params) {

                if(!StringHelper.isNullOrWhiteSpace(text)) {

                    ArrayList<String> listOfItems = itemParser.parseSeveralItems(text);
                    for(int i = 0; i < listOfItems.size(); i++){
                        Item newItem = new Item();
                        newItem.setName(listOfItems.get(i));
                        newItem.setUnit(unitStorage.getDefaultValue());
                        newItem = itemParser.parseAmountAndUnit(newItem);
                        Group group = AutoCompletionHelper.getInstance().getGroup(AutoCompletionHelper.removeSpacesAtEndOfWord(text));
                        if (group == null) {
                            newItem.setGroup(groupStorage.getDefaultValue());
                        } else {
                            newItem.setGroup(group);
                        }

                        shoppingListManager.addListItem(shoppingListId, newItem);

                        autoCompletionHelper.offerName(newItem.getName());
                    }

                }
                return null;
            }
        }.execute();


    }

    private void addItemCommandExecute() {
        ensureIsInitialized();
        getItems().enableEvents();
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
