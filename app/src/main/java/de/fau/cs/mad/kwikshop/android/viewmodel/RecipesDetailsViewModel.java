package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListChangeType;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;

public class RecipesDetailsViewModel extends ShoppingListViewModelBase {

    //listener interface
    public interface Listener extends ShoppingListViewModelBase.Listener {

    }

    private class CompositeListener implements Listener {

        @Override
        public void onNameChanged(String value) {
            for (Listener l : listeners) {
                l.onNameChanged(value);
            }
        }

        @Override
        public void onFinish() {
            for (Listener l : listeners) {
                l.onFinish();
            }
        }
    }


    //other fields
    private List<Listener> listeners = new LinkedList<>();
    private Listener compositeListener = new CompositeListener();

    private final Context context;
    private final ListStorage<Recipe> recipeStorage;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;

    private int recipeId;
    private boolean isNewRecipe;
    private Recipe recipe;
    private int scaleFactor;
    private String scaleName;

    // backing fields for properties
    private Command saveCommand;
    private Command cancelCommand;
    private Command deleteCommand;

    /**
     * Initializes a new instance of RecipesDetailsViewModel for the specified recipe
     * (will modify the recipe on save)
     */
    @Inject
    public RecipesDetailsViewModel(final Context context, final ViewLauncher viewLauncher,
                                        final ResourceProvider resourceProvider,
                                        final ListStorage<Recipe> recipeStorage) {

        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        if (viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }

        if (resourceProvider == null) {
            throw new IllegalArgumentException("'resourceProvider' must not be null");
        }

        if (recipeStorage == null) {
            throw new IllegalArgumentException("'recipeStorage' must not be null");
        }

        this.context = context;
        this.viewLauncher = viewLauncher;
        this.resourceProvider = resourceProvider;
        this.recipeStorage = recipeStorage;

    }

    public void initialize() {
        initialize(-1);
    }

    public void initialize(int recipeId) {
        this.recipeId = recipeId;
        this.isNewRecipe = recipeId == -1;

        this.saveCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                saveCommandExecute();
            }
        };
        this.cancelCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                cancelCommandExecute();
            }
        };
        this.deleteCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                deleteCommandExecute();
            }
        };

        setUp();
    }

    public void addListener(Listener value) {
        this.listeners.add(value);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }


    // Getters / Setters

    public Command getSaveCommand() {
        return saveCommand;
    }

    public Command getCancelCommand() {
        return cancelCommand;
    }

    public Command getDeleteCommand() {
        return this.deleteCommand;
    }

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


    @Override
    public void setName(String value) {
        super.setName(value);
        getSaveCommand().setCanExecute(getName() != null && getName().trim().length() > 0);
    }

    public boolean getIsNewRecipe() {
        return isNewRecipe;
    }


    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected ShoppingListViewModelBase.Listener getListener() {
        return compositeListener;
    }


    private void setUp() {

        this.saveCommand.setIsAvailable(true);
        this.cancelCommand.setIsAvailable(true);

        if (isNewRecipe) {

            this.deleteCommand.setIsAvailable(false);
            setName("");
            setScaleFactor(1);
            setScaleName(context.getString(R.string.recipe_scaleName_person));

        } else {

            this.deleteCommand.setIsAvailable(true);

            //TODO: handle exception when list is not found
            this.recipe = recipeStorage.loadList(recipeId);
            setScaleFactor(recipe.getScaleFactor());
            setScaleName(recipe.getScaleName());
            setName(recipe.getName());
        }

    }

    private void saveCommandExecute() {
        if (isNewRecipe) {
            this.recipeId = recipeStorage.createList();
            recipe = recipeStorage.loadList(recipeId);
        }

        recipe.setName(this.getName());
        recipe.setScaleFactor(scaleFactor);
        recipe.setScaleName(scaleName);

        recipeStorage.saveList(recipe);

        ListChangeType changeType = isNewRecipe
                ? ListChangeType.Added
                : ListChangeType.PropertiesModified;

        EventBus.getDefault().post(new RecipeChangedEvent(changeType, recipe.getId()));

        // if we just created a recipe, open it right away,
        // when a existing recipe was edited, just close the current view and go back to
        // whatever the previous screen was
        if (isNewRecipe) {
            viewLauncher.showRecipe(recipeId);
        }


        finish();

    }

    private void cancelCommandExecute() {
        finish();
    }

    private void deleteCommandExecute() {

        if (isNewRecipe) {
            throw new UnsupportedOperationException();
        }

        viewLauncher.showYesNoDialog(
                resourceProvider.getString(R.string.deleteRecipe_DialogTitle),
                resourceProvider.getString(R.string.deleteRecipe_DialogText),
                new Command() {
                    @Override
                    public void execute(Object parameter) {

                        recipeStorage.deleteList(recipeId);
                        EventBus.getDefault().post(new RecipeChangedEvent(ListChangeType.Deleted, recipeId));
                        finish();
                    }
                },
                NullCommand.Instance);
    }

}
