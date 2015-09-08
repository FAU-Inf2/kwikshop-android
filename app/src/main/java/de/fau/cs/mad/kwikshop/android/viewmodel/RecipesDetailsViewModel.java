package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;

import javax.inject.Inject;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class RecipesDetailsViewModel extends ListDetailsViewModel<Recipe> {

    private Recipe recipe;
    private int scaleFactor;
    private int oldScaleFactor;
    private String scaleName;
    private final Context context;

    /**
     * Initializes a new instance of RecipesDetailsViewModel for the specified recipe
     * (will modify the recipe on save)
     */
    @Inject
    public RecipesDetailsViewModel(final Context context,
                                   final ViewLauncher viewLauncher,
                                   final ResourceProvider resourceProvider,
                                   final ListManager<Recipe> listManager) {

        super(viewLauncher, resourceProvider, listManager);

        if(context == null) throw new ArgumentNullException("context");

        this.context = context;
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


    @Override
    protected void setUp() {

        super.setUp();

        if (isNewList) {

            this.deleteCommand.setIsAvailable(false);
            setName("");
            setScaleFactor(1);
            setScaleName(resourceProvider.getString(R.string.recipe_scaleName_person));

        } else {

            this.deleteCommand.setIsAvailable(true);

            //TODO: handle exception when list is not found
            this.recipe = listManager.getList(listId);
            setScaleFactor(recipe.getScaleFactor());
            setScaleName(recipe.getScaleName());
            setName(recipe.getName());
        }
        oldScaleFactor = scaleFactor;

    }

    @Override
    protected void saveCommandExecute() {
        if (isNewList) {
            listId = listManager.createList();
            recipe = listManager.getList(listId);
        }

        recipe.setName(this.getName());
        recipe.setScaleFactor(scaleFactor);
        recipe.setScaleName(scaleName);

        for(Item item : recipe.getItems()){
            item.setAmount(item.getAmount() * getScaleFactor()/oldScaleFactor);
        }


        listManager.saveList(listId);

        // if we just created a recipe, open it right away,
        // when a existing recipe was edited, just close the current view and go back to
        // whatever the previous screen was
        if (isNewList) {
            viewLauncher.showRecipe(listId);
        }

        finish();
    }

    @Override
    protected void deleteCommandExecute() {

        if (isNewList) {
            throw new UnsupportedOperationException();
        }


        if (SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.RECIPE_DELETION_SHOW_AGAIN_MSG, true, context)) {
            viewLauncher.showMessageDialogWithCheckbox(resourceProvider.getString(R.string.deleteRecipe_DialogTitle),
                    resourceProvider.getString(R.string.deleteRecipe_DialogText), resourceProvider.getString(R.string.yes),
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            listManager.deleteList(listId);
                            finish();
                        }
                    },
                    null, null, resourceProvider.getString(R.string.no),
                    NullCommand.VoidInstance, resourceProvider.getString(R.string.dont_show_this_message_again), false,
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.RECIPE_DELETION_SHOW_AGAIN_MSG, false, context);
                        }
                    }, null);
        } else {

            listManager.deleteList(listId);
            finish();
        }
    }

}
