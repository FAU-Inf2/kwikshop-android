package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;


import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.view.LocationActivity;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.SupermarketPlace;
import de.fau.cs.mad.kwikshop.android.util.ClusterMapItem;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.util.ClusterItemRendered;
import de.fau.cs.mad.kwikshop.android.view.LocationFragment;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import se.walkercrou.places.Place;
import se.walkercrou.places.Status;

import static de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper.*;

public class LocationViewModel {

    private Context context;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;
    private ClusterManager<ClusterMapItem> mClusterManager;

    @Inject
    public LocationViewModel(ResourceProvider resourceProvider, ViewLauncher viewLauncher) {

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if(viewLauncher == null) {
            throw new ArgumentNullException("viewLauncher");
        }

        this.resourceProvider = resourceProvider;
        this.viewLauncher = viewLauncher;
    }


    public void setContext(Context context){ this.context = context; }

    public Command<Void> getCancelProgressDialogCommand(){ return cancelProgressDialogCommand; }

    public Command<Void> getFinishActivityCommand(){ return finishCommand; }

    public Command<String> getRouteIntentCommand(){ return routeIntentCommand; }

    public Command getRestartActivityCommand(){ return restartActivityCommand; }



    final Command<Void> cancelProgressDialogCommand =  new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            viewLauncher.showListOfShoppingListsActivity();
        }
    };

    final Command<Void> finishCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            viewLauncher.finishActivity();
        }
    };

    final Command<String> routeIntentCommand = new Command<String>(){
        @Override
        public void execute(String targetAddress) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + targetAddress));
            viewLauncher.startActivity(intent);
        }
    };


    final Command retryConnectionCheckWithLocationPermissionCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            if(InternetHelper.checkInternetConnection(context))
                viewLauncher.showLocationActivity();
            else {
                notificationOfNoConnectionWithLocationPermission();
            }
        }
    };

    final Command retryConnectionCheckCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            if(InternetHelper.checkInternetConnection(context))
                viewLauncher.showLocationActivity();
            else {
               notificationOfNoConnectionForMap();
            }
        }
    };

    public Command<Void> startSupermarketFinderWithPermission = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, true, context);
            viewLauncher.showLocationActivity();

        }
    };

    final Command<Void> withdrawLocalizationPermissionCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, false, context);
            viewLauncher.showListOfShoppingListsActivity();
        }
    };


    final Command restartActivityCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            viewLauncher.restartActivity();
        }
    };

    final Command disableLocalization = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, false, context);
        }
    };


    public void getNearbySupermarketPlaces(Object instance, int radius, int resultCount){
        SupermarketPlace.initiateSupermarketPlaceRequest(context, instance, radius, resultCount);
    }

    public void startAsyncPlaceRequest(LocationFragment locationFragment, int radius, int resultCount) {

        if(loadBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, false, context)){
            if(InternetHelper.checkInternetConnection(context)){
                getNearbySupermarketPlaces(locationFragment, radius, resultCount);
            } else {
                notificationOfNoConnectionForMap();
            }
        } else {
            showAskForLocalizationPermission();
        }

    }

    public LatLng getLastLatLng(){
        LastLocation lastLocation = LocationFinderHelper.initiateLocationFinderHelper(context).getLastLocation();
        viewLauncher.dismissDialog();

        if(lastLocation.getLatitude() == 0d){
            viewLauncher.showMessageDialog(
                    resourceProvider.getString(R.string.localization_no_place_dialog_title),
                    resourceProvider.getString(R.string.no_last_location_dialog_message),
                    resourceProvider.getString(R.string.dialog_OK),
                    getCancelProgressDialogCommand(),
                    resourceProvider.getString(R.string.dialog_retry),
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            viewLauncher.restartActivity();
                        }
                    }
            );
        }

        return new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
    }

    public GoogleMap setupGoogleMap(GoogleMap map){

        map.setMyLocationEnabled(true);
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(getLastLatLng(), 15.0f) );
        UiSettings settings = map.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);

        // add cluster to map
        mClusterManager = new ClusterManager<>(context, map);
        mClusterManager.setRenderer(new ClusterItemRendered(context, map, mClusterManager));
        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);

        return map;
    }

    public void showPlacesInGoogleMap(List<Place> places){

        if(places == null){
            return;
        }
        for(Place place : places){
            mClusterManager.addItem(new ClusterMapItem(place.getLatitude(),place.getLongitude(), place.getName()));
        }
    }

    public Place findClickedPlace(Marker marker, List<Place> places){
        return LocationFinderHelper.findClickedPlace(marker, places);
    }

    public String findClickedAddress(Place clickedPlace){
        if(clickedPlace != null)
            return LocationFinderHelper.getAddressConverted(new LatLng(clickedPlace.getLatitude(), clickedPlace.getLongitude()), context);
        else
            return null;
    }

    public String convertStatus(Status status){
       return LocationFinderHelper.convertStatus(status, context);
    }

    public String getDistanceBetweenLastLocationAndPlace(Place place, LatLng latLng){
        return LocationFinderHelper.getDistanceBetweenLastLocationAndPlace(place, latLng);
    }

    @SuppressWarnings("unchecked")
    public void notificationOfNoConnectionForMap(){

        viewLauncher.dismissDialog();

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.alert_dialog_connection_label),
                resourceProvider.getString(R.string.alert_dialog_connection_message),
                resourceProvider.getString(R.string.alert_dialog_connection_try),
                retryConnectionCheckCommand,
                resourceProvider.getString(R.string.alert_dialog_connection_cancel),
                getFinishActivityCommand()
        );
    }

    @SuppressWarnings("unchecked")
    public void notificationOfNoConnectionWithLocationPermission(){

        viewLauncher.dismissDialog();

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.localization_dialog_title),
                resourceProvider.getString(R.string.localization_no_connection_message),
                resourceProvider.getString(R.string.alert_dialog_connection_try),
                retryConnectionCheckWithLocationPermissionCommand,
                resourceProvider.getString(R.string.localization_disable_localization),
                disableLocalization
        );

    }

    @SuppressWarnings("unchecked")
    public void checkPlaceResult(List<Place> places){

        viewLauncher.dismissDialog();

        if(!LocationFinderHelper.checkPlaces(places)){
            viewLauncher.showMessageDialog(
                    resourceProvider.getString(R.string.localization_no_place_dialog_title),
                    resourceProvider.getString(R.string.no_place_dialog_message),
                    resourceProvider.getString(R.string.dialog_OK),
                    cancelProgressDialogCommand,
                    resourceProvider.getString(R.string.dialog_retry),
                    getRestartActivityCommand()
            );
        }
    }


    public void showProgressDialogWithoutButton() {

        viewLauncher.dismissDialog();

        viewLauncher.showProgressDialogWithoutButton(
                resourceProvider.getString(R.string.supermarket_finder_progress_dialog_message),
                new Command<Void>() {
                    @Override
                    public void execute(Void parameter) {
                        viewLauncher.showListOfShoppingListsActivity();
                    }
                }
        );
    }




    private void showAskForLocalizationPermission(){

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.localization_dialog_title),
                resourceProvider.getString(R.string.localization_dialog_message),
                resourceProvider.getString(R.string.yes),
                startSupermarketFinderWithPermission,
                resourceProvider.getString(R.string.no),
                withdrawLocalizationPermissionCommand
        );

    }


}
