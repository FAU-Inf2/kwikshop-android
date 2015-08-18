package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import javax.inject.Inject;
import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.DefaultDataProvider;
import de.fau.cs.mad.kwikshop.android.model.EANrestClient;
import de.fau.cs.mad.kwikshop.android.model.ItemParser;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.EANparser;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
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
            viewLauncher.showShoppingList(listID);
        } else {
            Toast.makeText(context, "Not a EAN Barcode Format: " + result.getBarcodeFormat().name(), Toast.LENGTH_LONG).show();
            scannerView.startCamera();
        }
    }

    // parser result
    @Override
    public void handleParserResult(Item item) {
        if(!item.getName().isEmpty()){
            Toast.makeText(context,  "Name: " + item.getName() + "Found on: opengtindb.org", Toast.LENGTH_LONG).show();
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
            Toast.makeText(context, "Name: " + item.getName() + "Found on: outpan.com", Toast.LENGTH_LONG).show();
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
