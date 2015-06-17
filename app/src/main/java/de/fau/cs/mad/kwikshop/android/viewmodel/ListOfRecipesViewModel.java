package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.os.AsyncTask;

import java.util.Collection;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.RecipeStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeLoadedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.LoadRecipeTask;
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
    private final RecipeStorage recipeStorage;
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

    private final Command selectRecipeDetailsCommand = new Command<Integer>() {
        @Override
        public void execute(Integer recipeId) {
            viewLauncher.showRecipeDetailsView(recipeId);
        }
    };


    @Inject
    public ListOfRecipesViewModel(ViewLauncher viewLauncher, RecipeStorage recipeStorage) {

        this.viewLauncher = viewLauncher;
        this.recipeStorage = recipeStorage;

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
