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
import de.fau.cs.mad.kwikshop.android.model.ItemParser;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.OpenEANparser;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListFragment;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScannerViewModel extends ListViewModel<ShoppingList> implements ZXingScannerView.ResultHandler, OpenEANparser.onEANParserResponseListener {

    private Context context;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;
    private ZXingScannerView scannerView;
    private int listID;


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
        Log.e("BSVM", "ListID11: " + listID);
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
        OpenEANparser.initiateOpenEANparserRequest(context).parseWebsite(EAN, this);
    }



    @Override
    public void handleResult(Result result) {
        if(result.getBarcodeFormat() == BarcodeFormat.EAN_13 || result.getBarcodeFormat() == BarcodeFormat.EAN_8) {
            parseWebsite(result.getText());
            Log.e("BSVM", "ListID: " + listID);
            viewLauncher.showShoppingList(listID);
        } else {
            Toast.makeText(context, "Not a EAN Barcode Format", Toast.LENGTH_LONG).show();
            scannerView.startCamera();
        }
    }


    @Override
    public void handleParserResult(Item parsedItem) {
        if(!parsedItem.getName().isEmpty()){
            Toast.makeText(context, parsedItem.getName(), Toast.LENGTH_LONG).show();
            addItemFromParser(parsedItem);
        } else {
            Toast.makeText(context, "No product found for EAN", Toast.LENGTH_LONG).show();
            viewLauncher.showShoppingList(listID);
        }

    }

    public void addItemFromParser(final Item parsedItem){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                // TODO Merge Items
                listManager.addListItem(listID, parsedItem);
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
