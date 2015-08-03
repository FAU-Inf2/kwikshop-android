package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.greenrobot.event.EventBus;

public class ItemDetailsViewModel{

    private boolean initialized = false;
    private boolean isNewItem = false;
    private int listId;
    private int itemId;
    private ShoppingList shoppingList;
    private Item item;

    private List<Unit> units;
    private List<Group> groups;


    private final ViewLauncher viewLauncher;
    private final ListManager<ShoppingList> shoppingListManager;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<Group> groupStorage;
    private final DisplayHelper displayHelper;
    private final AutoCompletionHelper autoCompletionHelper;


    private static final int GALLERY = 1;
    private static final int GALLERY_INTENT_CALLED = 1;
    private static final int GALLERY_KITKAT_INTENT_CALLED = 0;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;


    private Bitmap imageItem = null;
    private Bitmap rotateImage = null;
    private String pathImage = "";
    private Uri mImageUri;
    private String imageId = "";


    @Inject
    public ItemDetailsViewModel(ViewLauncher viewLauncher, ListManager<ShoppingList> shoppingListManager, SimpleStorage<Unit> unitStorage,
                                SimpleStorage<Group> groupStorage, DisplayHelper displayHelper, AutoCompletionHelper autoCompletionHelper){

        if(viewLauncher == null) throw new ArgumentNullException("viewLauncher");
        if(shoppingListManager == null) throw new ArgumentNullException("shoppingListManager");
        if(unitStorage == null) throw new ArgumentNullException("unitStorage");
        if(groupStorage == null) throw new ArgumentNullException("groupStorage");
        if(displayHelper == null) throw new ArgumentNullException("displayHelper");
        if(autoCompletionHelper == null) throw new ArgumentNullException("autoCompletionHelper");

        this.viewLauncher = viewLauncher;
        this.shoppingListManager = shoppingListManager;
        this.unitStorage = unitStorage;
        this.groupStorage = groupStorage;
        this.displayHelper = displayHelper;
        this.autoCompletionHelper = autoCompletionHelper;

    }

    public void initialize(int listId, int itemId){
        if(!initialized){
            this.listId = listId;
            this.itemId = itemId;
            shoppingList = shoppingListManager.getList(listId);
            if(itemId == -1){
                isNewItem = true;
            }else {
                item = shoppingListManager.getListItem(listId, itemId);
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

    public void openVoiceRecognition(){
        viewLauncher.openVoiceRecognition(VOICE_RECOGNITION_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        EventBus.getDefault().post(new ActivityResultEvent(requestCode, resultCode, data));
    }

    public void setImageItem(){
        if (imageItem != null) {
            item.setImageItem(imageId);
        }
    }

    public void mergeAndSaveItem(){
        ItemMerger<ShoppingList> itemMerger = new ItemMerger<>(shoppingListManager);
        if(isNewItem()) {
            if(!itemMerger.mergeItem(listId, item)) {
                shoppingListManager.addListItem(listId, item);
            }
        } else {
            if(itemMerger.mergeItem(listId, item)){
                shoppingListManager.deleteItem(listId, item.getId());
            }else {
                shoppingListManager.saveListItem(listId, item);
            }
        }

    }

}