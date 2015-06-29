package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Group;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.ItemParser;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeItemLoadedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeLoadedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListChangeType;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ItemIdExtractor;
import de.fau.cs.mad.kwikshop.android.viewmodel.tasks.LoadRecipeTask;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.tasks.RecipeLoadItemTask;
import de.fau.cs.mad.kwikshop.android.viewmodel.tasks.RecipeSaveItemTask;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;

public class RecipeViewModel extends ShoppingListViewModelBase {

    public interface Listener extends ShoppingListViewModelBase.Listener {

        void onQuickAddTextChanged();
    }

    private class CompositeListener implements Listener {

        @Override
        public void onQuickAddTextChanged() {
            for(Listener listener : listeners) {
                listener.onQuickAddTextChanged();
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
    private final ListStorage<Recipe> recipeStorage;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<Group> groupStorage;
    private final ItemParser itemParser;
    protected final DisplayHelper displayHelper;
    private final AutoCompletionHelper autoCompletionHelper;
    private EventBus privateBus = EventBus.builder().build();

    private int recipeId;

    private final List<Listener> listeners = new ArrayList<>();
    private final Listener listener = new CompositeListener();

    private final ObservableArrayList<Item, Integer> items = new ObservableArrayList<>(new ItemIdExtractor());
    private String quickAddText = "";

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


    @Inject
    public RecipeViewModel(ViewLauncher viewLauncher, ListStorage<Recipe> recipeStorage,
                                 SimpleStorage<Unit> unitStorage, SimpleStorage<Group> groupStorage,
                                 ItemParser itemParser, DisplayHelper displayHelper,
                                 AutoCompletionHelper autoCompletionHelper) {

        if(viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }
        if(recipeStorage == null) {
            throw new IllegalArgumentException("'recipeStorage' must not be null");
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
            throw new IllegalArgumentException("'autoCompletionHelper' mut not be null");
        }

        this.viewLauncher = viewLauncher;
        this.recipeStorage = recipeStorage;
        this.unitStorage = unitStorage;
        this.groupStorage = groupStorage;
        this.itemParser = itemParser;
        this.displayHelper = displayHelper;
        this.autoCompletionHelper = autoCompletionHelper;
    }


    /**
     * Initializes the view model. You need to call this before an instance can be used
     * @param recipeId The id of the recipe to be displayed by the view model
     */
    public void initialize(int recipeId) {

        if(!initialized) {
            this.recipeId = recipeId;
            privateBus.register(this);
            EventBus.getDefault().register(this);

            new LoadRecipeTask(this.recipeStorage, privateBus, this.recipeId).execute();

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
     * Gets the command to be executed when a recipe item in the view is selected
     */
    public Command<Integer> getSelectItemCommand() {
        return selectItemCommand;
    }

    /**
     * To be called by teh view after two items have been swapped
     */
    public void itemsSwapped(int position1, int position2) {

        Item item1 = items.get(position1);
        Item item2 = items.get(position2);

        item1.setOrder(position2);
        item2.setOrder(position1);

        new RecipeSaveItemTask(recipeStorage, recipeId, item1, item2).execute();
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(RecipeLoadedEvent event) {

        //on the main thread, update the displayed shopping list after is has been (re-) loaded

        Recipe recipe = event.getRecipe();

        if(recipe.getId() == this.recipeId) {


            this.setName(recipe.getName());

            for(Item item : event.getRecipe().getItems()) {
                updateItem(item);
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(RecipeItemLoadedEvent event) {

        // on the main thread, update an displayed item after it has been loaded

        if(event.getRecipeId() == this.recipeId) {
            updateItem(event.getItem());
        }

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(RecipeChangedEvent event) {

        if(event.getListId() == this.recipeId) {

            if(event.getChangeType() == ListChangeType.Deleted) {
                finish();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(RecipeChangedEvent event) {

        if(event.getListId() == this.recipeId) {

            if(event.getChangeType() == ListChangeType.PropertiesModified) {
                new LoadRecipeTask(recipeStorage, privateBus, recipeId).execute();

            }

            //other change types should already be covered by other event handlers

        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(ItemChangedEvent event) {

        if(event.getListType() == ListType.Recipe && event.getListId() == this.recipeId) {

            switch (event.getChangeType()) {

                case  Added:
                case PropertiesModified:
                    new RecipeLoadItemTask(recipeStorage, privateBus, recipeId, event.getItemId()).execute();
                    break;

                case  Deleted:
                    // ignore (handled on background thread)
                    break;

            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent event) {

        if(event.getListType() == ListType.Recipe && event.getListId() == this.recipeId) {

            switch (event.getChangeType()) {

                case  Added:
                case PropertiesModified:
                    // ignore (handled on background thread)
                    break;
                case  Deleted:
                    items.removeById(event.getItemId());
                    break;

            }
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

    private synchronized void quickAddCommandExecute() {

        ensureIsInitialized();
        getItems().enableEvents();

        final String text = getQuickAddText();
        //reset quick add text
        setQuickAddText("");


        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void[] params) {

                if(!StringHelper.isNullOrWhiteSpace(text)) {

                    Item newItem = new Item();
                    newItem.setName(text);
                    newItem.setUnit(unitStorage.getDefaultValue());
                    newItem = itemParser.parseAmountAndUnit(newItem);
                    newItem.setGroup(groupStorage.getDefaultValue());

                    Recipe recipe = recipeStorage.loadList(recipeId);
                    recipe.addItem(newItem);

                    recipeStorage.saveList(recipe);

                    autoCompletionHelper.offerName(newItem.getName());

                    EventBus.getDefault().post(new RecipeChangedEvent(ListChangeType.ItemsAdded, recipe.getId()));
                    EventBus.getDefault().post(new ItemChangedEvent(ListType.Recipe, ItemChangeType.Added, recipe.getId(), newItem.getId()));
                }
                return null;
            }
        }.execute();


    }

    private void addItemCommandExecute() {
        ensureIsInitialized();
        viewLauncher.RecipeShowItemDetailsView(this.recipeId);
    }

    private void selectItemCommandExecute(int itemId) {
        ensureIsInitialized();
        viewLauncher.RecipeShowItemDetailsView(this.recipeId, itemId);
    }


    private void ensureIsInitialized() {
        if(!initialized) {
            throw new UnsupportedOperationException("You need to call initialized before view model can perform commands");
        }
    }

    private void updateItem(Item item) {

        int id = item.getId();

        items.setOrAddById(item);

    }
}

