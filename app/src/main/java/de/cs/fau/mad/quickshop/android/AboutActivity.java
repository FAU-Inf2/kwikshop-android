package de.cs.fau.mad.quickshop.android;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import cs.fau.mad.quickshop_android.R;


public class AboutActivity extends BaseActivity  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout in frameLayout from BaseActivity to access the navigation Drawer
        // do not use setContentView()!
        getLayoutInflater().inflate(R.layout.activity_about, frameLayout);
        mDrawerList.setItemChecked(position, true);
        setTitle(listArray.get(position));

        TextView versionView = (TextView) findViewById(R.id.about_textView_Version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionView.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
      */
}
