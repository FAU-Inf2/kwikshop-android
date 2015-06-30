package de.fau.cs.mad.kwikshop.android.viewmodel;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.DialogFinishedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ListIdExtractor;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewModelBase;
import de.greenrobot.event.EventBus;

public class AddRecipeToShoppingListViewModel extends ViewModelBase {

    // listener interface
    public interface Listener extends ViewModelBase.Listener {

        void onRecipeChanged(final ObservableArrayList<Recipe, Integer> oldValue,
                             final ObservableArrayList<Recipe, Integer> newValue);
    }

    // infrastructure references
    private final ViewLauncher viewLauncher;
    private final ListManager<Recipe> recipeManager;
    private final ListManager<ShoppingList> shoppingListManager;

    private Listener listener;

    // backing fields for properties
    private ObservableArrayList<Recipe, Integer> recipes;
    private final Command<Integer> selectRecipeCommand = new Command<Integer>() {
        @Override
        public void execute(Integer recipeId) {

            Recipe recipe = recipeManager.getList(recipeId);
            scaleFactor = recipe.getScaleFactor();
            scaleName = recipe.getScaleName();

            viewLauncher.showAddRecipeDialog(recipe);

/*
            for(Item item : recipe.getItems()){
                Item newItem = new Item();
                newItem.setName(item.getName());
                newItem.setAmount(item.getAmount());
                newItem.setBrand(item.getBrand());
                newItem.setComment(item.getComment());
                newItem.setHighlight(item.isHighlight());
                newItem.setGroup(item.getGroup());
                newItem.setUnit(item.getUnit());

                shoppingListManager.addListItem(shoppingListId, newItem);
            }
            viewLauncher.showShoppingList(shoppingListId);
*/
        }
    };

    private int shoppingListId;
    private boolean initialized = false;
    private int scaleFactor;
    private String scaleName;
    //value after scaling
    private double scaledValue;

    @Inject
    public AddRecipeToShoppingListViewModel(ViewLauncher viewLauncher, ListManager<Recipe> recipeManager,
                                            ListManager<ShoppingList> shoppingListManager) {

        this.viewLauncher = viewLauncher;
        this.recipeManager = recipeManager;
        this.shoppingListManager = shoppingListManager;

        setRecipes(new ObservableArrayList<>(new ObservableArrayList.IdExtractor<Recipe, Integer>() {
            @Override
            public Integer getId(Recipe object) {
                return object.getId();
            }
        }));

        EventBus.getDefault().register(this);

        this.recipes = new ObservableArrayList<>(new ListIdExtractor<Recipe>(), recipeManager.getLists());
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

    public int getScaleFactor(){
        return scaleFactor;
    }

    public String getScaleName(){
        return scaleName;
    }

    public void setScaleFactor(int scaleFactor){
        this.scaleFactor = scaleFactor;
    }

    public void setScaleName(String scaleName){
        this.scaleName = scaleName;
    }

    public void setScaledValue(double scaledValue){
        this.scaledValue = scaledValue;
    }


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

    @SuppressWarnings("unused")
    public void onEventMainThread(RecipeChangedEvent ev) {

        switch (ev.getChangeType()) {

            case Deleted:
                recipes.removeById(ev.getListId());
                break;

            case PropertiesModified:
            case Added:
                loadRecipe(ev.getListId());
                break;

            default:
                break;
        }
    }

    public void onEvent(DialogFinishedEvent event){
        Recipe recipe = event.getRecipe();
        for(Item item : recipe.getItems()){
            Item newItem = new Item();
            newItem.setName(item.getName());
            newItem.setAmount((int)event.getScaledValue() * item.getAmount());
            newItem.setBrand(item.getBrand());
            newItem.setComment(item.getComment());
            newItem.setHighlight(item.isHighlight());
            newItem.setGroup(item.getGroup());
            newItem.setUnit(item.getUnit());

            shoppingListManager.addListItem(shoppingListId, newItem);
        }
        viewLauncher.showShoppingList(shoppingListId);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent ev) {

        if (ev.getListType() == ListType.Recipe) {

            switch (ev.getChangeType()) {

                case Added:
                case Deleted:
                    loadRecipe(ev.getListId());
                    break;

                default:
                    break;
            }
        }

    }

    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
    }


    private void loadRecipe(int id) {

        Recipe loadedRecipe = recipeManager.getList(id);
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
