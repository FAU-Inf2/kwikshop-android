package de.fau.cs.mad.kwikshop.android.model.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import javax.inject.Inject;

import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.messages.ShareSuccessEvent;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.common.rest.responses.SharingResponse;
import de.greenrobot.event.EventBus;

public class RedeemSharingCodeTask extends AsyncTask<String, String, String> {

    @Inject
    RestClientFactory clientFactory;

    ProgressDialog progDailog;
    Context context;

    public RedeemSharingCodeTask(Context context, Activity activity) {
        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(activity));
        objectGraph.inject(this);

        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progDailog = new ProgressDialog(context);
        progDailog.setMessage(context.getResources().getString(R.string.share_loading));
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(true);
        progDailog.show();
    }

    @Override
    protected String doInBackground(String... sharingCode) {
        SharingResponse response;
        try {
            response = clientFactory.getShoppingListClient().share(sharingCode[0]);
        } catch(Exception e) {
            return null;
        }

        return response.getShoppingListName();
    }

    @Override
    protected void onPostExecute(final String result) {
        super.onPostExecute(result);

        if(result != null) {
            progDailog.setMessage(String.format(context.getResources().getString(R.string.share_success), result));
        } else {
            progDailog.setMessage(context.getResources().getString(R.string.share_failure));
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                progDailog.dismiss();

                if(result != null)
                    EventBus.getDefault().post(new ShareSuccessEvent());
            }
        }, 3000);
    }
}