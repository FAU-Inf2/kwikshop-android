package de.fau.cs.mad.kwikshop.android.viewmodel;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ListIdExtractor;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewModelBase;
import de.greenrobot.event.EventBus;

public class ListOfRecipesViewModel extends ViewModelBase {

    // listener interface
    public interface Listener extends ViewModelBase.Listener {

        void onRecipeChanged(final ObservableArrayList<Recipe, Integer> oldValue,
                             final ObservableArrayList<Recipe, Integer> newValue);
    }

    // infrastructure references
    private final ViewLauncher viewLauncher;
    private final ListManager<Recipe> recipeManager;
    private final EventBus privateBus = EventBus.builder().build();


    private Listener listener;

    // backing fields for properties
    private ObservableArrayList<Recipe, Integer> recipes;
    private final Command addRecipeCommand = new Command<Object>() {
        @Override
        public void execute(Object parameter) {
            viewLauncher.showAddRecipeView();
        }
    };
    private final Command<Integer> selectRecipeCommand = new Command<Integer>() {
        @Override
        public void execute(Integer recipeId) {
            viewLauncher.showRecipe(recipeId);
        }
    };

    private final Command<Integer> selectRecipeDetailsCommand = new Command<Integer>() {
        @Override
        public void execute(Integer recipeId) {
            viewLauncher.showRecipeDetailsView(recipeId);
        }
    };


    @Inject
    public ListOfRecipesViewModel(ViewLauncher viewLauncher, ListManager<Recipe> recipeManager) {

        if(viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }

        if(recipeManager == null) {
            throw new IllegalArgumentException("'recipeManager' must not be null");
        }

        this.viewLauncher = viewLauncher;
        this.recipeManager = recipeManager;

        setRecipes(new ObservableArrayList<>(new ObservableArrayList.IdExtractor<Recipe, Integer>() {
            @Override
            public Integer getId(Recipe object) {
                return object.getId();
            }
        }));

        EventBus.getDefault().register(this);
        privateBus.register(this);


        this.recipes = new ObservableArrayList<>(new ListIdExtractor<Recipe>(), recipeManager.getLists());
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

    public Command getAddRecipeCommand() {
        return this.addRecipeCommand;
    }

    public Command<Integer> getSelectRecipeCommand() {
        return this.selectRecipeCommand;
    }

    public Command<Integer> getSelectRecipeDetailsCommand() {
        return this.selectRecipeDetailsCommand;
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
                reloadRecipe(ev.getListId());
                break;

            default:
                break;
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent ev) {

        if (ev.getListType() == ListType.Recipe) {
            switch (ev.getChangeType()) {

                case Deleted:
                case Added:
                    reloadRecipe(ev.getListId());
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


    private void reloadRecipe(int listId) {
        Recipe recipe = recipeManager.getList(listId);
        synchronized (this) {

            int index = recipes.indexOfById(recipe.getId());
            if (index >= 0) {
                recipes.set(index, recipe);
            } else {
                recipes.add(recipe);
            }
        }
    }



}
