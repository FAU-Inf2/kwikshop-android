package de.fau.cs.mad.kwikshop.android.model;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.List;

import de.fau.cs.mad.kwikshop.android.common.ConnectionInfo;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;

public class ConnectionInfoStorage {

    private final RuntimeExceptionDao<ConnectionInfo, Integer> dao;

    public ConnectionInfoStorage(RuntimeExceptionDao<ConnectionInfo, Integer> dao) {

        if(dao == null) {
            throw new ArgumentNullException("dao");
        }


        this.dao = dao;
    }


    public ConnectionInfo getConnectionInfo() {

        List<ConnectionInfo> infos =  dao.queryForAll();
        if(infos.size() == 0) {
            return null;
        } else if(infos.size() == 1) {
            return infos.get(0);
        } else {
            throw new UnsupportedOperationException("Query for ConnectionInfo yielded more than one result. This should not be able to happen");
        }
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {

        if(connectionInfo == null) {
            throw new ArgumentNullException("connectionInfo");
        }

        //clear all instances currently in the database
        List<ConnectionInfo> infos =  dao.queryForAll();
        dao.delete(infos);

        //create copy of connectionInfo to make sure the id is not set
        connectionInfo = new ConnectionInfo(connectionInfo);
        dao.create(connectionInfo);
    }


}

