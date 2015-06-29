package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.os.AsyncTask;

import java.util.Collection;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.RecipeStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeLoadedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewModelBase;
import de.fau.cs.mad.kwikshop.android.viewmodel.tasks.LoadRecipeTask;
import de.greenrobot.event.EventBus;

public class AddRecipeToShoppingListViewModel extends ViewModelBase {

    // listener interface
    public interface Listener extends ViewModelBase.Listener {

        void onRecipeChanged(final ObservableArrayList<Recipe, Integer> oldValue,
                             final ObservableArrayList<Recipe, Integer> newValue);
    }

    // infrastructure references
    private final ViewLauncher viewLauncher;
    private final RecipeStorage recipeStorage;
    private final EventBus privateBus = EventBus.builder().build();
    private final ListStorage<ShoppingList> listStorage;


    private Listener listener;

    // backing fields for properties
    private ObservableArrayList<Recipe, Integer> recipes;
    private final Command<Integer> selectRecipeCommand = new Command<Integer>() {
        @Override
        public void execute(Integer recipeId) {

            ShoppingList shoppingList = listStorage.loadList(shoppingListId);
            Recipe recipe = recipeStorage.loadList(recipeId);
            for(Item item : recipe.getItems()){
                Item newItem = new Item();
                newItem.setName(item.getName());
                newItem.setAmount(item.getAmount());
                newItem.setBrand(item.getBrand());
                newItem.setComment(item.getComment());
                newItem.setHighlight(item.isHighlight());
                newItem.setGroup(item.getGroup());
                newItem.setUnit(item.getUnit());
                shoppingList.addItem(newItem);

                EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.ItemsAdded, shoppingList.getId()));
                EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.Added, shoppingList.getId(), newItem.getId()));
            }
            listStorage.saveList(shoppingList);
            viewLauncher.showShoppingList(shoppingListId);
        }
    };

    private int shoppingListId;
    private boolean initialized = false;

    @Inject
    public AddRecipeToShoppingListViewModel(ViewLauncher viewLauncher, RecipeStorage recipeStorage,
                                            ListStorage<ShoppingList> listStorage) {

        this.viewLauncher = viewLauncher;
        this.recipeStorage = recipeStorage;
        this.listStorage = listStorage;

        setRecipes(new ObservableArrayList<>(new ObservableArrayList.IdExtractor<Recipe, Integer>() {
            @Override
            public Integer getId(Recipe object) {
                return object.getId();
            }
        }));

        EventBus.getDefault().register(this);
        privateBus.register(this);

        new LoadRecipeTask(recipeStorage, privateBus).execute();
    }

    public void initialize(int shoppingListId) {
        if(!initialized) {
            this.shoppingListId = shoppingListId;
            initialized = true;
        }
    }



    public void setListener(final Listener listener) {
        this.listener = listener;
    }


    // Getters / Setters

    public ObservableArrayList<Recipe, Integer> getRecipes() {
        return this.recipes;
    }

    private void setRecipes(final ObservableArrayList<Recipe, Integer> value) {
        if (value != recipes) {
            ObservableArrayList<Recipe, Integer> oldValue = this.recipes;
            this.recipes = value;
            if (listener != null) {
                listener.onRecipeChanged(oldValue, value);
            }
        }
    }

    public Command<Integer> getSelectRecipeCommand() {
        return this.selectRecipeCommand;
    }

    @Override
    protected Listener getListener() {
        return listener;
    }

    public void onEventMainThread(RecipeChangedEvent ev) {

        switch (ev.getChangeType()) {

            case Deleted:
                recipes.removeById(ev.getListId());
                break;

            case PropertiesModified:
            case ItemsAdded:
            case ItemsRemoved:
            case Added:
                loadRecipeAsync(ev.getListId());
                break;

            default:
                break;
        }
    }

    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
    }


    private void loadRecipeAsync(int id) {

        AsyncTask<Object, Object, Collection<Recipe>> task = new LoadRecipeTask(recipeStorage, privateBus, id);
        task.execute();
    }

    public void onEventMainThread(RecipeLoadedEvent event) {

        Recipe loadedRecipe = event.getRecipe();

        synchronized (this) {

            int index = recipes.indexOfById(loadedRecipe.getId());
            if (index >= 0) {
                recipes.set(index, loadedRecipe);
            } else {
                recipes.add(loadedRecipe);
            }
        }

    }


}
