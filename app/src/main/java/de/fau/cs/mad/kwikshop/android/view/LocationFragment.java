package de.fau.cs.mad.kwikshop.android.view;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;
import butterknife.ButterKnife;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.viewmodel.LocationViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

public class LocationFragment  extends FragmentWithViewModel{

    private LocationViewModel viewModel;
    public final static String SEARCH_ENABLED = "search_enabled";

    @Inject
    ViewLauncher viewLauncher;

    public static LocationFragment newInstance() {
        return new LocationFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem searchItem = menu.findItem(R.id.location_search);
        SearchView searchView  = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if(null != searchManager ) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getActivity().getApplicationContext(), LocationActivity.class)));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        viewModel = objectGraph.get(LocationViewModel.class);
        objectGraph.inject(this);
        viewModel.setContext(getActivity().getApplicationContext());
        setHasOptionsMenu(true);
        handleIntent(getActivity().getIntent());


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.inject(this, rootView);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
        viewModel.setMapFragment(mapFragment);
        viewModel.setView(rootView);

        Command<Void> placeRequestCommand = viewModel.getStartPlaceRequestCommand();
        if(placeRequestCommand.getCanExecute())
            placeRequestCommand.execute(null);

        return rootView;
    }


    void dismissDialog(){
        viewLauncher.dismissDialog();}

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissDialog();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissDialog();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            viewModel.setSearchAddress(query);
        } else {
            if(intent.getExtras() != null){
                String query = intent.getExtras().getString(SEARCH_ENABLED);
                viewModel.setSearchAddress(query);
            }
        }


    }


}

