package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.fau.cs.mad.kwikshop.android.view.LocationActivity;


public class LocationFinder implements LocationListener {

    private static final String TAG = LocationActivity.class.getSimpleName();
    private LocationManager locationManager;
    private Criteria criteria;
    private String provider;
    Context context;

    // coordinates of location
    private double latitude;
    private double longitude;

    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;



    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private Location location;

    public LocationFinder(Context context){

        this.context = context;
        getLocation();
    }

    public Location getLocation() {

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            if (locationManager != null) {
                // get last position
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        }

        // if GPS Enabled get lat/long using GPS Services
        if (isGPSEnabled) {
            if (location == null) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null) {
                    // get last position
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        }
        return location;

    }

    public double getLatitude(){
        return location.getLatitude();
    }

    public double getLongitude(){
        return location.getLongitude();
    }


    public String getAddressFromLastLocation(){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),  location.getLongitude(), 1);
            if (addresses.size() > 0){
                String address = "";
                int maxLines =  addresses.get(0).getMaxAddressLineIndex();
                for(int i = 0; i <= maxLines; i++){
                    address = address + addresses.get(0).getAddressLine(i) + " ";
                }
              return address;
            }
        } catch (IOException e) {
            Log.e(TAG,e.getMessage().toString());
        }
        return "No Address found";
    }


    public static String getAddress(LatLng coord, Context context){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(coord.latitude,  coord.longitude, 1);
            if (addresses.size() > 0){
                String address = "";
                int maxLines =  addresses.get(0).getMaxAddressLineIndex();
                for(int i = 0; i <= maxLines; i++){
                    address = address + addresses.get(0).getAddressLine(i) + " ";
                }
                return address;
            }
        } catch (IOException e) {
            Log.e(TAG,e.getMessage().toString());
        }
        return "No Address found";
    }


    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}