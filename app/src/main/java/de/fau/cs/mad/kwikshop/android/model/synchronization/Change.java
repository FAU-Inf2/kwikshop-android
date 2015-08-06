package de.fau.cs.mad.kwikshop.android.model.synchronization;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;

public class Change<TClient, TServer> {


    private final ChangeType clientChangeType;
    private final TClient clientObject;
    private final ChangeType serverChangeType;
    private final TServer serverObject;

    public Change(ChangeType clientChangeType, TClient clientObject,
                  ChangeType serverChangeType, TServer serverObject) {

        if(clientChangeType == null) {
            throw new ArgumentNullException("clientChangeType");
        }

        if(serverChangeType == null) {
            throw new ArgumentNullException("serverChangeType");
        }

        this.clientChangeType = clientChangeType;
        this.clientObject = clientObject;
        this.serverChangeType = serverChangeType;
        this.serverObject = serverObject;
    }



    public ChangeType getClientChangeType() {
        return this.clientChangeType;
    }

    public TClient getClientObject() {
        return this.clientObject;
    }

    public ChangeType getServerChangeType() {
        return this.serverChangeType;
    }

    public TServer getServerObject() {
        return this.serverObject;
    }


}
