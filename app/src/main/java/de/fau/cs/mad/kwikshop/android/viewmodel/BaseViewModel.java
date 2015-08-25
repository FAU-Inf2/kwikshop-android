package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.view.LoginActivity;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class BaseViewModel {

    private Context context;
    private ResourceProvider resourceProvider;
    private ViewLauncher viewLauncher;

    @Inject
    public BaseViewModel(Context context, ResourceProvider resourceProvider, ViewLauncher viewLauncher){
        this.context = context;
        this.resourceProvider = resourceProvider;
        this.viewLauncher = viewLauncher;

        if(viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }

        if(resourceProvider == null)
            throw new IllegalArgumentException("'resourceProvider' must not be null");
    }


    public void makeToast(){
        Toast.makeText(context, "NavigationDrawer",Toast.LENGTH_LONG).show();
    }

    public void startLoginActivity() {
        Intent intent = new Intent(context, LoginActivity.class);
        Bundle b = new Bundle();
        b.putBoolean("FORCE", true); //To make sure the Activity does not close immediately
        intent.putExtras(b);
        viewLauncher.startActivity(intent);
    }
}
