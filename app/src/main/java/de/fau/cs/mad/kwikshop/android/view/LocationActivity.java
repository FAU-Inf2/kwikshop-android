package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import de.fau.cs.mad.kwikshop.android.R;
import de.greenrobot.event.EventBus;

public class LocationActivity extends BaseActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, ListOfShoppingListsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(this.getClass().getSimpleName(), "created");

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), LocationFragment.newInstance()).commit();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(this.getClass().getSimpleName(), "started");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(this.getClass().getSimpleName(), "resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(this.getClass().getSimpleName(), "paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(this.getClass().getSimpleName(), "stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(this.getClass().getSimpleName(), "destroyed");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(this.getClass().getSimpleName(), "restarted");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

   

}
