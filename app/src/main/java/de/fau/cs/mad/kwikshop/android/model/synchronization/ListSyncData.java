package de.fau.cs.mad.kwikshop.android.model.synchronization;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.android.util.CollectionUtilities;
import de.fau.cs.mad.kwikshop.common.DeletionInfo;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObjectServer;

public class ListSyncData<TListClient extends DomainListObject, TListServer extends DomainListObjectServer> {


    private Collection<TListClient> clientLists;
    private Map<Integer, TListServer> serverLists;
    private Map<Integer, TListClient> clientListsByServerId;
    private Map<Integer, DeletionInfo> deletedListsServer;
    private Map<Integer, DeletedList> deletedListsClientByServerId;

    public ListSyncData(Collection<TListClient> clientLists, Collection<DeletedList> deletedListsClient,
                        Collection<TListServer> serverLists, List<DeletionInfo> deletedListsServer) {

        if(clientLists == null) {
            throw new ArgumentNullException("clientLists");
        }

        if(deletedListsClient == null) {
            throw new ArgumentNullException("deletedListsClient");
        }

        if(serverLists == null) {
            throw new ArgumentNullException("serverLists");
        }

        if(deletedListsServer == null) {
            throw new ArgumentNullException("deletedListsServer");
        }


        this.clientLists = clientLists;
        this.serverLists = CollectionUtilities.toMap(serverLists);
        this.clientListsByServerId = CollectionUtilities.toMapByServerId(clientLists);
        this.deletedListsServer = CollectionUtilities.toMap(deletedListsServer);
        this.deletedListsClientByServerId = CollectionUtilities.toDeletedListMapByServerId(deletedListsClient);
    }

    public Collection<TListClient> getClientLists() {
        return clientLists;
    }

    public Map<Integer, TListServer> getServerLists() {
        return serverLists;
    }

    public Map<Integer, TListClient> getClientListsByServerId() {
        return this.clientListsByServerId;
    }

    public Map<Integer, DeletionInfo> getDeletedListsServerByServerId(){
        return deletedListsServer;
    }

    public Map<Integer, DeletedList> getDeletedListsClientByServerId() {
        return deletedListsClientByServerId;
    }




}
