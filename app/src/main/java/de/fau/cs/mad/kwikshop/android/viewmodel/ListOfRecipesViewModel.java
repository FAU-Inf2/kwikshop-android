package de.fau.cs.mad.kwikshop.android.viewmodel;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class ListOfRecipesViewModel extends ListOfListsViewModel<Recipe> {

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
    public ListOfRecipesViewModel(ViewLauncher viewLauncher, ListManager<Recipe> listManager) {
        super(viewLauncher, listManager);
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

}
