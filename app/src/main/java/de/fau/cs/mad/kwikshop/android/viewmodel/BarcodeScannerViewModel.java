package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.DefaultDataProvider;
import de.fau.cs.mad.kwikshop.android.model.EANrestClient;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;
import de.fau.cs.mad.kwikshop.android.model.ItemParser;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.EANparser;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListActivity;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScannerViewModel extends ListViewModel<ShoppingList> implements ZXingScannerView.ResultHandler,
        EANparser.onEANParserResponseListener,
        EANrestClient.onEANrestResponse {

    private Context context;
    private Activity activity;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;
    private ZXingScannerView scannerView;
    private int listID;
    private String EAN;


    @Inject
    public BarcodeScannerViewModel(ViewLauncher viewLauncher, ListManager<ShoppingList> listManager,
                                   SimpleStorage<Unit> unitStorage, SimpleStorage<Group> groupStorage,
                                   ItemParser itemParser, DisplayHelper displayHelper,
                                   AutoCompletionHelper autoCompletionHelper, LocationFinderHelper locationFinderHelper,
                                   ResourceProvider resourceProvider) {

        super(viewLauncher, listManager, unitStorage, groupStorage, itemParser, displayHelper, autoCompletionHelper, locationFinderHelper);

        if(resourceProvider == null) {throw new ArgumentNullException("resourceProvider");}
        this.resourceProvider = resourceProvider;
        this.viewLauncher = viewLauncher;
    }

    public ZXingScannerView getScannerView(){
        scannerView = new ZXingScannerView(context);
        return scannerView ;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public void setActivity(Activity activity){
        this.activity = activity;
    }

    final Command retryConnectionCheck = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            if(viewLauncher.checkInternetConnection())
                viewLauncher.showLocationActivity();
            else {
                notificationOfNoConnection();
            }
        }
    };

    final Command dismissDialogCommand = new Command<Void>(){
        @Override
        public void execute(Void parameter) {
            viewLauncher.dismissDialog();
        }
    };

    @SuppressWarnings("unchecked")
    public void notificationOfNoConnection(){

        viewLauncher.showMessageDialog(
                resourceProvider.getString(R.string.alert_dialog_connection_label),
                resourceProvider.getString(R.string.alert_dialog_connection_message),
                resourceProvider.getString(R.string.alert_dialog_connection_try),
                retryConnectionCheck,
                resourceProvider.getString(R.string.alert_dialog_connection_cancel),
                dismissDialogCommand
        );
    }
    public boolean checkInternetConnection(){
        return viewLauncher.checkInternetConnection();
    }

    public void setListId(int listID){
        this.listID = listID;
    }

    public void hideActionBar(){
        ((ActionBarActivity)context).getSupportActionBar().hide();
    }

    public void onPause(){
        scannerView.stopCamera();
    }

    public void onResume(){
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    public void parseWebsite(String EAN){
        EANparser.initiateOpenEANparserRequest(context).parseWebsite(EAN, this);
    }

    public void getRestResponse(String EAN){
        EANrestClient.initiateEANrest(context).getRestResponse(EAN, this);
    }

    // barcode result
    @Override
    public void handleResult(Result result) {
        if(result.getBarcodeFormat() == BarcodeFormat.EAN_13 || result.getBarcodeFormat() == BarcodeFormat.EAN_8) {
            EAN = result.getText();
            viewLauncher.showProgressDialog("Fetching Data...", null, false, null);
            parseWebsite(EAN);
            startShoppingListActivityWithoutSupermarketRequest();
        } else {
            Toast.makeText(context, "Not a EAN Barcode Format: " + result.getBarcodeFormat().name(), Toast.LENGTH_LONG).show();
            scannerView.startCamera();
        }
    }



    // parser result
    @Override
    public void handleParserResult(Item item) {
        if(!item.getName().isEmpty()){
            Toast.makeText(context,  "Debug: " + item.getName() + "Found on: opengtindb.org", Toast.LENGTH_LONG).show();
            addItemToShoppingList(item);
        } else {
            getRestResponse(EAN);
        }
        viewLauncher.dismissProgressDialog();
    }


    // rest client result
    @Override
    public void handleRESTresponse(Item item) {
        if(!item.getName().isEmpty()){
            Toast.makeText(context, "Debug: " + item.getName() + "Found on: outpan.com", Toast.LENGTH_LONG).show();
            addItemToShoppingList(item);
        } else {
            Toast.makeText(context, "No product found for EAN", Toast.LENGTH_LONG).show();
        }
        viewLauncher.dismissProgressDialog();
    }


    public void addItemToShoppingList(final Item item){

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                item.setUnit(unitStorage.getDefaultValue());
                item.setGroup(groupStorage.getDefaultValue());

                if(!itemMerger.mergeItem(listID, item))
                    listManager.addListItem(listID, item);


                return null;
            }
        }.execute();

    }

    private void startShoppingListActivityWithoutSupermarketRequest() {
        Intent intent = new Intent(context, ShoppingListActivity.class);
        intent.putExtra(ShoppingListActivity.SHOPPING_LIST_ID, listID);
        intent.putExtra(LocationViewModel.SHOPPINGMODEPLACEREQUEST_CANCEL, true);
        viewLauncher.startActivity(intent);
    }


    @Override
    protected void loadList() {

    }

    @Override
    protected void addItemCommandExecute() {

    }

    @Override
    protected void selectItemCommandExecute(int parameter) {

    }



}
