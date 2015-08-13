package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.ItemParser;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.SupermarketPlace;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListActivity;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import se.walkercrou.places.Place;
import se.walkercrou.places.Status;

public class LocationViewModel extends ListViewModel<ShoppingList> {

    private Activity activity;
    private Context context;
    private int listId;
    private int placeChoiceIndex;
    private List<Place> places;
    private boolean cancelSelectionOfSupermarket = false;

    private Boolean canceled = false;

    private final ViewLauncher viewLauncher;
    private final ListManager<ShoppingList> shoppingListManager;
    private final ResourceProvider resourceProvider;

    public static String SHOPPINGMODEPLACEREQUEST_CANCEL = "ShoppingModePlaceRequest_cancel";


    @Inject
    public LocationViewModel(ViewLauncher viewLauncher,
                             ListManager<ShoppingList> shoppingListManager,
                             SimpleStorage<Unit> unitStorage,
                             SimpleStorage<Group> groupStorage,
                             ItemParser itemParser,
                             DisplayHelper displayHelper,
                             AutoCompletionHelper autoCompletionHelper,
                             LocationFinderHelper locationFinderHelper,
                             ResourceProvider resourceProvider,
                             RegularlyRepeatHelper repeatHelper) {

        super(viewLauncher, shoppingListManager, unitStorage, groupStorage, itemParser, displayHelper,
                autoCompletionHelper, locationFinderHelper);

        if(resourceProvider == null) {throw new ArgumentNullException("resourceProvider");}

        this.resourceProvider = resourceProvider;
        this.viewLauncher = viewLauncher;
        this.shoppingListManager = shoppingListManager;
    }

    public void setCancelSelectionOfSupermarket(boolean status){ this.cancelSelectionOfSupermarket = status; }

    public void setPlaces(List<Place> places){ this.places = places; }

    public void setListId(int listId){ this.listId = listId; }

    public void setContext(Context context){ this.context = context; }

    public void setActivity(Activity activity){ this.activity = activity; }

    public Boolean isCanceld(){ return canceled; }

    public Command<Void> getCancelProgressDialogCommand(){ return cancelProgressDialogCommand; }

    public void setPlaceChoiceIndex(int choice){ this.placeChoiceIndex = choice; }

    @Override
    protected void loadList() {

    }

    @Override
    protected void addItemCommandExecute() {

    }

    @Override
    protected void selectItemCommandExecute(int parameter) {

    }

    public Command<Void> getFinishActivityCommand(){ return finishCommand; }

    public Command<String> getRouteIntentCommand(){ return routeIntentCommand; }

    public Command<Void> getRestartActivityCommand(){ return restartActivityCommand; }

    public Command<Void> getSavePlaceToShoppingListCommand(){ return savePlaceToShoppingListCommand; }

    public Command<Void> getAcceptDialogCommand() {return acceptDialogCommand;  }

    public Command<Integer> getStartShoppingListFragmemtWithoutPlaceRequestCommand(){ return startShoppingListFragmemtWithoutPlaceRequestCommand; }


    final Command cancelProgressDialogCommand =  new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            canceled = true;
            viewLauncher.showListOfShoppingListsActivity();
        }
    };

    final Command finishCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            viewLauncher.finishActivity();
        }
    };

    final Command routeIntentCommand = new Command<String>(){
        @Override
        public void execute(String targetAddress) {
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + targetAddress));
            viewLauncher.startActivity(intent);
        }
    };

    final Command restartActivityCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            viewLauncher.startActivity(activity.getIntent());
        }
    };

    final Command savePlaceToShoppingListCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            // save place to shopping list
        }
    };

    final Command acceptDialogCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {

        }
    };

    final Command startShoppingListFragmemtWithoutPlaceRequestCommand = new Command<Integer>(){
        @Override
        public void execute(Integer listId) {
            startShoppingListFragmentWithoutPlaceRequest(listId.intValue());
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


    final Command retryConnectionCheck = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            if(viewLauncher.checkInternetConnection())
                viewLauncher.showLocationActivity();
            else {
                notificationOfNoConnectionWithLocationPermission();
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

        return map;
    }

    public void showPlacesInGoogleMap(List<Place> places, GoogleMap map){
        if(places == null){
            return;
        }
        for(Place place : places){
            IconGenerator iconFactory = new IconGenerator(activity.getApplicationContext());
            MarkerOptions markerOptions = new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(place.getName()))).
                    position(new LatLng(place.getLatitude(), place.getLongitude())).
                    anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
            map.addMarker(markerOptions);
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
    public void notificationOfNoConnection(){

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.alert_dialog_connection_label),
                resourceProvider.getString(R.string.alert_dialog_connection_message),
                resourceProvider.getString(R.string.alert_dialog_connection_try),
                retryConnectionCheck,
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
                retryConnectionCheck,
                resourceProvider.getString(R.string.localization_disable_localization),
                disableLocalization
        );

    }

    public void showProgressDialogWithListID(int listID){

        viewLauncher.showProgressDialogWithListID(
                resourceProvider.getString(R.string.supermarket_finder_progress_dialog_message),
                resourceProvider.getString(R.string.cancel),
                listID,
                true,
                getStartShoppingListFragmemtWithoutPlaceRequestCommand()
        );
    }

    public void showNoPlaceWasFoundDialog(){

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.no_place_dialog_title),
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
                if(placeChoiceIndex > 0){
                    Place selectedPlace = places.get(placeChoiceIndex - 1);
                    // set new lastLocation object
                    LastLocation lastLocation = new LastLocation();
                    lastLocation.setName(selectedPlace.getName());
                    lastLocation.setLatitude(selectedPlace.getLatitude());
                    lastLocation.setLongitude(selectedPlace.getLongitude());
                    lastLocation.setAddress(selectedPlace.getAddress());
                    lastLocation.setTimestamp(System.currentTimeMillis());
                    item.setLocation(lastLocation);
                    listManager.saveListItem(listId, item);
                }
            }
        }
    }

}
