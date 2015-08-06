package de.fau.cs.mad.kwikshop.android.view;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.SupermarketPlace;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.LocationViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;
import se.walkercrou.places.Status;

import static de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper.API_ENDPOINT;
import static de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper.saveString;


public class LocationFragment extends Fragment implements  OnMapReadyCallback, SupermarketPlace.AsyncPlaceRequestListener {

    private View rootView;
    private GoogleMap map;
    private AlertDialog alert;
    private LocationFinderHelper lastLocation;
    private double lastLat;
    private double lastLng;

    private Context context;
    private List<Place> places;

    private LocationViewModel viewModel;

    @Inject
    ViewLauncher viewLauncher;

    @Inject
    ResourceProvider resourceProvider;

    @InjectView(R.id.map_infobox)
    RelativeLayout mapInfoBox;

    @InjectView(R.id.map_place_name)
    TextView mapPlaceName;

    @InjectView(R.id.map_place_open_status)
    TextView mapPlaceOpenStatus;

    @InjectView(R.id.map_place_distance)
    TextView mapPlaceDistance;

    @InjectView(R.id.direction_button)
    View mapDirectionButton;

    private static final String LOG_TAG = "LocationFragment";

    public static LocationFragment newInstance() {
        LocationFragment fragment = new LocationFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.inject(this, rootView);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        viewModel = objectGraph.get(LocationViewModel.class);
        objectGraph.inject(this);

        viewModel.setActivity(getActivity());
        viewModel.setContext(context);

        showProgressDialog();

        // async places request
        if(viewLauncher.checkInternetConnection()){
            viewModel.getNearbySupermarketPlaces(this);
            viewModel.getLastLatLng();
        } else {
            notificationOfNoConnection();
        }

        hideInfoBox();
        return rootView;

    }

    public void showProgressDialog(){
        viewLauncher.showProgressDialog(
                resourceProvider.getString(R.string.supermarket_finder_progress_dialog_message),
                resourceProvider.getString(R.string.alert_dialog_connection_cancel),
                true,
                viewModel.getCancelProgressDialogCommand()
        );

    }

    // called when place request is ready
    @Override
    public void postResult(List<Place> pPlaces) {
        places = pPlaces;
        initiateMap();
        dismissProgressDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    void dismissProgressDialog(){
       viewLauncher.dismissProgressDialog();
    }

    private void initiateMap(){
        if(!viewModel.isCanceld()){
            MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    private void showInfoBox(){

        mapInfoBox.setVisibility(View.VISIBLE);
        mapDirectionButton.bringToFront();
        mapDirectionButton.setVisibility(View.VISIBLE);
    }

    private void hideInfoBox(){
        mapInfoBox.setVisibility(View.INVISIBLE);
        mapDirectionButton.setVisibility(View.INVISIBLE);
    }

    private void notificationOfNoConnection(){

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.alert_dialog_connection_label),
                resourceProvider.getString(R.string.alert_dialog_connection_message),
                resourceProvider.getString(R.string.alert_dialog_connection_try),
                new Command<Void>() {
                    @Override
                    public void execute(Void parameter) {
                        if(viewLauncher.checkInternetConnection())
                            viewLauncher.showLocationActivity();
                        else {
                            notificationOfNoConnection();
                        }
                    }
                },
                resourceProvider.getString(R.string.alert_dialog_connection_cancel),
                viewModel.getFinishActivityCommand()
        );

    }

    @Override
    public void onPause() {
        super.onPause();
        viewLauncher.dismissDialog();
    }

    @Override
    public void onMapReady(GoogleMap gmap) {

        map =  viewModel.setupGoogleMap(gmap);
        viewModel.showPlacesInGoogleMap(places, map);

        // display info box
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                final Place clickedPlace = viewModel.findClickedPlace(marker, places);
                final String clickedAddress = viewModel.findClickedAddress(clickedPlace);

                showInfoBox();

                mapPlaceName.setText(clickedPlace.getName());
                mapPlaceOpenStatus.setText(viewModel.converStatus(clickedPlace.getStatus()));
                mapPlaceDistance.setText(viewModel.getDistanceBetweenLastLocationAndPlace(clickedPlace, new LatLng(lastLat,lastLng)));
                mapDirectionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.getrouteIntentCommand().execute(clickedAddress);
                    }
                });
                return false;
            }
        });

        // hide info box
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideInfoBox();
            }
        });

    }



}

