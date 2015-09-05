package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.util.Locale;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.view.LoginActivity;
import de.fau.cs.mad.kwikshop.android.view.SettingFragment;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class BaseViewModel {

    private Context context;
    private Activity activity;
    private ViewLauncher viewLauncher;
    private boolean shoppingModeEnabled = false;

    public static String RESTARTEDACTIVITY = "RestartedActivity";

    @Inject
    public BaseViewModel(Context context, Activity activity, ResourceProvider resourceProvider, ViewLauncher viewLauncher){
        this.context = context;
        this.activity = activity;
        this.viewLauncher = viewLauncher;

        if(viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }

        if(resourceProvider == null)
            throw new IllegalArgumentException("'resourceProvider' must not be null");
    }

    public boolean isShoppingModeEnabled() {
        return shoppingModeEnabled;
    }

    public void setShoppingModeEnabled(boolean shoppingModeEnabled) {
        this.shoppingModeEnabled = shoppingModeEnabled;
    }


    public void startLoginActivity() {
        Intent intent = new Intent(context, LoginActivity.class);
        Bundle b = new Bundle();
        b.putBoolean("FORCE", true); //To make sure the Activity does not close immediately
        intent.putExtras(b);
        viewLauncher.startActivity(intent);
    }

    public void setSavedLocale() {

        if (activity.getIntent().getBooleanExtra(RESTARTEDACTIVITY, true)) {
            return;
        }

        // get current locale index
        int currentLocaleIdIndex =  SharedPreferencesHelper.loadInt(SharedPreferencesHelper.LOCALE, 0, context);
        Locale setLocale= new Locale(SettingFragment.localeIds[currentLocaleIdIndex].toString());

        if(currentLocaleIdIndex == 0) // default
            setLocale = Locale.getDefault();

        // change locale configuration
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = setLocale;
        res.updateConfiguration(conf, dm);

        // Activity must be restarted to set saved
        viewLauncher.startActivity(activity.getIntent().putExtra(RESTARTEDACTIVITY, true));
    }



}
