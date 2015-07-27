package de.fau.cs.mad.kwikshop.android.model;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetHelper extends BroadcastReceiver {

    NetworkStateChangeListener listener;


    public static boolean checkInternetConnection(Activity activity) {
        if(activity == null)
            return false;
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.isConnected()) {
               listener.onNewNetworkConnection();
            } else {
                listener.onLostNetworkConnection();
            }
        }
    }

    public void setOnNetworkStateChangeListener(NetworkStateChangeListener listener){
        this.listener = listener;
    }

    public interface NetworkStateChangeListener{
        public void onLostNetworkConnection();
        public void onNewNetworkConnection();
    }

}
