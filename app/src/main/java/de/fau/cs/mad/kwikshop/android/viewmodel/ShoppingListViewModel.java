package de.fau.cs.mad.kwikshop.android.viewmodel;


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
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ItemSortType;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.*;

public class ShoppingListViewModel extends ListViewModel<ShoppingList> {

    private final LocationFinderHelper locationFinderHelper;
    private final ResourceProvider resourceProvider;

    private final ObservableArrayList<Item, Integer> boughtItems = new ObservableArrayList<>(new ItemIdExtractor());
    private ItemSortType itemSortType = ItemSortType.MANUAL;


    private final Command<Integer> toggleIsBoughtCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) { toggleIsBoughtCommandExecute(parameter); }
    };
    private final Command<Integer> deleteItemCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) { deleteItemCommandExecute(parameter);}
    };


    @Inject
    public ShoppingListViewModel(ViewLauncher viewLauncher,
                                 ListManager<ShoppingList> shoppingListManager,
                                 SimpleStorage<Unit> unitStorage,
                                 SimpleStorage<Group> groupStorage,
                                 ItemParser itemParser,
                                 DisplayHelper displayHelper,
                                 AutoCompletionHelper autoCompletionHelper,
                                 LocationFinderHelper locationFinderHelper,
                                 ResourceProvider resourceProvider) {


        super(viewLauncher, shoppingListManager, unitStorage, groupStorage, itemParser, displayHelper,
                autoCompletionHelper);

        if(locationFinderHelper == null) {
            throw new IllegalArgumentException("'locationFinderHelper' must not be null");
        }

        if (resourceProvider == null) {
            throw new IllegalArgumentException("'resourceProvider' must not be null");
        }

        this.locationFinderHelper = locationFinderHelper;
        this.resourceProvider = resourceProvider;
    }


    /**
     * Gets the shopping list items that have already been bought
     */
    public ObservableArrayList<Item, Integer> getBoughtItems() {
        return boughtItems;
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


    public void setLocationOnStartingShopping(){

        ShoppingList shoppingList = listManager.getList(this.listId);

        if(!shoppingList.getLocation().isVisited()){
            shoppingList.setLocation(locationFinderHelper.setLocation());
            listManager.saveList(listId);
        }

    }

    public void boughtItemsSwapped(int position1, int position2) {

        Item item1 = boughtItems.get(position1);
        Item item2 = boughtItems.get(position2);

        item1.setOrder(position2);
        item2.setOrder(position1);

        listManager.saveListItem(listId, item1);
        listManager.saveListItem(listId, item2);
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(ShoppingListChangedEvent event) {

        if(event.getListId() == this.listId) {

            if(event.getChangeType() == ListChangeType.Deleted) {
                finish();
            } else if(event.getChangeType() == ListChangeType.PropertiesModified) {
                loadList();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent event) {

        if(event.getListId() == this.listId) {

            switch (event.getChangeType()) {

                case  Added:
                case PropertiesModified:
                    Item item = listManager.getListItem(listId, event.getItemId());
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
        ShoppingList list = listManager.getList(listId);

        List<Item> changedItems = new LinkedList<>();

        for(Item item : list.getItems()) {
            if(item.isBought() != isBoughtNew) {
                item.setBought(isBoughtNew);
                changedItems.add(item);
            }
        }

        for(Item item : changedItems) {
            listManager.saveListItem(listId, item);
        }

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemSortType sortType) {
        setItemSortType(sortType);
    }


    @Override
    protected void addItemCommandExecute() {
        ensureIsInitialized();
        getItems().enableEvents();
        viewLauncher.showItemDetailsView(this.listId);
    }

    @Override
    protected void selectItemCommandExecute(int itemId) {
        ensureIsInitialized();
        viewLauncher.showItemDetailsView(this.listId, itemId);
    }

    @Override
    protected void loadList() {

        ShoppingList shoppingList = listManager.getList(this.listId);

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

            listManager.saveListItem(listId, item);
        }
    }

    private void deleteItemCommandExecute(final int id) {

        listManager.deleteItem(listId, id);
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
