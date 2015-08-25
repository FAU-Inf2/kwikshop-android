package de.fau.cs.mad.kwikshop.android.viewmodel;


import android.content.Context;
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
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ItemSortType;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.*;
import de.fau.cs.mad.kwikshop.common.*;
import de.fau.cs.mad.kwikshop.common.ItemViewModel;

public class ShoppingListViewModel extends ListViewModel<ShoppingList> {

    private Context context;
    private int tmp_item_id;

    private final ResourceProvider resourceProvider;
    private final RegularlyRepeatHelper repeatHelper;
    private final ListManager<Recipe> recipeManager;


    private final ObservableArrayList<ItemViewModel, Integer> boughtItems = new ObservableArrayList<>(new ItemIdExtractor());
    private ItemSortType itemSortType = ItemSortType.MANUAL;

    private final Command<Integer> toggleIsBoughtCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) { toggleIsBoughtCommandExecute(parameter); }
    };
    private final Command<Integer> deleteItemCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) { deleteItemCommandExecute(parameter);}
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
                                 RegularlyRepeatHelper repeatHelper) {


        super(viewLauncher, shoppingListManager, unitStorage, groupStorage, itemParser, displayHelper,
                autoCompletionHelper, locationFinderHelper);

        if(context == null) throw new ArgumentNullException("context");

        if (resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if(repeatHelper == null) {
            throw new ArgumentNullException("repeatHelper");
        }

        if(recipeManager == null){
            throw new ArgumentNullException("recipeManager");
        }


        this.context = context;
        this.resourceProvider = resourceProvider;
        this.repeatHelper = repeatHelper;
        this.recipeManager = recipeManager;
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


    @Override
    public void itemsSwapped(int position1, int position2) {
        ItemViewModel item1 = items.get(position1);
        ItemViewModel item2 = items.get(position2);

        item1.setOrder(position1);
        item2.setOrder(position2);

        listManager.saveListItem(listId, item1);
        listManager.saveListItem(listId, item2);
    }



    public void deleteItem(int itemId, String title, String message, String positiveString, String negativeString, String checkBoxMessage){
        this.tmp_item_id = itemId;
        if(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG, true, context))
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

        if(event.getListType() == ListType.ShoppingList && event.getListId() == this.listId) {

            switch (event.getChangeType()) {

                case Added:
                    ItemViewModel item = listManager.getListItem(listId, event.getItemId());
                    updateItem(item);
                    sortItems();
                    updateOrderOfItems();
                    break;
                case PropertiesModified:
                    ItemViewModel item1 = listManager.getListItem(listId, event.getItemId());
                    updateItem(item1);
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

        List<ItemViewModel> changedItems = new LinkedList<>();

        for(ItemViewModel item : list.getItems()) {
            if(item.isBought() != isBoughtNew) {
                item.setBought(isBoughtNew);
                changedItems.add(item);
            }
        }

        for(ItemViewModel item : changedItems) {
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

        for(ItemViewModel item : shoppingList.getItems()) {
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
        ItemViewModel item = items.getById(id);

        if(item != null) {

            item.setBought(!item.isBought());

            if (item.isBought()) {
                if (item.getRepeatType() == RepeatType.Schedule && item.isRemindFromNextPurchaseOn() && item.getLastBought() == null) {
                    Calendar now = Calendar.getInstance();
                    item.setLastBought(now.getTime());

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
            ItemViewModel item = (ItemViewModel)li.previous();
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

    public void changeCheckBoxesVisibility(){
        ShoppingList shoppingList = listManager.getList(this.listId);

        for(ItemViewModel item : shoppingList.getItems()) {
            updateItem(item);
        }
    }
    private void updateItem(de.fau.cs.mad.kwikshop.common.ItemViewModel item) {
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
