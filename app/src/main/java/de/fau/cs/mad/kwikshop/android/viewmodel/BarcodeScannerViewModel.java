package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.view.RecipeActivity;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.EANrestClient;
import de.fau.cs.mad.kwikshop.android.model.InternetHelper;
import de.fau.cs.mad.kwikshop.android.model.ItemParser;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.EANparser;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListActivity;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListFragment;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScannerViewModel extends ListViewModel<ShoppingList> implements ZXingScannerView.ResultHandler,
        EANparser.onEANparserListener,
        EANrestClient.onEANrestListener {

    private Context context;
    private Activity activity;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;
    private ZXingScannerView scannerView;
    private int listID;
    private String EAN;

    private final ListManager<ShoppingList> shoppingListManager;

    private final ListManager<Recipe> recipeManager;


    @Inject
    public BarcodeScannerViewModel(ViewLauncher viewLauncher,  ListManager<Recipe> recipeManager, ListManager<ShoppingList> shoppingListManager,
                                   SimpleStorage<Unit> unitStorage, SimpleStorage<Group> groupStorage,
                                   ItemParser itemParser, DisplayHelper displayHelper,
                                   AutoCompletionHelper autoCompletionHelper, LocationFinderHelper locationFinderHelper,
                                   ResourceProvider resourceProvider) {

        super(viewLauncher, shoppingListManager, unitStorage, groupStorage, itemParser, displayHelper, autoCompletionHelper, locationFinderHelper, null);

        if(resourceProvider == null) {throw new ArgumentNullException("resourceProvider");}

        this.recipeManager = recipeManager;
        this.shoppingListManager = shoppingListManager;
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
            if (InternetHelper.checkInternetConnection(context))
                viewLauncher.showLocationActivity();
            else
                notificationOfNoConnection();
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
        return InternetHelper.checkInternetConnection(context);
    }

    public void setListId(int listID){
        this.listID = listID;
    }

    public void hideActionBar(){
        ((AppCompatActivity)activity).getSupportActionBar().hide();
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
            viewLauncher.showProgressDialog(resourceProvider.getString(R.string.barcode_scanner_progress_message), null, false, null);

            // parse website to get information about the scanned product
            parseWebsite(EAN);

            // target activity after scan is finished
            if(!activity.getClass().getSimpleName().equals("RecipeActivity")){
                startShoppingListActivityWithoutSupermarketRequest();
            } else {
                startRecipeList();
            }

        } else {
            Toast.makeText(context, resourceProvider.getString(R.string.barcode_scanner_not_ean_format), Toast.LENGTH_LONG).show();
            scannerView.startCamera();
        }
    }

    // parser result
    @Override
    public void handleParserResponse(Item item) {
        if(!item.getName().isEmpty()){
            //Toast.makeText(context,  "Debug: " + item.getName() + " Found on: opengtindb.org", Toast.LENGTH_LONG).show();
            addItem(item);
            viewLauncher.dismissDialog();
        } else {
            getRestResponse(EAN);
        }

    }

    // rest client result
    @Override
    public void handleRESTresponse(Item item) {
        if(!item.getName().equals("null")){
            //Toast.makeText(context, "Debug: " + item.getName() + " Found on: outpan.com", Toast.LENGTH_LONG).show();
            addItem(item);
        } else {
            Toast.makeText(context, resourceProvider.getString(R.string.barcode_scanner_no_product), Toast.LENGTH_LONG).show();
        }
        viewLauncher.dismissDialog();
    }

    public void addItem(Item item) {

        item.setUnit(unitStorage.getDefaultValue());
        item.setGroup(groupStorage.getDefaultValue());

        if(!activity.getClass().getSimpleName().equals("RecipeActivity")){
            if (!new ItemMerger<>(shoppingListManager).mergeItem(listID, item))
                shoppingListManager.addListItem(listID, item);
        } else {
            if (!new ItemMerger<>(recipeManager).mergeItem(listID, item))
                recipeManager.addListItem(listID, item);
        }
    }

    private void startShoppingListActivityWithoutSupermarketRequest() {
        Intent intent = new Intent(context, ShoppingListActivity.class);
        intent.putExtra(ShoppingListActivity.SHOPPING_LIST_ID, listID);
        intent.putExtra(ShoppingListFragment.DO_NOT_ASK_FOR_SUPERMARKET, true);
        viewLauncher.startActivity(intent);
    }

    private void startRecipeList() {
        viewLauncher.startActivity(RecipeActivity.getIntent(context, listID));
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
