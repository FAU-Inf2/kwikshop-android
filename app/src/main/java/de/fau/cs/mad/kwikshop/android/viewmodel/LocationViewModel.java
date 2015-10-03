package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;


import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.SupermarketPlace;
import de.fau.cs.mad.kwikshop.android.util.ClusterMapItem;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.util.ClusterItemRendered;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import se.walkercrou.places.Place;
import se.walkercrou.places.Status;

import static de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper.*;

public class LocationViewModel implements OnMapReadyCallback, SupermarketPlace.AsyncPlaceRequestListener  {

    private Context context;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;
    private ClusterManager<ClusterMapItem> mClusterManager;
    private LocationViewModel locationViewModel;
    private List<Place> places;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private View rootView;
    private String searchAddress;
    private LatLng location;
    private LatLng searchLocation = null;


    RelativeLayout mapInfoBox;
    TextView mapPlaceName;
    TextView mapPlaceOpenStatus;
    TextView mapPlaceDistance;
    View mapDirectionButton;

    private final static int RESULT_COUNT = 40;
    private final static int MAX_RADIUS = 50000;
    private final static int MIN_RADIUS = 500;

    @Inject
    LocationFinderHelper locationFinderHelper;

    @Inject
    public LocationViewModel(ResourceProvider resourceProvider, ViewLauncher viewLauncher) {

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        if(viewLauncher == null) {
            throw new ArgumentNullException("viewLauncher");
        }

        locationViewModel = this;
        this.resourceProvider = resourceProvider;
        this.viewLauncher = viewLauncher;
    }

    public void setContext(Context context){ this.context = context; }

    public void setMapFragment(SupportMapFragment mapFragment){this.mapFragment = mapFragment;}

    public void setSearchAddress(String searchAddress) {this.searchAddress = searchAddress;}

    public void setView(View view){
        this.rootView = view;
        wireUpView();
    }

    public Command<Void> getCancelProgressDialogCommand(){ return cancelProgressDialogCommand; }

    public Command<Void> getFinishActivityCommand(){ return finishCommand; }

    public Command<String> getRouteIntentCommand(){ return routeIntentCommand; }

    public Command<Void> getRestartActivityCommand(){ return restartActivityCommand; }

    public Command<Void> getStartPlaceRequestCommand(){ return startPlaceRequestCommand; }


    final Command<Void> startPlaceRequestCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            showProgressDialogWithoutButton();
            hideInfoBox();
            startAsyncPlaceRequest(locationViewModel);
        }
    };

    final Command<Void> installGooglePlayServiceCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            if(showListOfShoppingListCommand.getCanExecute())
            showListOfShoppingListCommand.execute(null);

            final String appPackageName = "com.google.android.gms";
            try {
                viewLauncher.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                viewLauncher.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    };

    final Command<Void> showListOfShoppingListCommand =   new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            viewLauncher.showListOfShoppingListsActivity();
        }
    };

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
        if(searchAddress != null){
            searchLocation = locationFinderHelper.getAddressFromString(searchAddress);
        }
        SupermarketPlace.initiateSupermarketPlaceRequest(context, instance, radius, resultCount, searchLocation);
    }

    public void startAsyncPlaceRequest(LocationViewModel locationViewModel) {

        if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS){
            if(loadBoolean(SharedPreferencesHelper.LOCATION_PERMISSION, false, context)){
                if(InternetHelper.checkInternetConnection(context)){
                    getNearbySupermarketPlaces(locationViewModel, getRadius(), RESULT_COUNT);
                } else {
                    notificationOfNoConnectionForMap();
                }
            } else {
                showAskForLocalizationPermission();
            }
        } else {
            if(mapFragment.getView()!= null){
                mapFragment.getView().setVisibility(View.INVISIBLE);
            }
            notificationOfNoPlayServiceInstalled();
        }
    }


    @Override
    public void postResult(List<Place> mPlaces) {

        viewLauncher.dismissDialog();
        places = mPlaces;

        if (mapFragment != null){
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        setupGoogleMap(map);
        showPlacesInGoogleMap(places);
        viewLauncher.dismissDialog();

        if(!LocationFinderHelper.checkPlaces(places)){
            notificationOfNoPlaceResult();
        }

        onMarkerClickHandler();
        onMapClickHandler();
    }


    private void wireUpView(){
        mapInfoBox = (RelativeLayout) rootView.findViewById(R.id.map_infobox);
        mapPlaceName = (TextView) rootView.findViewById(R.id.map_place_name);
        mapPlaceOpenStatus = (TextView) rootView.findViewById(R.id.map_place_open_status);
        mapPlaceDistance = (TextView) rootView.findViewById(R.id.map_place_distance);
        mapDirectionButton =  rootView.findViewById(R.id.direction_button);
    }

    private void onMarkerClickHandler(){

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final Place clickedPlace = findClickedPlace(marker, places);
                final String clickedAddress = findClickedAddress(clickedPlace);
                showInfoBox();
                mapPlaceName.setText(clickedPlace.getName());
                mapPlaceOpenStatus.setText(convertStatus(clickedPlace.getStatus()));
                mapPlaceDistance.setText(getDistanceBetweenLastLocationAndPlace(clickedPlace, getLastLatLng()));
                mapDirectionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getRouteIntentCommand().execute(clickedAddress);
                    }
                });
                return false;
            }
        });
    }


    private void onMapClickHandler(){
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

    public void hideInfoBox(){
        mapInfoBox.setVisibility(View.INVISIBLE);
        mapDirectionButton.setVisibility(View.INVISIBLE);
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

    public int getRadius(){
        int defaultRadius = resourceProvider.getInteger(R.integer.supermarket_finder_radius);
        return SharedPreferencesHelper.loadInt(SharedPreferencesHelper.SUPERMARKET_FINDER_RADIUS, defaultRadius, context);
    }

    public void changeRadius(){

            final int defaultValue = resourceProvider.getInteger(R.integer.supermarket_finder_radius);
            final int currentValue = loadInt(SUPERMARKET_FINDER_RADIUS, defaultValue, context);

            viewLauncher.showNumberInputDialog(

                    // title and message
                    resourceProvider.getString(R.string.radius),
                    resourceProvider.getString(R.string.setting_change_radius_description),

                    //current value
                    currentValue,

                    // ok button: save updated value
                    resourceProvider.getString(android.R.string.ok), new Command<String>() {
                        @Override
                        public void execute(String value) {

                            try {

                                int intValue = Integer.parseInt(value);

                                if(intValue != currentValue && intValue > 0) {

                                    if(intValue > MAX_RADIUS){
                                        saveInt(SUPERMARKET_FINDER_RADIUS, MAX_RADIUS, context);
                                    } else if(intValue < MIN_RADIUS){
                                        saveInt(SUPERMARKET_FINDER_RADIUS, MIN_RADIUS, context);
                                    } else {
                                        saveInt(SUPERMARKET_FINDER_RADIUS, intValue, context);
                                    }
                                }


                            } catch (NumberFormatException ex) {
                                // should not happen, because showNumberInputDialog() only allows digits as input
                                // => just ignore the error
                            }

                            viewLauncher.restartActivity();
                        }
                    },

                    // reset to default value (neutral button)
                    resourceProvider.getString(R.string.str_default),
                    new Command<String>() {
                        @Override
                        public void execute(String parameter) {

                            if(defaultValue != currentValue) {
                                saveInt(SUPERMARKET_FINDER_RADIUS, defaultValue, context);
                            }
                            viewLauncher.restartActivity();
                        }
                    },

                    // cancel button
                    resourceProvider.getString(android.R.string.cancel),
                    NullCommand.StringInstance
            );

    }

    public void setupGoogleMap(GoogleMap map){

        if(searchLocation != null){
            location = searchLocation;
        } else {
            location = getLastLatLng();
        }

        //setup map
        map.setMyLocationEnabled(true);
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(location, 15.0f) );
        UiSettings settings = map.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setMapToolbarEnabled(false);

        // add cluster to map
        mClusterManager = new ClusterManager<>(context, map);
        mClusterManager.setRenderer(new ClusterItemRendered(context, map, mClusterManager));
        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);

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
    public void notificationOfNoPlaceResult() {
        viewLauncher.dismissDialog();

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.localization_no_place_dialog_title),
                resourceProvider.getString(R.string.no_place_dialog_message),
                resourceProvider.getString(R.string.dialog_OK),
                cancelProgressDialogCommand,
                resourceProvider.getString(R.string.dialog_retry),
                getRestartActivityCommand()
        );


    }


    public void showProgressDialogWithoutButton() {

        viewLauncher.dismissDialog();

        String dialogMessage;
        if(searchAddress == null){
            dialogMessage  = resourceProvider.getString(R.string.supermarket_finder_progress_dialog_message);
        } else {
            dialogMessage = resourceProvider.getString(R.string.supermarket_finder_search_progress_dialog_message) + searchAddress;
        }

        viewLauncher.showProgressDialogWithoutButton(
                dialogMessage,
                showListOfShoppingListCommand
        );
    }

    public void notificationOfNoPlayServiceInstalled(){

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.no_play_service_installed_title),
                resourceProvider.getString(R.string.no_play_service_installed_message),
                resourceProvider.getString(R.string.install),
                installGooglePlayServiceCommand,
                resourceProvider.getString(R.string.cancel),
                showListOfShoppingListCommand
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


    public void selectPlaceType(){

        boolean supermarketIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_SUPERMARKET, true, context);
        boolean bakeryIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_BAKERY, false, context);
        boolean gasStationIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_GAS_STATION, false, context);
        boolean liquorStoreIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_LIQUOR_STORE, false, context);
        boolean pharmacyIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_PHARMACY, false, context);
        boolean shoppingMallIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_SHOPPING_MALL, false, context);
        boolean floristIsEnabled = SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.STORE_TYPE_STORE, false, context);


        boolean[] storeTypeStatus = new boolean[]{
                supermarketIsEnabled,
                bakeryIsEnabled,
                gasStationIsEnabled,
                liquorStoreIsEnabled,
                pharmacyIsEnabled,
                shoppingMallIsEnabled,
                floristIsEnabled
        };

        viewLauncher.showMultiplyChoiceDialog(
                resourceProvider.getString(R.string.localization_store_types_dialog_title),
                resourceProvider.getStringArray(R.array.store_types_array),
                storeTypeStatus,
                //select command
                new Command<Integer>() {
                    @Override
                    public void execute(Integer selection) {
                        switch(selection){
                            case 0:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_SUPERMARKET, true, context);
                                break;
                            case 1:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_BAKERY, true, context);
                                break;
                            case 2:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_GAS_STATION, true, context);
                                break;
                            case 3:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_LIQUOR_STORE, true, context);
                                break;
                            case 4:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_PHARMACY, true, context);
                                break;
                            case 5:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_SHOPPING_MALL, true, context);
                                break;
                            case 6:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_STORE, true, context);
                                break;

                        }

                    }
                },
                //deselect command
                new Command<Integer>() {
                    @Override
                    public void execute(Integer deSelection) {
                        switch(deSelection){
                            case 0:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_SUPERMARKET, false, context);
                                break;
                            case 1:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_BAKERY, false, context);
                                break;
                            case 2:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_GAS_STATION, false, context);
                                break;
                            case 3:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_LIQUOR_STORE, false, context);
                                break;
                            case 4:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_PHARMACY, false, context);
                                break;
                            case 5:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_SHOPPING_MALL, false, context);
                                break;
                            case 6:
                                SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.STORE_TYPE_STORE, false, context);
                                break;
                        }

                    }
                },
                //positive command
                resourceProvider.getString(R.string.dialog_OK),
                new Command<Void>() {
                    @Override
                    public void execute(Void parameter) {
                        viewLauncher.restartActivity();
                    }
                },
                //negative command
                resourceProvider.getString(R.string.cancel),
                new Command<Void>() {
                    @Override
                    public void execute(Void parameter) {

                    }
                }
        );
    }



}
