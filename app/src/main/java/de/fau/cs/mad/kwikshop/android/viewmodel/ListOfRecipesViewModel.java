package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.util.DefaultRecipesHelper;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class ListOfRecipesViewModel extends ListOfListsViewModel<Recipe> {

    private final DefaultRecipesHelper defaultRecipesHelper;

    private final Context context;

    // backing fields for properties
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
    public ListOfRecipesViewModel(ViewLauncher viewLauncher, ListManager<Recipe> listManager,
                                  DefaultRecipesHelper defaultRecipesHelper, Context context) {
        super(viewLauncher, listManager);

        if(defaultRecipesHelper == null) throw new ArgumentNullException("defaultRecipesHandler");

        if(context == null) throw new ArgumentNullException("context");

        this.defaultRecipesHelper = defaultRecipesHelper;

        this.context = context;
    }



    // Getters / Setters


    public Command getAddRecipeCommand() {
        return this.addRecipeCommand;
    }

    public Command<Integer> getSelectRecipeCommand() {
        return this.selectRecipeCommand;
    }

    public Command<Integer> getSelectRecipeDetailsCommand() {
        return this.selectRecipeDetailsCommand;
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(RecipeChangedEvent ev) {

        switch (ev.getChangeType()) {

            case Deleted:
                getLists().removeById(ev.getListId());
                break;

            case PropertiesModified:
            case Added:
                reloadList(ev.getListId());
                break;

            default:
                break;
        }
    }


    @Override
    protected ListType getListType() {
        return ListType.Recipe;
    }


    public void showAddDefaultRecipesDialog(){
        if(listManager.getLists().size() == 0 && SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.ASK_TO_ADD_DEFAULT_RECIPES, true, context)){
            viewLauncher.showMessageDialogWithCheckbox(context.getString(R.string.recipes), context.getString(R.string.recipe_default_no_recipes),
                    context.getString(R.string.yes),
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            defaultRecipesHelper.addDefaultRecipes();
                        }
                    },
                    null, null, context.getString(R.string.no), NullCommand.VoidInstance, context.getString(R.string.dont_show_this_message_again), false,
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.ASK_TO_ADD_DEFAULT_RECIPES, false, context);
                        }
                    }, null);
        }
    }

}
