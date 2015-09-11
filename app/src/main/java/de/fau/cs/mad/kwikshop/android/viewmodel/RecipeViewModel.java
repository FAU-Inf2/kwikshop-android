package de.fau.cs.mad.kwikshop.android.viewmodel;


import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
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

    private final ListManager<ShoppingList> shoppingListManager;

    private final ListManager<Recipe> recipeManager;

    private final ResourceProvider resourceProvider;

    @Inject
    public RecipeViewModel(ViewLauncher viewLauncher, ListManager<Recipe> recipeManager, ListManager<ShoppingList> shoppingListManager,
                                 SimpleStorage<Unit> unitStorage, SimpleStorage<Group> groupStorage,
                                 ItemParser itemParser, DisplayHelper displayHelper,
                                 AutoCompletionHelper autoCompletionHelper, LocationFinderHelper locationFinderHelper,
                                 ResourceProvider resourceProvider) {

        super(viewLauncher, recipeManager, unitStorage, groupStorage, itemParser, displayHelper, autoCompletionHelper, locationFinderHelper, null);

        if(shoppingListManager == null) throw new ArgumentNullException("shoppingListManager");

        if(resourceProvider == null) throw new ArgumentNullException("resourceProvider");

        this.recipeManager = recipeManager;

        this.shoppingListManager = shoppingListManager;

        this.resourceProvider = resourceProvider;
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
        items.setOrAddById(new ItemViewModel(item));
    }

    public void showAddRecipeDialog(int listId) {
        if (shoppingListManager.getLists().size() == 0) {
            viewLauncher.showMessageDialog(resourceProvider.getString(R.string.recipe_add_to_shoppinglist),
                    resourceProvider.getString(R.string.recipe_no_shoppinglist),
                    resourceProvider.getString(R.string.yes),
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            viewLauncher.showAddShoppingListView();
                        }
                    },
                    resourceProvider.getString(R.string.no), null);
        } else {
            viewLauncher.showAddRecipeDialog(shoppingListManager, recipeManager, listId, false);
        }
    }
}

