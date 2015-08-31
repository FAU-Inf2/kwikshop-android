package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.SupermarketPlace;
import de.fau.cs.mad.kwikshop.android.viewmodel.LocationViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import se.walkercrou.places.Place;

public class LocationFragment extends Fragment implements  SupermarketPlace.AsyncPlaceRequestListener {

    private Context context;
    private List<Place> places;
    private LatLng latLng;
    private LocationViewModel viewModel;
    private View rootView;
    private MapView mapView;
    private GoogleMap map;
    private Bundle mSavedInstanceState;


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
    public void onResume() {
        if(mapView != null){
            mapView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        if(mapView != null){
            mapView.onDestroy();
        }
        super.onDestroyView();
    }

    @Override
    public final void onLowMemory() {
        if(mapView != null){
            mapView.onLowMemory();
        }
        super.onLowMemory();
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

        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            Log.e("map:", "Mao initializer failed");
        }

        mapView = (MapView) rootView.findViewById(R.id.map);
        mapView.onCreate(mSavedInstanceState);
        map = mapView.getMap();


        mSavedInstanceState = savedInstanceState;

        showProgressDialog();

        // async places request
        if(viewLauncher.checkInternetConnection()){
            viewModel.getNearbySupermarketPlaces(this, 5000, 30);
            latLng = viewModel.getLastLatLng();
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
                false,
                viewModel.getCancelProgressDialogCommand()
        );
    }

    // called when place request is ready
    @Override
    public void postResult(List<Place> pPlaces) {
        places = pPlaces;
        Log.e("MAP: ", "initiate Map");
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
        if (!viewModel.isCanceld()) {

            onMapReady(map);

            //FragmentManager fm = getChildFragmentManager();
           // SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
           // mapFragment.getMapAsync(this);
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
        viewModel.notificationOfNoConnectionForMap();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewLauncher.dismissDialog();
    }


    public void onMapReady(GoogleMap map) {

        map = viewModel.setupGoogleMap(map);
        viewModel.showPlacesInGoogleMap(places);

        // display info box
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final Place clickedPlace = viewModel.findClickedPlace(marker, places);
                final String clickedAddress = viewModel.findClickedAddress(clickedPlace);
                showInfoBox();
                mapPlaceName.setText(clickedPlace.getName());
                mapPlaceOpenStatus.setText(viewModel.converStatus(clickedPlace.getStatus()));
                mapPlaceDistance.setText(viewModel.getDistanceBetweenLastLocationAndPlace(clickedPlace, latLng));
                mapDirectionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.getRouteIntentCommand().execute(clickedAddress);
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

