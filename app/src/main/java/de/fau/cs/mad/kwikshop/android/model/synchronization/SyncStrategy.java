package de.fau.cs.mad.kwikshop.android.model.synchronization;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Implements the basic sync strategy for two lists of objects of types TClient and T2
 * @param <TClient> The client-side type of the objects to synchronize
 * @param <TServer> The server-side type of the objects to synchronize
 * @param <TSyncData> The type of sync state the concrete implementation uses
 *                    (A instance will be created before sync using initializeSyncData() and
 *                    passed to all subsequent calls to methods that need to be implemented)
 *
 */
public abstract class SyncStrategy<TClient, TServer, TSyncData> {



    public final void synchronize() {

        TSyncData syncData = initializeSyncData();

        Collection<Change<TClient, TServer>> changes = getChanges(syncData);

        processChanges(syncData, changes);

        cleanUpSyncData(syncData);

    }



    private Collection<Change<TClient, TServer>> getChanges(TSyncData syncData) {

        LinkedList<Change<TClient, TServer>> changes = new LinkedList<>();

        Set<Integer> processedServerIds = new HashSet<>();

        for(TClient clientObject : getClientObjects(syncData)) {

            // Client change type can either be
            //  - None
            //  - Modified
            //  - Added
            // but not Deleted, otherwise it would not be in this list

            // Server change type can either be
            //  - None (server object exists and was not modified)
            //  - Modified
            //  - Deleted (server object was deleted)
            // but not Added (we're iterating over items on client side)


            ChangeType clientChangeType = clientObjectModified(syncData,  clientObject)
                    ? ChangeType.Modified
                    : ChangeType.None;

            ChangeType serverChangeType;
            TServer serverObject = null;


            if(clientObjectExistsOnServer(syncData, clientObject)) {

                serverObject = getServerObjectForClientObject(syncData, clientObject);

                if(serverObjectModified(syncData, serverObject)) {
                    serverChangeType = ChangeType.Modified;
                } else {
                    serverChangeType = ChangeType.None;
                }

                processedServerIds.add(getServerId(syncData, serverObject));

            } else {

                //item not found on server

                if(clientObjectDeletedOnServer(syncData, clientObject)) {
                    serverChangeType = ChangeType.Deleted;
                } else {
                    serverChangeType = ChangeType.None;
                    clientChangeType = ChangeType.Added;
                }
            }

            changes.add(new Change<>(clientChangeType, clientObject, serverChangeType, serverObject));
        }


        for(TServer serverObject : getServerObjects(syncData)) {

            //skip objects already processed while iterating over client objects
            if(processedServerIds.contains(getServerId(syncData, serverObject))) {
                continue;
            }

            //we're iterating over server objects, so server change type can be
            // - None
            // - Modified
            // - Added

            ChangeType serverChangeType;
            ChangeType clientChangeType;
            TClient clientObject = null;

            if(serverObjectExistsOnClient(syncData, serverObject)) {

                serverChangeType = serverObjectModified(syncData, serverObject)
                        ? ChangeType.Modified
                        : ChangeType.None;

                clientObject = getClientObjectForServerObject(syncData, serverObject);

                if(clientObjectModified(syncData, clientObject)) {
                    clientChangeType = ChangeType.Modified;
                } else {
                    clientChangeType = ChangeType.None;
                }
            } else {

                if(serverObjectDeletedOnClient(syncData, serverObject)) {
                    clientChangeType = ChangeType.Deleted;

                    serverChangeType = serverObjectModified(syncData, serverObject)
                            ? ChangeType.Modified
                            : ChangeType.None;
                } else {
                    clientChangeType = ChangeType.None;
                    serverChangeType = ChangeType.Added;
                }
            }

            changes.add(new Change<>(clientChangeType, clientObject, serverChangeType, serverObject));
        }

        return changes;

    }

    private void processChanges(TSyncData syncData, Collection<Change<TClient, TServer>> changes) {

        for(Change<TClient, TServer> change : changes) {
            processChange(syncData, change);
        }

    }

    private void processChange(TSyncData syncData, Change<TClient, TServer> change) {

        // nothing changed
        if(change.getClientChangeType() == ChangeType.None &&
           change.getServerChangeType() == ChangeType.None) {

            //nothing to do
        }

        if(change.getClientChangeType() == ChangeType.None &&
           change.getServerChangeType() == ChangeType.Modified) {

            updateClientObject(syncData, change.getClientObject(), change.getServerObject());
        }

        if(change.getClientChangeType() == ChangeType.None &&
                change.getServerChangeType() == ChangeType.Added) {

            createClientObject(syncData, change.getServerObject());
        }

        if(change.getClientChangeType() == ChangeType.None &&
                change.getServerChangeType() == ChangeType.Deleted) {

            deleteClientObject(syncData, change.getClientObject());
        }



        if(change.getClientChangeType() == ChangeType.Modified &&
                change.getServerChangeType() == ChangeType.None) {

            updateServerObject(syncData, change.getClientObject(), change.getServerObject());
        }

        if(change.getClientChangeType() == ChangeType.Modified &&
                change.getServerChangeType() == ChangeType.Modified) {

            //Conflict: Server data takes precedence over client data
            updateClientObject(syncData, change.getClientObject(), change.getServerObject());
        }

        if(change.getClientChangeType() == ChangeType.Modified &&
                change.getServerChangeType() == ChangeType.Added) {

            //not a valid combination
            throw new UnsupportedOperationException();
        }

        if(change.getClientChangeType() == ChangeType.Modified &&
                change.getServerChangeType() == ChangeType.Deleted) {

            //recreate a server object for the modified client object
            createServerObject(syncData, change.getClientObject());
        }



        if(change.getClientChangeType() == ChangeType.Added &&
                change.getServerChangeType() == ChangeType.None) {

            createServerObject(syncData, change.getClientObject());
        }

        if(change.getClientChangeType() == ChangeType.Added &&
                change.getServerChangeType() == ChangeType.Modified) {

            //not a valid combination
            throw new UnsupportedOperationException();
        }

        if(change.getClientChangeType() == ChangeType.Added &&
                change.getServerChangeType() == ChangeType.Added) {

            //not a valid combination
            throw new UnsupportedOperationException();
        }

        if(change.getClientChangeType() == ChangeType.Added &&
                change.getServerChangeType() == ChangeType.Deleted) {

            //not a valid combination
            throw new UnsupportedOperationException();
        }

        if(change.getClientChangeType() == ChangeType.Deleted &&
                change.getServerChangeType() == ChangeType.None) {

            deleteServerObject(syncData, change.getServerObject());
        }

        if(change.getClientChangeType() == ChangeType.Deleted &&
                change.getServerChangeType() == ChangeType.Modified) {

            createClientObject(syncData, change.getServerObject());
        }

        if(change.getClientChangeType() == ChangeType.Deleted &&
                change.getServerChangeType() == ChangeType.Added) {

            //not a valid combination
            throw new UnsupportedOperationException();
        }

        if(change.getClientChangeType() == ChangeType.Deleted &&
                change.getServerChangeType() == ChangeType.Deleted) {

            //fine, nothing to do
        }
    }



    protected abstract TSyncData initializeSyncData();

    protected abstract void cleanUpSyncData(TSyncData syncData);

    protected abstract int getClientId(TSyncData syncData, TClient object);

    protected abstract int getServerId(TSyncData syncData, TServer object);

    protected abstract Collection<TClient> getClientObjects(TSyncData syncData);

    protected abstract Collection<TServer> getServerObjects(TSyncData syncData);

    protected abstract boolean clientObjectExistsOnServer(TSyncData syncData, TClient object);

    protected abstract boolean serverObjectExistsOnClient(TSyncData syncData, TServer object);

    protected abstract boolean clientObjectDeletedOnServer(TSyncData syncData, TClient object);

    protected abstract boolean serverObjectDeletedOnClient(TSyncData syncData, TServer object);

    protected abstract TClient getClientObjectForServerObject(TSyncData syncData, TServer serverObject);

    protected abstract TServer getServerObjectForClientObject(TSyncData syncData, TClient clientObject);

    protected abstract boolean serverObjectModified(TSyncData syncData, TServer serverObject);

    protected abstract boolean clientObjectModified(TSyncData syncData, TClient clientObject);



    protected abstract void deleteServerObject(TSyncData syncData, TServer serverObject);

    protected abstract void deleteClientObject(TSyncData syncData, TClient clientObject);

    protected abstract void createServerObject(TSyncData syncData, TClient clientObject);

    protected abstract void createClientObject(TSyncData syncData, TServer serverObject);

    protected abstract void updateServerObject(TSyncData syncData, TClient clientObject, TServer serverObject);

    protected abstract void updateClientObject(TSyncData syncData, TClient clientObject, TServer serverObject);

}
