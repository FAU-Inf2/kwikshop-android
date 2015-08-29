package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.app.Activity;
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
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.LocationManager;
import de.fau.cs.mad.kwikshop.android.model.SupermarketPlace;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.util.ClusterMapItem;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.util.ClusterItemRendered;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListActivity;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import se.walkercrou.places.Place;
import se.walkercrou.places.Status;

public class LocationViewModel {

    private Activity activity;
    private Context context;
    private int listId;
    private int placeChoiceIndex;
    private List<Place> places;
    private boolean cancelSelectionOfSupermarket = false;


    private Boolean canceled = false;

    private final ListManager<ShoppingList> listManager;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;
    private final LocationManager locationManager;

    public static String SHOPPINGMODEPLACEREQUEST_CANCEL = "ShoppingModePlaceRequest_cancel";
    private ClusterManager<ClusterMapItem> mClusterManager;


    @Inject
    public LocationViewModel(ListManager<ShoppingList> shoppingListManager,
                             ResourceProvider resourceProvider, LocationManager locationManager,
                             ViewLauncher viewLauncher) {

        if(shoppingListManager ==null) {
            throw new ArgumentNullException("shoppingListManager");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if(locationManager == null) {
            throw new ArgumentNullException("locationManager");
        }

        if(viewLauncher == null) {
            throw new ArgumentNullException("viewLauncher");
        }

        this.listManager = shoppingListManager;
        this.resourceProvider = resourceProvider;
        this.locationManager = locationManager;
        this.viewLauncher = viewLauncher;
    }


    public void setCancelSelectionOfSupermarket(boolean status){ this.cancelSelectionOfSupermarket = status; }

    public void setPlaces(List<Place> places){ this.places = places; }

    public void setListId(int listId){ this.listId = listId; }

    public void setContext(Context context){ this.context = context; }

    public void setActivity(Activity activity){ this.activity = activity; }

    public Boolean isCanceld(){ return canceled; }

    public Command<Void> getCancelProgressDialogCommand(){ return cancelProgressDialogCommand; }

    public void setPlaceChoiceIndex(int choice){ this.placeChoiceIndex = choice; }

    public int getPlaceChoiceIndex() { return this.placeChoiceIndex; }


    public Command<Void> getFinishActivityCommand(){ return finishCommand; }

    public Command<String> getRouteIntentCommand(){ return routeIntentCommand; }

    public Command<Void> getRestartActivityCommand(){ return restartActivityCommand; }

    public Command<Void> getSavePlaceToShoppingListCommand(){ return savePlaceToShoppingListCommand; }

    public Command<Void> getAcceptDialogCommand() {return acceptDialogCommand;  }

    public Command<Integer> getStartShoppingListFragmemtWithoutPlaceRequestCommand(){ return startShoppingListFragmemtWithoutPlaceRequestCommand; }


    final Command<Void> cancelProgressDialogCommand =  new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            canceled = true;
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

    final Command<Void> restartActivityCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            viewLauncher.startActivity(activity.getIntent());
        }
    };

    final Command<Void> savePlaceToShoppingListCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            // save place to shopping list
        }
    };

    final Command<Void> acceptDialogCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {

        }
    };

    final Command<Integer> startShoppingListFragmemtWithoutPlaceRequestCommand = new Command<Integer>(){
        @Override
        public void execute(Integer listId) {
            startShoppingListFragmentWithoutPlaceRequest(listId);
        }
    };


    final Command grantLocalizationPermissionCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, true, context);
            Intent intent = ShoppingListActivity.getIntent(context, listId);
            viewLauncher.startActivity(intent);
        }
    };

    final Command withdrawLocalizationPermissionCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, false, context);
        }
    };

    final Command doMotShowLocalizationPermissionAgainCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION_SHOW_AGAIN_MSG, false, context );
        }
    };

    final Command showLocalizationPermissionAgainCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.LOCATION_PERMISSION_SHOW_AGAIN_MSG, true, context );
        }
    };

    final Command selectionOfPlaceChoiceCommand = new Command<Integer>(){
        @Override
        public void execute(Integer index) {
            setPlaceChoiceIndex(index);
        }
    };

    final Command cancelSelectionOfPlaceChoiceCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            setCancelSelectionOfSupermarket(true);
        }
    };


    final Command retryConnectionCheckWithLocationPermissionCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            if(viewLauncher.checkInternetConnection())
                viewLauncher.showLocationActivity();
            else {
                notificationOfNoConnectionWithLocationPermission();
            }
        }
    };

    final Command retryConnectionCheckCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            if(viewLauncher.checkInternetConnection())
                viewLauncher.showLocationActivity();
            else {
               notificationOfNoConnectionForMap();
            }
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

    public LatLng getLastLatLng(){
        LastLocation lastLocation = LocationFinderHelper.initiateLocationFinderHelper(context).getLastLocation();
        return new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
    }

    public GoogleMap setupGoogleMap(GoogleMap map){


        map.setMyLocationEnabled(true);
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(getLastLatLng(), 15.0f) );
        UiSettings settings = map.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);

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

    public String converStatus(Status status){
       return LocationFinderHelper.convertStatus(status, context);
    }

    public String getDistanceBetweenLastLocationAndPlace(Place place, LatLng latLng){
        return LocationFinderHelper.getDistanceBetweenLastLocationAndPlace(place, latLng);
    }

    @SuppressWarnings("unchecked")
    public void notificationOfNoConnectionForMap(){

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

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.localization_dialog_title),
                resourceProvider.getString(R.string.localization_no_connection_message),
                resourceProvider.getString(R.string.alert_dialog_connection_try),
                retryConnectionCheckWithLocationPermissionCommand,
                resourceProvider.getString(R.string.localization_disable_localization),
                disableLocalization
        );

    }

    public void showProgressDialogWithListID(int listID){

        /*
        viewLauncher.showProgressDialogWithListID(
                resourceProvider.getString(R.string.supermarket_finder_progress_dialog_message),
                resourceProvider.getString(R.string.cancel),
                listID,
                true,
                getStartShoppingListFragmemtWithoutPlaceRequestCommand()
        );
        */

        viewLauncher.showProgressDialogWithListID(
                resourceProvider.getString(R.string.supermarket_finder_progress_dialog_message),
                null,
                listID,
                true,
                null
        );
    }

    public void showNoPlaceWasFoundDialog(){

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.localization_no_place_dialog_title),
                resourceProvider.getString(R.string.no_place_dialog_message),
                resourceProvider.getString(R.string.dialog_OK),
                getAcceptDialogCommand(),
                resourceProvider.getString(R.string.dialog_retry),
                getRestartActivityCommand()
        );

    }

    @SuppressWarnings("unchecked")
    public void showSelectCurrentSupermarket(List<Place> places){

        CharSequence[] placeNames = getNamesFromPlaces(places);

        viewLauncher.showMessageDialogWithRadioButtons(
                resourceProvider.getString(R.string.localization_supermarket_select_dialog_title),
                placeNames,
                resourceProvider.getString(R.string.dialog_OK),
                getSavePlaceToShoppingListCommand(),
                resourceProvider.getString(R.string.dialog_retry),
                getRestartActivityCommand(),
                resourceProvider.getString(R.string.cancel),
                cancelSelectionOfPlaceChoiceCommand,
                selectionOfPlaceChoiceCommand

        );
    }

    @SuppressWarnings("unchecked")
    public void showAskForLocalizationPermission(){

        viewLauncher.showMessageDialogWithCheckbox(
                resourceProvider.getString(R.string.localization_dialog_title),
                resourceProvider.getString(R.string.localization_dialog_message),
                resourceProvider.getString(R.string.yes),
                grantLocalizationPermissionCommand,
                null,
                null,
                resourceProvider.getString(R.string.no),
                withdrawLocalizationPermissionCommand,
                resourceProvider.getString(R.string.dont_show_this_message_again),
                false,
                doMotShowLocalizationPermissionAgainCommand,
                showLocalizationPermissionAgainCommand
        );

    }

    public CharSequence[] getNamesFromPlaces(List<Place> places){
        return LocationFinderHelper.getNamesFromPlaces(places, context);
    }


    public boolean checkPlaces(List<Place> places){
        return LocationFinderHelper.checkPlaces(places);
    }

    public void startShoppingListFragmentWithoutPlaceRequest(int listId){
        Intent intent = ShoppingListActivity.getIntent(context, listId);
        intent.putExtra(SHOPPINGMODEPLACEREQUEST_CANCEL, true);
        viewLauncher.startActivity(intent);
    }

    public void dismissProgressDialog(){
        viewLauncher.dismissProgressDialog();
    }

    public void dismissDialog(){
        viewLauncher.dismissDialog();
    }

    public boolean checkInternetConnection(){
        return viewLauncher.checkInternetConnection();
    }

    public void setLocationOnItemBought(Item item){

        if(!cancelSelectionOfSupermarket){
            if(checkPlaces(places)){
                // not listed supermarket selection has index 0
                Place selectedPlace = places.get(placeChoiceIndex);

                // get location object for the place
                LastLocation location = locationManager.getLocationForPlace(selectedPlace);
                item.setLocation(location);
                listManager.saveListItem(listId, item);

            }
        }
    }

}
