package de.fau.cs.mad.kwikshop.android.viewmodel;


import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.messages.ListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.messages.RecipeChangedEvent;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesWrapper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.Unit;

public class RecipeItemDetailsViewModel extends ItemDetailsViewModel<Recipe> {

    private ItemDetailsViewModel.Listener listener;

    @Inject
    public RecipeItemDetailsViewModel(ListManager<Recipe> listManager, SimpleStorage<Unit> unitStorage,
                                      SimpleStorage<Group> groupStorage, ViewLauncher viewLauncher,
                                      AutoCompletionHelper autoCompletionHelper,
                                      ItemMerger<Recipe> itemMerger,
                                      SharedPreferencesWrapper sharedPreferences,
                                      ResourceProvider resourceProvider) {

        super(listManager, unitStorage, groupStorage, viewLauncher, autoCompletionHelper, itemMerger, sharedPreferences, resourceProvider);
    }

    public void setListener(ItemDetailsViewModel.Listener listener) {
        this.listener = listener;
    }


    @Override
    protected ListType getListType() {
        return ListType.Recipe;
    }

    @Override
    protected Listener getListener() {
        return listener != null ? listener : ItemDetailsViewModel.NullListener.Instance;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(RecipeChangedEvent event)  {
        if(event.getChangeType() == ListChangeType.Deleted && event.getListId() == this.listId) {
            finish();
        }
    }


}
