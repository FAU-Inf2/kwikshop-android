package de.fau.cs.mad.kwikshop.android.view;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;

import butterknife.ButterKnife;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;
import de.fau.cs.mad.kwikshop.android.model.LocationFinder;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;


public class LocationFragment extends Fragment implements  OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    private View rootView;
    private GoogleMap map;
    private AlertDialog alert;
    private LocationFinder lastLocation;
    double lastLat;
    double lastLng;
    String address;

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


        whereIsTheNextSupermarketRequest();

        return rootView;

    }

    private void whereIsTheNextSupermarketRequest(){

        AsyncTask<Void, Void, Void> aTask = new AsyncTask<Void, Void, Void>(){
            double lat;
            double lng;
            List<Place> places;

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
                GooglePlaces client = new GooglePlaces(googleBrowserApiKey);
                //places = client.getPlacesByQuery("supermarkt", GooglePlaces.MAXIMUM_RESULTS, Param.name("types").value("grocery_or_supermarket"));
                places = client.getNearbyPlaces(lat, lng, 2000, GooglePlaces.MAXIMUM_RESULTS, Param.name("types").value("grocery_or_supermarket"));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                initiateMap(places);
                for(Place place : places){
                    Log.i(LOG_TAG,"Places: " + place.getName());
                }
            }
        };

        aTask.execute();

    }

    private void initiateMap(List<Place> places){

        // no connection to internet
        if(!InternetHelper.checkInternetConnection(getActivity())){
            notificationOfNoConnection();
        }

        lastLocation = new LocationFinder(getActivity().getApplicationContext());
        lastLat = lastLocation.getLatitude();
        lastLng = lastLocation.getLongitude();
        address = lastLocation.getAddressFromLastLocation();

        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(lastLat,lastLng) , 15.0f) );
        UiSettings settings = map.getUiSettings();
        settings.setAllGesturesEnabled(true);

        for(Place place : places){

           String distance = getDistanceBetweenLastLocationAndPlace(place);

            IconGenerator iconFactory = new IconGenerator(getActivity().getApplicationContext());
            addIcon(iconFactory, place.getName() + " " +  distance, new LatLng(place.getLatitude(), place.getLongitude()));
         //   map.addMarker(new MarkerOptions().position(new LatLng(place.getLatitude(), place.getLongitude())).title(place.getName()));
        }

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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(getActivity().getApplicationContext(),
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }
}

