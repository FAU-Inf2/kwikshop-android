package de.fau.cs.mad.kwikshop.android.model.synchronization.background;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import dagger.ObjectGraph;

import de.fau.cs.mad.kwikshop.android.di.KwikShopBackgroundModule;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.synchronization.CompositeSynchronizer;


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private final Context context;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        this.context = context;
    }


    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        this.context = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopBackgroundModule(this.context));
        final CompositeSynchronizer synchronizer = objectGraph.get(CompositeSynchronizer.class);
        synchronizer.synchronize();

    }
}
