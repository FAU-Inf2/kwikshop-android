package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Group;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.common.interfaces.DomainListObject;
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
    protected final ItemMerger itemMerger;

    protected int listId;
    protected final ObservableArrayList<Item, Integer> items = new ObservableArrayList<>(new ItemIdExtractor());
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
    private final Command<Integer> selectItemCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) { selectItemCommandExecute(parameter); }
    };


    public ListViewModel(ViewLauncher viewLauncher, ListManager<TList> listManager,
                                 SimpleStorage<Unit> unitStorage, SimpleStorage<Group> groupStorage,
                                 ItemParser itemParser, DisplayHelper displayHelper,
                                 AutoCompletionHelper autoCompletionHelper) {


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

        this.viewLauncher = viewLauncher;
        this.listManager = listManager;
        this.unitStorage = unitStorage;
        this.groupStorage = groupStorage;
        this.itemParser = itemParser;
        this.displayHelper = displayHelper;
        this.autoCompletionHelper = autoCompletionHelper;
        this.itemMerger = new ItemMerger(listManager);
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
     * Gets the shopping list items that have not yet been bought
     */
    public ObservableArrayList<Item, Integer> getItems() {
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
     * To be called by teh view after two items have been swapped
     */
    public void itemsSwapped(int position1, int position2) {

        Item item1 = items.get(position1);
        Item item2 = items.get(position2);

        item1.setOrder(position2);
        item2.setOrder(position1);

        listManager.saveListItem(listId, item1);
        listManager.saveListItem(listId, item2);
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
                        if(!itemMerger.mergeItem(listId, newItem)) {
                            listManager.addListItem(listId, newItem);
                        }
                        autoCompletionHelper.offerName(newItem.getName());
                    }

                }
                return null;
            }
        }.execute();


    }


    protected abstract void loadList();

    protected abstract void addItemCommandExecute();

    protected abstract void selectItemCommandExecute(int parameter);

}
