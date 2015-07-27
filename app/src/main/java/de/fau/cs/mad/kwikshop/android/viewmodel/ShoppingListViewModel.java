package de.fau.cs.mad.kwikshop.android.viewmodel;


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
import de.fau.cs.mad.kwikshop.android.util.ItemComparator;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ItemSortType;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.*;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

public class ShoppingListViewModel extends ListViewModel<ShoppingList> {


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
                autoCompletionHelper, locationFinderHelper);


        if (resourceProvider == null) {
            throw new IllegalArgumentException("'resourceProvider' must not be null");
        }


        this.resourceProvider = resourceProvider;
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
        //updateOrderOfItems();
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


    @Override
    public void itemsSwapped(int position1, int position2) {
        Item item1 = items.get(position1);
        Item item2 = items.get(position2);

        item1.setOrder(position1);
        item2.setOrder(position2);

        listManager.saveListItem(listId, item1);
        listManager.saveListItem(listId, item2);
    }


    public void setLocationOnItemBought(final int id, final String googleBrowserApiKey){

        AsyncTask<Void, Void, Void> locationAsyncTask =  new AsyncTask<Void, Void, Void>() {

             Item item;
             LastLocation location;
             List<Place> places;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                item = items.getById(id);
                if(item == null) {
                    item = boughtItems.getById(id);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {

                // get location
                location = locationFinderHelper.getLastLocation();

                // try to get information about a location
                try {
                    GooglePlaces client = new GooglePlaces(googleBrowserApiKey);
                    places = client.getNearbyPlaces(location.getLatitude(), location.getLongitude(), 200, 1, Param.name("types").value("grocery_or_supermarket"));
                } catch (Exception e) {
                    //  e.printStackTrace();
                }

                // place was found
                if (places != null) {
                    location.setName(places.get(0).getName());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                item.setLocation(location);
                listManager.saveListItem(listId, item);
            }
        };

        if(listManager.getListItem(listId, id).getLocation() == null) {
            locationAsyncTask.execute();
        }

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

                case Added: // TODO: New Items are moved to the top of the list, maybe we want to change this
                    Item item = listManager.getListItem(listId, event.getItemId());
                    updateItem(item);
                    sortItems();
                    //updateOrderOfItems();
                    break;
                case PropertiesModified:
                    Item item1 = listManager.getListItem(listId, event.getItemId());
                    updateItem(item1);
                    //updateOrderOfItems();
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
            updateItem(item);
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

    private void toggleIsBoughtCommandExecute(final int id) {
        Item item = items.getById(id);

        if(item != null) {

            item.setBought(!item.isBought());

            if (item.isBought()) {
                if (item.isRegularlyRepeatItem() && item.isRemindFromNextPurchaseOn() && item.getLastBought() == null) {
                    Calendar now = Calendar.getInstance();
                    item.setLastBought(now.getTime());
                    RegularlyRepeatHelper repeatHelper = RegularlyRepeatHelper.getInstance();

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
            }

            listManager.saveListItem(listId, item);
        }
    }

    private void deleteItemCommandExecute(final int id) {
        listManager.deleteItem(listId, id);
    }

    public int getBoughtItemsCount() {
        ListIterator li = items.listIterator(items.size());
        int i = 0;

        while(li.hasPrevious()) {
            Item item = (Item)li.previous();
            if(item.isBought())
                i++;
            else
                break;
        }
        return i;
    }

    public void moveBoughtItemsToEnd() {
        Collections.sort(getItems(), new ItemComparator(displayHelper, ItemSortType.BOUGHTITEMS));
    }

    private void updateItem(Item item) {
        if(item.isBought()) { // Add bought items at the end of the list
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


}
