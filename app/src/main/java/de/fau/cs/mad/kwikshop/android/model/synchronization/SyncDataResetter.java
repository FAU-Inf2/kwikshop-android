package de.fau.cs.mad.kwikshop.android.model.synchronization;

import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;

public interface SyncDataResetter<TList extends DomainListObject> {

    void resetSyncData();

}
