package de.fau.cs.mad.kwikshop.android.model.synchronization;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.Unit;

public class RecipeSyncDataResetter extends AbstractSyncDataResetter<Recipe> {

    @Inject
    public RecipeSyncDataResetter(ListManager<Recipe> listManager,
                                  SimpleStorage<Unit> unitStorage,
                                  SimpleStorage<Group> groupStorage,
                                  SimpleStorage<LastLocation> locationStorage) {

        super(listManager, unitStorage, groupStorage, locationStorage);
    }
}
