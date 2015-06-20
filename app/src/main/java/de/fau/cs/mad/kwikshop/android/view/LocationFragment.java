package de.fau.cs.mad.kwikshop.android.view;


import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;


public class LocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    // LogCat tag
    private static final String TAG = LocationActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    @InjectView(R.id.tv_location)
    TextView tv_location;

    @InjectView(R.id.tv_city)
    TextView tv_city;

    private View rootView;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    // coordinates of location
    private double latitude;
    private double longitude;

    private AlertDialog alert;
    private GoogleMap map;


    public static LocationFragment newInstance() {

        LocationFragment fragment = new LocationFragment();
        return fragment;

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check availability of play services
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.inject(this, rootView);

        return rootView;

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private void initiateMap(){
        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);
        mapFragment.getMapAsync(this);

        UiSettings settings = map.getUiSettings();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(latitude, longitude),13.5f, 0f, 0f))); // zoom, tilt, bearing
        map.setTrafficEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setCompassEnabled(true);
        settings.setMyLocationButtonEnabled(true);
        settings.setRotateGesturesEnabled(true);
        settings.setScrollGesturesEnabled(true);
        settings.setTiltGesturesEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setZoomGesturesEnabled(true);
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {
                // TODO Auto-generated method stub

                map.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!"));
            }
        });



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
   //   googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("My Location"));
    }

    @Override
    public void onConnected(Bundle bundle) {

        if(InternetHelper.checkInternetConnection(getActivity())){
            setLastLocation();
            displayLocation();
            displayCityName();
            initiateMap();
        } else {
            notificationOfNoConnection();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }


    protected synchronized void buildGoogleApiClient() {
        Context context = getActivity().getApplicationContext();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // Method to check Play Service support
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()
                .getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "This device is not supported.", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
            return false;
        }
        return true;
    }

    //Method to check Last Location
    private void setLastLocation(){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "No last Location available.", Toast.LENGTH_LONG).show();
        }

    }

    // Method to display the coordinates
    private void displayLocation(){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            tv_location.setText("Latitude: " + String.valueOf(mLastLocation.getLatitude()) +
                    " Longitude: " + String.valueOf(mLastLocation.getLongitude()));
        }
    }

    // Method to display your city name
    private void displayCityName(){
        Geocoder geocoder = new Geocoder(getActivity().getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(mLastLocation.getLatitude(),  mLastLocation.getLongitude(), 1);
            if (addresses.size() > 0){
                String address = "";
                int maxLines =  addresses.get(0).getMaxAddressLineIndex();
                for(int i = 0; i <= maxLines; i++){
                    address = address + addresses.get(0).getAddressLine(i) + " ";
                }
                tv_city.setText(address);
            }
        } catch (IOException e) {
            Log.e(TAG,e.getMessage().toString());
        }
    }

    // Method to inform the user about no internet connection
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



}

