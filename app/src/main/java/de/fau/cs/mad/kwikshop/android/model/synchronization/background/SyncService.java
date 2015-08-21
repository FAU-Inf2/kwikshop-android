package de.fau.cs.mad.kwikshop.android.model.synchronization.background;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncService extends Service {

    // Storage for an instance of the sync adapter
    private static SyncAdapter syncAdapter = null;

    // Object to use as a thread-safe lock
    private static final Object syncAdapterLock = new Object();



    @Override
    public void onCreate() {
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }



}
