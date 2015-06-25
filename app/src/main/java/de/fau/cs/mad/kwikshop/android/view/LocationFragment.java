package de.fau.cs.mad.kwikshop.android.view;


import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.apache.http.HttpException;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;
import de.fau.cs.mad.kwikshop.android.model.LocationFinder;
import se.walkercrou.places.Day;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Hours;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;
import se.walkercrou.places.Status;
import se.walkercrou.places.exception.NoResultsFoundException;


public class LocationFragment extends Fragment implements  OnMapReadyCallback {

    private View rootView;
    private GoogleMap map;
    private AlertDialog alert;
    private LocationFinder lastLocation;
    double lastLat;
    double lastLng;
    String address;

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

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.inject(this, rootView);

        hideInfoBox();

        whereIsTheNextSupermarketRequest();

        return rootView;

    }

    private void whereIsTheNextSupermarketRequest(){

        AsyncTask<Void, Void, Void> aTask = new AsyncTask<Void, Void, Void>(){
            double lat;
            double lng;
            List<Place> places;
            boolean isConnectionLost = false;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                LocationFinder location = new LocationFinder(getActivity());
                lat = location.getLatitude();
                lng = location.getLongitude();

            }

            @Override
            protected Void doInBackground(Void... params) {
                String googleBrowserApiKey = getResources().getString(R.string.google_browser_api_key);
                if (InternetHelper.checkInternetConnection(getActivity())) {

                    GooglePlaces client  = new GooglePlaces(googleBrowserApiKey);

                    if (client != null) {
                        try {
                            places = client.getNearbyPlaces(lat, lng, 2000, 30, Param.name("types").value("grocery_or_supermarket"));
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Exception: " + e.getMessage());
 
                        }
                    }

                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(places != null){
                    initiateMap(places);
                } else {
                    notificationOfNoConnection();
                }
            }
        };

        // no connection to internet
        if(!InternetHelper.checkInternetConnection(getActivity())){
            notificationOfNoConnection();
        } else {
            aTask.execute();
        }
    }

    private void initiateMap(final List<Place> places){

        // no connection to internet
        if(!InternetHelper.checkInternetConnection(getActivity())){
            notificationOfNoConnection();
        }

        // get last/current location
        lastLocation = new LocationFinder(getActivity().getApplicationContext());
        lastLat = lastLocation.getLatitude();
        lastLng = lastLocation.getLongitude();
        address = lastLocation.getAddressFromLastLocation();

        // set up map
        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(lastLat,lastLng) , 15.0f) );
        UiSettings settings = map.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);


        // display place on the map
        for(Place place : places){
            IconGenerator iconFactory = new IconGenerator(getActivity().getApplicationContext());
            addIcon(iconFactory, place.getName(), new LatLng(place.getLatitude(), place.getLongitude()));
        }


        // display info box
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                final Place clickedPlace = findCorrespondPlaceToMarker(marker, places);
                final String clickedAdress = LocationFinder.getAddress(new LatLng(clickedPlace.getLatitude(),clickedPlace.getLongitude()),
                        getActivity().getApplicationContext());

                if(clickedPlace != null){

                    showInfoBox();

                    mapPlaceName.setText(clickedPlace.getName());
                    mapPlaceOpenStatus.setText(convertStatus(clickedPlace.getStatus()));
                    mapPlaceDistance.setText(getDistanceBetweenLastLocationAndPlace(clickedPlace));
                    mapDirectionButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("http://maps.google.com/maps?daddr=" + clickedAdress));
                            startActivity(intent);
                        }
                    });
                }

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

    private void showInfoBox(){

        mapInfoBox.setVisibility(View.VISIBLE);
        mapDirectionButton.bringToFront();
        mapDirectionButton.setVisibility(View.VISIBLE);
    }

    private void hideInfoBox(){
        mapInfoBox.setVisibility(View.INVISIBLE);
        mapDirectionButton.setVisibility(View.INVISIBLE);
    }

    private String convertStatus(Status status){
        if(status.toString().equals(Status.OPENED.toString())){
            return getResources().getString(R.string.place_status_opened);
        } else if(status.toString().equals(Status.CLOSED.toString())){
            return getResources().getString(R.string.place_status_closed);
        } else
            return "";
    }

    private Place findCorrespondPlaceToMarker(Marker marker, List<Place> places){
        for(Place place : places){
            if(place.getLatitude() - marker.getPosition().latitude == 0.0 && place.getLongitude() - marker.getPosition().longitude == 0.0){
                return place;
            }
        }
        return null;
    }

    private String getDistanceBetweenLastLocationAndPlace(Place place){
        Location shopLocation = new Location("place");
        shopLocation.setLatitude(place.getLatitude());
        shopLocation.setLongitude(place.getLongitude());

        Location lastLocation = new Location("current");
        lastLocation.setLatitude(lastLat);
        lastLocation.setLongitude(lastLng);

        return  distanceConverter(lastLocation.distanceTo(shopLocation));

    }


    private String distanceConverter(double distance){
        if(distance >= 1000){
            return Math.round((distance / 1000) * 10.0) / 10.0 + " km";
        } else
            return Math.round(distance * 10.0) / 10.0 + " m";
    }

    private void addIcon(IconGenerator iconFactory, String text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        map.addMarker(markerOptions);
    }

    // Method to inform user about no internet connection
    private void notificationOfNoConnection(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.alert_dialog_connection_label);
        builder.setMessage(R.string.alert_dialog_connection_message);
        builder.setPositiveButton(R.string.alert_dialog_connection_try, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(InternetHelper.checkInternetConnection(getActivity())){
                    getActivity().startActivity(new Intent(getActivity().getApplicationContext(), LocationActivity.class));
                } else {
                    notificationOfNoConnection();
                }
            }
        });

        builder.setNegativeButton(R.string.alert_dialog_connection_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        alert = builder.create();
        alert.show();

    }

    @Override
    public void onPause() {
        super.onPause();
        if(alert != null)
            alert.dismiss();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }


}

