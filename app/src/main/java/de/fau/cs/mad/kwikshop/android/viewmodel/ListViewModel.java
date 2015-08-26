package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.ItemParser;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ItemIdExtractor;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;

public abstract class ListViewModel<TList extends DomainListObject> extends ListViewModelBase {

    public interface Listener extends ListViewModelBase.Listener {
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

    protected final List<Listener> listeners = new ArrayList<>();
    protected final Listener listener = new CompositeListener();


    private boolean initialized = false;


    protected final ViewLauncher viewLauncher;
    protected final ListManager<TList> listManager;
    protected final SimpleStorage<Unit> unitStorage;
    protected final SimpleStorage<Group> groupStorage;
    protected final ItemParser itemParser;
    protected final DisplayHelper displayHelper;
    protected final AutoCompletionHelper autoCompletionHelper;
    protected ItemMerger itemMerger;
    protected LocationFinderHelper locationFinderHelper;

    public ArrayAdapter<String> unitAdapter;
    public ArrayAdapter<String> groupAdapter;
    public MultiAutoCompleteTextView qAddUnit;
    public MultiAutoCompleteTextView qAddGroup;
    protected int listId;
    private int quickRemoveUnitIndex = -1;
    private int quickRemoveGroupIndex = -1;
    protected final ObservableArrayList<ItemViewModel, Integer> items = new ObservableArrayList<>(new ItemIdExtractor());
    private String quickAddText = "";
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
    private final Command<Void> quickAddUnitCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {

            quickAddUnitsCommandExecute(unitAdapter, qAddUnit);

        }
    };
    private final Command<Void> quickAddGroupCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {

            quickAddGroupsCommandExecute(groupAdapter, qAddGroup);

        }
    };
    private final Command<Void> quickRemoveGroupCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {

            quickRemoveGroupCommandExecute(groupAdapter);

        }
    };
    private final Command<Void> quickRemoveUnitCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {

            quickRemoveUnitCommandExecute(unitAdapter);

        }
    };
    private final Command<Integer> selectItemCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) { selectItemCommandExecute(parameter); }
    };


    public ListViewModel(ViewLauncher viewLauncher, ListManager<TList> listManager,
                                 SimpleStorage<Unit> unitStorage, SimpleStorage<Group> groupStorage,
                                 ItemParser itemParser, DisplayHelper displayHelper,
                                 AutoCompletionHelper autoCompletionHelper, LocationFinderHelper locationFinderHelper) {


        if(viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }
        if(listManager == null) {
            throw new IllegalArgumentException("'listManager' must not be null");
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

        if(locationFinderHelper == null) {
            throw new IllegalArgumentException("'locationFinderHelper' must not be null");
        }

        this.viewLauncher = viewLauncher;
        this.listManager = listManager;
        this.unitStorage = unitStorage;
        this.groupStorage = groupStorage;
        this.itemParser = itemParser;
        this.displayHelper = displayHelper;
        this.autoCompletionHelper = autoCompletionHelper;
        this.itemMerger = new ItemMerger<>(listManager);
        this.locationFinderHelper = locationFinderHelper;
    }


    /**
     * Adds a listener to be notified when changes in the view model occcur
     */
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    /**
     * Initializes the view model. You need to call this before an instance can be used
     * @param listId The id of the shopping list to be displayed by the view model
     */
    public void initialize(int listId) {

        if(!initialized) {
            this.listId = listId;
            EventBus.getDefault().register(this);

            loadList();
            initialized = true;
        }
    }


    /**
     * Gets the shopping list items
     */
    public ObservableArrayList<ItemViewModel, Integer> getItems() {
        return items;
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
    public Command<Void> getQuickAddUnitCommand(ArrayAdapter<String> adapter, MultiAutoCompleteTextView qAddUnit) { this.unitAdapter = adapter; this.qAddUnit = qAddUnit; return quickAddUnitCommand;}
    public Command<Void> getQuickAddGroupCommand(ArrayAdapter<String> adapter, MultiAutoCompleteTextView qAddGroup) { this.groupAdapter = adapter; this.qAddGroup = qAddGroup; return quickAddGroupCommand;}
    public Command<Void> getQuickRemoveGroupCommand(ArrayAdapter<String> adapter, int index){this.groupAdapter = adapter; this.quickRemoveGroupIndex = index; return quickRemoveGroupCommand;}
    public Command<Void> getQuickRemoveUnitCommand(ArrayAdapter<String> adapter, int index){this.unitAdapter = adapter; this.quickRemoveUnitIndex = index; return quickRemoveUnitCommand;}
    /**
     * Gets the command to be executed when a shopping list item in the view is selected
     */
    public Command<Integer> getSelectItemCommand() {
        return selectItemCommand;
    }

    /**
     * Gets the current value for the quick-add text field
     */
    public String getQuickAddText() {
        return quickAddText;
    }

    /**
     * Gets the current index for the quick-remove unit
     */
    public int getQuickRemoveUnitIndex() {
        return quickRemoveUnitIndex;
    }
    /**
     * Gets the current index for the quick-remove group
     */
    public int getQuickRemoveGroupIndex() {
        return quickRemoveGroupIndex;
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
     * Sets the value of the quick-remove index field
     */
    public void setQuickRemoveUnitIndex(int value) {
            quickRemoveUnitIndex = value;
    }
    /**
     * Sets the value of the quick-remove index field
     */
    public void setQuickRemoveGroupIndex(int value) {
        quickRemoveGroupIndex = value;
    }
    /**
     * To be called by the view after two items have been swapped
     */
    public void itemsSwapped(int position1, int position2) {

      //  setLocationOnItemSwapped(position1);

        Item item1 = items.get(position1).getItem();
        Item item2 = items.get(position2).getItem();

        item1.setOrder(position2);
        item2.setOrder(position1);


        listManager.saveListItem(listId, item1);
        listManager.saveListItem(listId, item2);

    }

    public void setLocationOnItemSwapped(int pos){

        Item item = items.get(pos).getItem();

        item.setLocation(locationFinderHelper.getLastLocation());
        listManager.saveListItem(listId, item);


    }

    /**
     * Updates the current order of Items
     */
    public void updateOrderOfItems() {
        int i = 0;
        for(ItemViewModel item: getItems()) {
            item.getItem().setOrder(i);

            // Break on first bought Item - we don't care about their order.
            if(item.getItem().isBought())
                break;

            i++;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected Listener getListener() {
        return this.listener;
    }



    protected void ensureIsInitialized() {
        if(!initialized) {
            throw new UnsupportedOperationException("You need to call initialize() before view model can perform commands");
        }
    }

    protected synchronized void quickAddCommandExecute() {

        ensureIsInitialized();
        getItems().enableEvents();

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
                        Group group = autoCompletionHelper.getGroup(StringHelper.removeSpacesAtEndOfWord(text));
                        if (group == null) {
                            newItem.setGroup(groupStorage.getDefaultValue());
                        } else {
                            newItem.setGroup(group);
                        }
                        if(!itemMerger.mergeItem(listId, newItem))
                            listManager.addListItem(listId, newItem);

                        autoCompletionHelper.offerName(newItem.getName());
                    }

                }
                return null;
            }
        }.execute();


    }

    protected void quickAddUnitsCommandExecute(ArrayAdapter<String> adapter, MultiAutoCompleteTextView qAddUnit){

        final String text = getQuickAddText();
        //reset quick add text

                if (!StringHelper.isNullOrWhiteSpace(text)) {
                    Unit newUnit = new Unit();
                    newUnit.setName(text);
                    unitStorage.addItem(newUnit);

                    autoCompletionHelper.offerName(newUnit.getName());

                    viewLauncher.notifyUnitSpinnerChange(adapter);
                }

        qAddUnit.setText("");
    }
    protected void quickAddGroupsCommandExecute(ArrayAdapter<String> adapter, MultiAutoCompleteTextView qAddGroup){

        final String text = getQuickAddText();
        //reset quick add text
        setQuickAddText("");


        if (!StringHelper.isNullOrWhiteSpace(text)) {
            Group newGroup = new Group();
            newGroup.setName(text);
            groupStorage.addItem(newGroup);
            autoCompletionHelper.offerName(newGroup.getName());
            viewLauncher.notifyGroupSpinnerChange(adapter);
        }

        qAddGroup.setText("");

    }

    protected void quickRemoveUnitCommandExecute(ArrayAdapter<String> adapter){
        if (this.quickRemoveUnitIndex != -1) {
            List<Unit> units;
            //get units from the database and sort them by name
            units = unitStorage.getItems();
            Collections.sort(units, new Comparator<Unit>() {
                @Override
                public int compare(Unit lhs, Unit rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            Unit u = units.get(quickRemoveUnitIndex);
            unitStorage.deleteSingleItem(u);
            viewLauncher.notifyUnitSpinnerChange(adapter);

        }
    }
    protected void quickRemoveGroupCommandExecute(ArrayAdapter<String> adapter){
        if (this.quickRemoveGroupIndex != -1) {
            List<Group> groups;
            //get units from the database and sort them by name
            groups = groupStorage.getItems();

            Group g = groups.get(quickRemoveGroupIndex);
            groupStorage.deleteSingleItem(g);
            viewLauncher.notifyGroupSpinnerChange(adapter);


        }
    }

    final Command<Void> cancelProgressDialogCommand =  new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            viewLauncher.showShoppingList(listId);
        }
    };

    public ViewLauncher getViewLauncher(){
        return viewLauncher;
    }

    public Command<Void> getCancelProgressDialogCommand() { return cancelProgressDialogCommand; }

    protected abstract void loadList();

    protected abstract void addItemCommandExecute();

    protected abstract void selectItemCommandExecute(int parameter);

}
