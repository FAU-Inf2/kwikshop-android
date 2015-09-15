package de.fau.cs.mad.kwikshop.android.viewmodel;


import android.content.Context;
import android.os.AsyncTask;
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
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ItemSortType;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.*;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RepeatType;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.sorting.BoughtItem;
import de.fau.cs.mad.kwikshop.common.sorting.ItemOrderWrapper;
import se.walkercrou.places.Place;

public class ShoppingListViewModel extends ListViewModel<ShoppingList> {


    //region Fields

    private int tmp_item_id;

    private List<Place> places;
    private ItemSortType itemSortType = ItemSortType.MANUAL;
    private final ObservableArrayList<ItemViewModel, Integer> checkedItems = new ObservableArrayList<>(new ItemIdExtractor());

    private boolean findNearbySupermarketCanceled = false;

    private Context context;
    private final ResourceProvider resourceProvider;
    private final RegularlyRepeatHelper repeatHelper;
    private final ListManager<Recipe> recipeManager;
    private final RestClientFactory clientFactory;
    private final LocationManager locationManager;

    private Place currentPlace;


    //region Commands

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

    private final Command<Void> findNearbySupermarketCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            findNearbySupermarketCommandExecute();
        }
    };
    final Command<Void> deleteCheckBoxCheckedCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG, false, context);
        }
    };

    final Command<Void> deletePositiveCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            listManager.deleteItem(listId, tmp_item_id);
        }
    };

    final Command<Void> withdrawLocalizationPermissionCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, false, context);
        }
    };

    final Command<Void> doNotShowLocalizationPermissionAgainCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION_SHOW_AGAIN_MSG, false, context);
        }
    };

    final Command<Void> showLocalizationPermissionAgainCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION_SHOW_AGAIN_MSG, true, context );
        }
    };

    public Command<Void> restartShoppingListWithLocalization = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, true, context);
            viewLauncher.showShoppingListWithSupermarketDialog(listId);

        }
    };





    //endregion

    //endregion


    //region Constructor

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
                                 RestClientFactory clientFactory,
                                 LocationManager locationManager,
                                 SimpleStorage<BoughtItem> boughtItemStorage) {


        super(viewLauncher, shoppingListManager, unitStorage, groupStorage, itemParser, displayHelper,
                autoCompletionHelper, locationFinderHelper, boughtItemStorage);

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

        if(locationManager == null) {
            throw new ArgumentNullException("locationManager");
        }

        if(boughtItemStorage == null) {
            throw new ArgumentNullException("boughtItemStorage");
        }

        this.context = context;
        this.resourceProvider = resourceProvider;
        this.repeatHelper = repeatHelper;
        this.recipeManager = recipeManager;
        this.clientFactory = clientFactory;
        this.locationManager = locationManager;
    }


    //endregion


    //region Properties


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

    public Command<Void> getFindNearbySupermarketCommand() {
        return this.findNearbySupermarketCommand;
    }




    public ObservableArrayList<ItemViewModel, Integer> getCheckedItems() {
        return checkedItems;
    }


    public int getBoughtItemsCount() {
        int i = 0;
        for(ItemViewModel item : items) {
            if(item.getItem().isBought()) {
                i++;
            }
        }
        return i;
    }

    //endregion


    //region Public Methods

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
            viewLauncher.showMessageDialogWithCheckbox(title, message, positiveString, deletePositiveCommand, null, null,
                    negativeString, NullCommand.VoidInstance,
                    checkBoxMessage, false, deleteCheckBoxCheckedCommand, null);
        else
            deletePositiveCommand.execute(null);
    }

    //TODO: this should be a command
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


    public void moveBoughtItemsToEnd() {
        Collections.sort(getItems(), new ItemComparator(displayHelper, ItemSortType.BOUGHTITEMS));
    }

    public void changeCheckBoxesVisibility(){
        Iterator <ItemViewModel> itr = items.iterator();
        while(itr.hasNext()){
            updateItemViewModel(itr.next());
        }
    }


    //endregion


    //region Event Handlers

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

    // results of place request
    @SuppressWarnings("unused")
    public void onEventMainThread(FindSupermarketsResult result) {

        if(!findNearbySupermarketCanceled) {

            viewLauncher.dismissDialog();
            setPlaces(result.getPlaces());

            if(!LocationFinderHelper.checkPlaces(places)){
                // no place info dialog
                viewLauncher.showMessageDialog(
                        resourceProvider.getString(R.string.localization_no_place_dialog_title),
                        resourceProvider.getString(R.string.no_place_dialog_message),
                        resourceProvider.getString(R.string.dialog_OK),
                        NullCommand.VoidInstance,
                        resourceProvider.getString(R.string.dialog_retry),
                        getFindNearbySupermarketCommand()
                );

                return;
            }

            showSelectCurrentSupermarket(places);
        }
    }

    //endregion


    //region Command Implementations

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

    private void deleteItemCommandExecute(final int id) {
        listManager.deleteItem(listId, id);
    }

    private void findNearbySupermarketCommandExecute() {

        boolean isLocalizationEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, false, context);

        findNearbySupermarketCanceled = false;

        if(isLocalizationEnabled){

            if(InternetHelper.checkInternetConnection(context)){

                // progress dialog with listID to cancel progress
                viewLauncher.showProgressDialogWithListID(
                        resourceProvider.getString(R.string.supermarket_finder_progress_dialog_message),
                        null,
                        listId,
                        true,
                        new Command<Integer>() {
                            @Override
                            public void execute(Integer parameter) {
                                if(parameter == listId) {
                                    findNearbySupermarketCanceled = true;
                                }
                            }
                        }
                );

                // place request: radius 1000 result count 5
                getNearbySupermarketPlaces(500, 10);

            } else {

                // no connection dialog
                notificationOfNoConnectionWithLocationPermission();
            }
        } else {

            // No permission for location tracking - ask for permission dialog
            if(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.LOCATION_PERMISSION_SHOW_AGAIN_MSG, true, context)){
                showAskForLocalizationPermission();
            }
        }

    }

    private void toggleIsBoughtCommandExecute(final int id) {
        Item item = items.getById(id).getItem();

        if(item != null) {

            item.setBought(!item.isBought());

            if (item.isBought()) {

                //save location
                if(currentPlace != null) {

                    // get location object for the place
                    LastLocation location = locationManager.getLocationForPlace(currentPlace);
                    item.setLocation(location);
                }

                if(item.getLocation() != null) {
                    BoughtItem boughtItem = new BoughtItem(item.getName(), item.getLocation().getPlaceId(), item.getLocation().getName());
                    boughtItem.setDate(Calendar.getInstance().getTime());
                    boughtItem.setShoppingListId(listId);
                    boughtItemStorage.addItem(boughtItem);

                    /* All Items are bought -> mark all BoughtItems as syncable */
                    if(getBoughtItemsCount() - getItems().size() == 0) {
                        for(BoughtItem item1 : boughtItemStorage.getItems()) {
                            if(item1.getShoppingListId() == listId) {
                                item1.setSync(true);
                                boughtItemStorage.updateItem(item1);
                            }
                        }
                    }
                }

                Calendar now = Calendar.getInstance();
                item.setLastBought(now.getTime());

                if (item.getRepeatType() == RepeatType.Schedule && item.isRemindFromNextPurchaseOn()) {


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
                /* Delete this BoughtItem */
                for(BoughtItem boughtItem : boughtItemStorage.getItems()) {
                    if(boughtItem.getName().equals(item.getName())) {
                        boughtItemStorage.deleteSingleItem(boughtItem);
                    }
                }
            }

            listManager.saveListItem(listId, item);
        }
    }


    //endregion


    //region Private / Protected Implementation

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

    private void setPlaces(List<Place> places){
        this.places = places;
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

    private void getNearbySupermarketPlaces(int radius, int resultCount){
        SupermarketPlace.initiateSupermarketPlaceRequest(context, null, radius, resultCount);
    }

    private void notificationOfNoConnectionWithLocationPermission(){

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.localization_dialog_title),
                resourceProvider.getString(R.string.localization_no_connection_message),
                resourceProvider.getString(R.string.alert_dialog_connection_try),
                findNearbySupermarketCommand,
                resourceProvider.getString(R.string.localization_disable_localization),
                withdrawLocalizationPermissionCommand
        );

    }

    private void showAskForLocalizationPermission(){

        viewLauncher.showMessageDialogWithCheckbox(
                resourceProvider.getString(R.string.localization_dialog_title),
                resourceProvider.getString(R.string.localization_dialog_message),
                resourceProvider.getString(R.string.yes),
                restartShoppingListWithLocalization,
                null,
                null,
                resourceProvider.getString(R.string.no),
                withdrawLocalizationPermissionCommand,
                resourceProvider.getString(R.string.dont_show_this_message_again),
                false,
                doNotShowLocalizationPermissionAgainCommand,
                showLocalizationPermissionAgainCommand
        );

    }

    @SuppressWarnings("unchecked")
    private void showSelectCurrentSupermarket(final List<Place> places){

        if(places == null) {
            return;
        }

        CharSequence[] placeNames = LocationFinderHelper.getNamesFromPlaces(places, context);

        viewLauncher.showMessageDialogWithRadioButtons(
                resourceProvider.getString(R.string.localization_supermarket_select_dialog_title),
                placeNames,
                resourceProvider.getString(R.string.dialog_OK),
                new Command<Integer>() {
                    @Override
                    public void execute(Integer selectedIndex) {
                        if(selectedIndex >= 0 && selectedIndex < places.size()) {
                            currentPlace = places.get(selectedIndex);
                        }
                    }
                },
                resourceProvider.getString(R.string.dialog_retry),
                new ParameterLessCommandWrapper<Integer>(getFindNearbySupermarketCommand()),
                resourceProvider.getString(R.string.cancel),
                NullCommand.IntegerInstance
        );
    }

    private void sortItems() {
        Collections.sort(getItems(), new ItemComparator(displayHelper, getItemSortType()));
        moveBoughtItemsToEnd();
        updateOrderOfItems();
        listener.onItemSortTypeChanged();
    }


    public void showDialogLeaveShoppingMode(final int listID) {
        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.dialog_leave_shopping_mode_title),
                resourceProvider.getString(R.string.dialog_leave_shopping_mode_message),
                resourceProvider.getString(R.string.dialog_leave_shopping_mode_quit),
                new Command<Void>() {
                    @Override
                    public void execute(Void parameter) {

                        viewLauncher.showShoppingList(listID);
                    }
                },
                resourceProvider.getString(R.string.cancel),
                NullCommand.VoidInstance
        );
    }

    public void restartShoppingList(){
        viewLauncher.returnToShoppingList(listId);
    }

    public void deleteItemWithoutDialog(int id){
        listManager.deleteItem(listId, id);
    }



    //endregion
}
