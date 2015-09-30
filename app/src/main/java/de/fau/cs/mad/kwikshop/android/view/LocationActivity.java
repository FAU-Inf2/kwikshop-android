package de.fau.cs.mad.kwikshop.android.view;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.viewmodel.LocationViewModel;


public class LocationActivity extends BaseActivity {

    private LocationViewModel viewModel;

    public static Intent getIntent(Context context) {
        return new Intent(context, LocationActivity.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.location_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        viewModel.setContext(getApplicationContext());
        switch (item.getItemId()) {
            case R.id.change_store_type:
                viewModel.selectPlaceType();
                break;
            case R.id.change_radius:
                viewModel.changeRadius();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(this));
        viewModel = objectGraph.get(LocationViewModel.class);
        objectGraph.inject(this);

        baseViewModel.setCurrentActivityName(this.getClass().getSimpleName());

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().add(frameLayout.getId(), LocationFragment.newInstance()).commit();
        }
    }




}
