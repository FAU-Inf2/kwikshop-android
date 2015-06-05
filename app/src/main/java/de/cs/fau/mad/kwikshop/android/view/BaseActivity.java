package de.cs.fau.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Locale;

import cs.fau.mad.kwikshop_android.R;

/**
 * BaseActivity: all activities have to inherit
 */

public class BaseActivity extends ActionBarActivity {

    public static FrameLayout frameLayout;
    public static boolean refreshed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSavedLocale();
        frameLayout = (FrameLayout)findViewById(R.id.content_frame);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overview_action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_listofshoppinglists:
                startActivity(new Intent(this, ListOfShoppingListsActivity.class));
                return true;
            case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setSavedLocale() {

        // get current locale index
        int currentLocaleIdIndex = getSharedPreferences(SettingFragment.SETTINGS, Context.MODE_PRIVATE).getInt(SettingFragment.OPTION_1, 0);

        if(refreshed)
            return;

        if(currentLocaleIdIndex != 0)
            refreshed = true; // save refreshed status to avoid endless loops

        Locale setLocale = new Locale(SettingFragment.localeIds[currentLocaleIdIndex].toString());
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = setLocale;
        res.updateConfiguration(conf, dm);

        // Activity must be restarted to set saved locale
        Intent refresh = new Intent(this, ListOfShoppingListsActivity.class);
        startActivity(refresh);
    }

}
