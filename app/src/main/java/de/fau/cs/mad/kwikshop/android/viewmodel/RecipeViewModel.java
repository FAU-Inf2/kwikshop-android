package de.fau.cs.mad.kwikshop.android.viewmodel;


import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.common.Group;
import de.fau.cs.mad.kwikshop.android.common.Item;
import de.fau.cs.mad.kwikshop.android.common.Recipe;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.ItemParser;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListChangeType;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class RecipeViewModel extends ListViewModel<Recipe> {


    @Inject
    public RecipeViewModel(ViewLauncher viewLauncher, ListManager<Recipe> recipeManager,
                                 SimpleStorage<Unit> unitStorage, SimpleStorage<Group> groupStorage,
                                 ItemParser itemParser, DisplayHelper displayHelper,
                                 AutoCompletionHelper autoCompletionHelper, LocationFinderHelper locationFinderHelper) {

        super(viewLauncher, recipeManager, unitStorage, groupStorage, itemParser, displayHelper, autoCompletionHelper, locationFinderHelper);
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(RecipeChangedEvent event) {

        if(event.getListId() == this.listId) {

            if(event.getChangeType() == ListChangeType.Deleted) {
                finish();
            } else  if(event.getChangeType() == ListChangeType.PropertiesModified) {
                loadList();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent event) {

        if(event.getListType() == ListType.Recipe && event.getListId() == this.listId) {

            switch (event.getChangeType()) {

                case Added:
                case PropertiesModified:
                    loadList();
                    break;

                case  Deleted:
                    items.removeById(event.getItemId());
                    break;

            }
        }
    }


    @Override
    protected void addItemCommandExecute() {
        ensureIsInitialized();
        viewLauncher.RecipeShowItemDetailsView(this.listId);
    }

    @Override
    protected void selectItemCommandExecute(int itemId) {
        ensureIsInitialized();
        viewLauncher.RecipeShowItemDetailsView(this.listId, itemId);
    }

    @Override
    protected void loadList() {

        Recipe recipe = listManager.getList(this.listId);

        this.setName(recipe.getName());

        for(Item item : recipe.getItems()) {
            updateItem(item);
        }
    }


    private void updateItem(Item item) {
        items.setOrAddById(item);
    }

}

