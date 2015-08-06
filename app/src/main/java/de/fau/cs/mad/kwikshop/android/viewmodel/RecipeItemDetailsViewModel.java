package de.fau.cs.mad.kwikshop.android.viewmodel;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;

public class RecipeItemDetailsViewModel extends ItemDetailsViewModel {


    private final ListManager<Recipe> listManager;

    private final ItemMerger<Recipe> itemMerger;

    @Override
    protected ListType getListType() {
        return ListType.Recipe;
    }

    @Override
    protected ListManager<Recipe> getListManager() {
        return this.listManager;
    }

    @Override
    protected ItemMerger<Recipe> getItemMerger(){ return this.itemMerger; }

    @Inject
    public RecipeItemDetailsViewModel(ViewLauncher viewLauncher, ListManager<Recipe> listManager, SimpleStorage<Unit> unitStorage,
                                      SimpleStorage<Group> groupStorage, DisplayHelper displayHelper, AutoCompletionHelper autoCompletionHelper){
        super(viewLauncher, unitStorage, groupStorage, displayHelper, autoCompletionHelper);

        if(listManager == null) throw new ArgumentNullException("listManager");

        this.listManager = listManager;

        this.itemMerger = new ItemMerger<>(listManager);
    }


}
