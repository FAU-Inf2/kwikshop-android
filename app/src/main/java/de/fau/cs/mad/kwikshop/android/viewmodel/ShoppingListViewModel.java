package de.fau.cs.mad.kwikshop.android.viewmodel;


import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.*;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.*;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.android.util.ItemComparator;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.view.BarcodeScannerFragment;
import de.fau.cs.mad.kwikshop.android.view.BaseActivity;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ItemSortType;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.*;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RepeatType;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.ItemOrderWrapper;
import de.greenrobot.event.EventBus;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

public class ShoppingListViewModel extends ListViewModel<ShoppingList> {

    private Context context;
    private int tmp_item_id;

    private boolean inShoppingMode = false;
    private ArrayList<Item> swipedItemOrder = new ArrayList<>();
    private List<Place> places;
    private int placesChoiceIndex;

    private final ResourceProvider resourceProvider;
    private final RegularlyRepeatHelper repeatHelper;
    private final ListManager<Recipe> recipeManager;
    private final RestClientFactory clientFactory;


    private final ObservableArrayList<ItemViewModel, Integer> boughtItems = new ObservableArrayList<>(new ItemIdExtractor());
    private ItemSortType itemSortType = ItemSortType.MANUAL;

    private final Command<Integer> toggleIsBoughtCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) {
            toggleIsBoughtCommandExecute(parameter);
        }
    };
    private final Command<Integer> deleteItemCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) {
            deleteItemCommandExecute(parameter);
        }
    };
    private final Command<Void> sendBoughtItemsToServerCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            sendBoughtItemsToServerCommandExecute();
        }
    };

    public ObservableArrayList<ItemViewModel, Integer> getCheckedItems() {
        return checkedItems;
    }

    private final ObservableArrayList<ItemViewModel, Integer> checkedItems = new ObservableArrayList<ItemViewModel, Integer>(new ItemIdExtractor());

    final Command<Void> deleteCheckBoxCheckedCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG, false, context);
        }
    };

    final Command<Void> deleteNegativeCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            //do nothing
            //this is just so the command is executable
        }
    };

    final Command<Void> deletePositiveCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            listManager.deleteItem(listId, tmp_item_id);
        }
    };


    @Inject
    public ShoppingListViewModel(Context context,
                                 ViewLauncher viewLauncher,
                                 ListManager<ShoppingList> shoppingListManager,
                                 ListManager<Recipe> recipeManager,
                                 SimpleStorage<Unit> unitStorage,
                                 SimpleStorage<Group> groupStorage,
                                 ItemParser itemParser,
                                 DisplayHelper displayHelper,
                                 AutoCompletionHelper autoCompletionHelper,
                                 LocationFinderHelper locationFinderHelper,
                                 ResourceProvider resourceProvider,
                                 RegularlyRepeatHelper repeatHelper,
                                 RestClientFactory clientFactory) {


        super(viewLauncher, shoppingListManager, unitStorage, groupStorage, itemParser, displayHelper,
                autoCompletionHelper, locationFinderHelper);

        if (context == null) throw new ArgumentNullException("context");

        if (resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if (repeatHelper == null) {
            throw new ArgumentNullException("repeatHelper");
        }

        if (recipeManager == null) {
            throw new ArgumentNullException("recipeManager");
        }

        if (clientFactory == null) {
            throw new ArgumentNullException("clientFactory");
        }


        this.context = context;
        this.resourceProvider = resourceProvider;
        this.repeatHelper = repeatHelper;
        this.recipeManager = recipeManager;
        this.clientFactory = clientFactory;
    }


    /**
     * Gets the shopping list items that have already been bought
     */
    /*public ObservableArrayList<Item, Integer> getBoughtItems() {
        return boughtItems;
    }*/

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
        this.itemSortType = value;
    }

    public void sortItems() {
        Collections.sort(getItems(), new ItemComparator(displayHelper, getItemSortType()));
        moveBoughtItemsToEnd();
        updateOrderOfItems();
        listener.onItemSortTypeChanged();
    }

    /**
     * Gets the command to be executed when a item in the view is swiped
     */
    public Command<Integer> getToggleIsBoughtCommand() {
        return toggleIsBoughtCommand;
    }

    /**
     * Gets the command to be executed when an item's delete-button is pressed
     */
    public Command<Integer> getDeleteItemCommand() {
        return deleteItemCommand;
    }

    public Command<Void> getSendBoughtItemsToServerCommand() {
        return sendBoughtItemsToServerCommand;
    }


    @Override
    public void itemsSwapped(int position1, int position2) {
        Item item1 = items.get(position1).getItem();
        Item item2 = items.get(position2).getItem();

        item1.setOrder(position1);
        item2.setOrder(position2);

        listManager.saveListItem(listId, item1);
        listManager.saveListItem(listId, item2);
    }


    public void deleteItem(int itemId, String title, String message, String positiveString, String negativeString, String checkBoxMessage) {
        this.tmp_item_id = itemId;
        if (SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG, true, context))
            viewLauncher.showMessageDialogWithCheckbox(title, message, positiveString, deletePositiveCommand, null, null, negativeString, deleteNegativeCommand, checkBoxMessage, false, deleteCheckBoxCheckedCommand, null);
        else
            deletePositiveCommand.execute(null);
    }

    public void showAddRecipeDialog(int listId) {
        if (recipeManager.getLists().size() == 0) {
            viewLauncher.showMessageDialog(resourceProvider.getString(R.string.recipe_add_recipe), resourceProvider.getString(R.string.recipe_no_recipe),
                    resourceProvider.getString(R.string.yes),
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            viewLauncher.showAddRecipeView();
                        }
                    },
                    resourceProvider.getString(R.string.no), null);
        } else {
            viewLauncher.showAddRecipeDialog(listManager, recipeManager, listId, true);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ShoppingListChangedEvent event) {

        if (event.getListId() == this.listId) {
            if (event.getChangeType() == ListChangeType.Deleted) {
                finish();
            } else if (event.getChangeType() == ListChangeType.PropertiesModified) {
                loadList();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent event) {

        if (event.getListType() == ListType.ShoppingList && event.getListId() == this.listId) {

            switch (event.getChangeType()) {

                case Added:
                    Item item = listManager.getListItem(listId, event.getItemId());
                    updateItem(new ItemViewModel(item));
                    sortItems();
                    updateOrderOfItems();
                    break;
                case PropertiesModified:
                    Item item1 = listManager.getListItem(listId, event.getItemId());
                    updateItem(new ItemViewModel(item1));
                    updateOrderOfItems();
                    break;
                case Deleted:
                    items.removeById(event.getItemId());
                    break;

            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(MoveAllItemsEvent event) {

        boolean isBoughtNew = event.isMoveAllToBought();
        ShoppingList list = listManager.getList(listId);

        List<Item> changedItems = new LinkedList<>();

        for (Item item : list.getItems()) {
            if (item.isBought() != isBoughtNew) {
                item.setBought(isBoughtNew);
                changedItems.add(item);
            }
        }

        for (Item item : changedItems) {
            listManager.saveListItem(listId, item);
        }

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemSortType sortType) {
        setItemSortType(sortType);
        ShoppingList list = listManager.getList(listId);
        list.setSortTypeInt(sortType.ordinal());
        listManager.saveList(list.getId());
        sortItems();
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

        for(Item item : shoppingList.getItems()) {
            updateItem(new ItemViewModel(item));
        }

        int  sortTypeInt = shoppingList.getSortTypeInt();
        switch (sortTypeInt) {
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
        sortItems();
        //moveBoughtItemsToEnd();

        this.setName(shoppingList.getName());
    }

    public void setInShoppingMode(boolean bool) {
        this.inShoppingMode = bool;
    }

    public boolean getInShoppingMode() {
        return this.inShoppingMode;
    }

    public void setPlaces(List<Place> places){
        this.places = places;
    }

    public void setPlacesChoiceIndex(int placesChoiceIndex) {
        this.placesChoiceIndex = placesChoiceIndex;
    }

    public List<Item> getSwipedItemOrder(){ return this.swipedItemOrder; }

    private void toggleIsBoughtCommandExecute(final int id) {
        Item item = items.getById(id).getItem();

        if(item != null) {

            item.setBought(!item.isBought());

            if (item.isBought()) {
                swipedItemOrder.add(item);
                Calendar now = Calendar.getInstance();
                item.setLastBought(now.getTime());

                if (item.getRepeatType() == RepeatType.Schedule && item.isRemindFromNextPurchaseOn()) {

                    //save location
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

                    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT, resourceProvider.getLocale());
                    String message = resourceProvider.getString(R.string.reminder_set_msg) + " " + dateFormat.format(remindDate.getTime());
                    viewLauncher.showToast(message, Toast.LENGTH_LONG);
                }
            } else {
                swipedItemOrder.remove(item);
            }

            listManager.saveListItem(listId, item);
        }
    }

    private void sendBoughtItemsToServerCommandExecute() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {

                    List<BoughtItem> boughtItemOrder = new ArrayList<>();
                    for (Item item : swipedItemOrder) {
                        boughtItemOrder.add(new BoughtItem(item.getName()));
                    }

                    ItemOrderWrapper itemOrderWrapper = new ItemOrderWrapper(boughtItemOrder, places.get(placesChoiceIndex).getPlaceId(),
                            places.get(placesChoiceIndex).getName());
                    clientFactory.getShoppingListClient().postItemOrder(itemOrderWrapper);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;

            }
        }.execute();
    }

    private void deleteItemCommandExecute(final int id) {
        listManager.deleteItem(listId, id);
    }

    public int getBoughtItemsCount() {
        ListIterator li = items.listIterator(items.size());
        int i = 0;

        while(li.hasPrevious()) {
            ItemViewModel item = (ItemViewModel)li.previous();
            if(item.getItem().isBought())
                i++;
            else
                break;
        }
        return i;
    }

    public void moveBoughtItemsToEnd() {
        Collections.sort(getItems(), new ItemComparator(displayHelper, ItemSortType.BOUGHTITEMS));
    }

    public void changeCheckBoxesVisibility(){
        Iterator <ItemViewModel> itr = items.iterator();
        while(itr.hasNext()){
            updateItemViewModel(itr.next());
        }
    }
    private void updateItem(ItemViewModel item) {
        if(item.getItem().isBought()) { // Add bought items at the end of the list
            if (items.size() - 1 >= 0) {
                items.setOrAddById(items.size() - 1, item);
            } else {
                items.setOrAddById(item);
            }
        } else {
            items.setOrAddById(item);
        }

        items.notifyItemModified(item);
    }

    private void updateItemViewModel(ItemViewModel item) {

            items.setOrAddById(item);

    }
}
