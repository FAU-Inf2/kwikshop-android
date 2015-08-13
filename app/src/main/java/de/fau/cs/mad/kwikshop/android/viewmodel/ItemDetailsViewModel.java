package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ActivityResultEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.DeleteItemEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.greenrobot.event.EventBus;

public class ItemDetailsViewModel{

    private Context context;

    private boolean initialized = false;
    private boolean isNewItem = false;
    private int listId;
    private int itemId;
    private Item item;

    private List<Unit> units;
    private List<Group> groups;


    private final ViewLauncher viewLauncher;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<Group> groupStorage;
    private final DisplayHelper displayHelper;
    private final AutoCompletionHelper autoCompletionHelper;


    private Bitmap imageItem = null;
    private Bitmap rotateImage = null;
    private String pathImage = "";
    private Uri mImageUri;
    private String imageId = "";

    private AsyncTask<Void, Void, Void> loadTask;


    @Inject
    public ItemDetailsViewModel(ViewLauncher viewLauncher, SimpleStorage<Unit> unitStorage,
                                SimpleStorage<Group> groupStorage, DisplayHelper displayHelper, AutoCompletionHelper autoCompletionHelper){

        if(viewLauncher == null) throw new ArgumentNullException("viewLauncher");
        if(unitStorage == null) throw new ArgumentNullException("unitStorage");
        if(groupStorage == null) throw new ArgumentNullException("groupStorage");
        if(displayHelper == null) throw new ArgumentNullException("displayHelper");
        if(autoCompletionHelper == null) throw new ArgumentNullException("autoCompletionHelper");

        this.viewLauncher = viewLauncher;
        this.unitStorage = unitStorage;
        this.groupStorage = groupStorage;
        this.displayHelper = displayHelper;
        this.autoCompletionHelper = autoCompletionHelper;

    }

    public void initialize(int listId, int itemId){
        if(!initialized){
            this.listId = listId;
            this.itemId = itemId;
            if(itemId == -1){
                isNewItem = true;
            }else {
                //item = shoppingListManager.getListItem(listId, itemId);
            }
            units = unitStorage.getItems();
            groups = groupStorage.getItems();

            initialized = true;
        }
    }

    public boolean isNewItem(){
        return this.isNewItem;
    }

    public Item getItem(){ return item; }

    public String getItemName(){ return item.getName(); }

    public List<Unit> getUnits() { return units; }

    public List<Group> getGroups() { return groups; }

    public Bitmap getImageItem(){ return imageItem; }

    public Bitmap getRotateImage(){ return rotateImage; }

    public String getPathImage(){ return pathImage; }

    public Uri getmImageUri(){ return mImageUri; }

    public String getImageId(){ return imageId; }

    public void setItem(Item item){
        this.item = item;
    }

    public void setmImageUri(Uri data){
        mImageUri = data;
    }

    public void setImageItem(Bitmap imageItem){
        this.imageItem = imageItem;
    }

    public void setImageId(String imageId){
        this.imageId = imageId;
    }

    public void setPathImage(String pathImage){
        this.pathImage = pathImage;
    }

    public void setImageId(){
        imageId = item.getImageItem();
    }

    public void setRotateImage(Bitmap rotateImage){
        this.rotateImage = rotateImage;
    }

    public Unit getSelectedUnit(int index){
        return units.get(index);
    }

    public Group getSelectedGroup(int index){
        return groups.get(index);
    }

    public void setContext(Context context){ this.context = context; }


    final Command<Void> deletePositiveCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            EventBus.getDefault().post(new DeleteItemEvent(listId, itemId));
        }
    };

    final Command<Void> deleteNegativeCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            //do nothing
            //this is just so the command is executable
        }
    };

    final Command<Void> deleteCheckBoxCheckedCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG, false, context);
        }
    };

    public void sortUnitsByName(){
        Collections.sort(units, new Comparator<Unit>() {
            @Override
            public int compare(Unit lhs, Unit rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

    }

    public ArrayList<String> getUnitNames(){
        ArrayList<String> unitNames = new ArrayList<>();
        for (Unit u : units) {
            unitNames.add(displayHelper.getDisplayName(u));
        }
        return unitNames;
    }

    public Unit getSelectedUnit(){
        Unit selectedUnit = isNewItem() || item.getUnit() == null
                ? unitStorage.getDefaultValue()
                : item.getUnit();
        return selectedUnit;
    }

    public ArrayList<String> getGroupNames(){
        ArrayList<String> groupNames = new ArrayList<>();
        for (Group g : groups) {
            groupNames.add(displayHelper.getDisplayName(g));
        }
        return groupNames;
    }

    public Group getSelectedGroup(){
        Group selectedGroup = isNewItem() || item.getGroup() == null
                ? groupStorage.getDefaultValue()
                : item.getGroup();
        return selectedGroup;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        EventBus.getDefault().post(new ActivityResultEvent(requestCode, resultCode, data));
    }

    public void setImageItem(){
        if (imageItem != null) {
            item.setImageItem(imageId);
        }
        else
            item.setImageItem(null);
    }

    public void showDeleteItemDialog(String title, String message, String positiveString, String negativeString, String checkBoxMessage){
        if(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG, true, context))
            viewLauncher.showMessageDialogWithCheckbox(title, message, positiveString, deletePositiveCommand, null, null, negativeString, deleteNegativeCommand, checkBoxMessage, false, deleteCheckBoxCheckedCommand, null);
        else
            EventBus.getDefault().post(new DeleteItemEvent(listId, itemId));
    }

    public void mergeAndSaveItem(final ListManager listManager, final ItemMerger itemMerger, final Item item){
        loadTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (isNewItem()) {
                    if (!itemMerger.mergeItem(listId, item)) {
                        listManager.addListItem(listId, item);
                    }
                } else {
                    if (itemMerger.mergeItem(listId, item)) {
                        listManager.deleteItem(listId, item.getId());
                    } else {
                        listManager.saveListItem(listId, item);
                    }
                }
                return null;
            }

        };
        loadTask.execute();

    }

    public void offerAutoCompletion(String name, Group group, String brand){
        autoCompletionHelper.offerNameAndGroup(name, group);
        autoCompletionHelper.offerBrand(brand);

    }

    public void deleteItem(ListManager listManager){

        if(!isNewItem()){
            listManager.deleteItem(listId, itemId);
        }

    }

}
