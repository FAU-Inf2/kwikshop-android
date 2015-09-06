package de.fau.cs.mad.kwikshop.android.viewmodel;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.Unit;

public class RecipeItemDetailsViewModel extends ItemDetailsViewModel {


    @Inject
    public RecipeItemDetailsViewModel(ViewLauncher viewLauncher, ListManager<Recipe> listManager, SimpleStorage<Unit> unitStorage,
                                      SimpleStorage<Unit> singularUnitStorage,
                                      SimpleStorage<Group> groupStorage, DisplayHelper displayHelper, AutoCompletionHelper autoCompletionHelper){
        super(viewLauncher, unitStorage, singularUnitStorage, groupStorage, displayHelper, autoCompletionHelper);

        if(listManager == null) throw new ArgumentNullException("listManager");

    }


}
