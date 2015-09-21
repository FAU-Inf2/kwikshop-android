package de.fau.cs.mad.kwikshop.android.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesWrapper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewModelBase;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.common.util.ObjectHelper;
import de.fau.cs.mad.kwikshop.common.util.StringHelper;

public abstract class ItemDetailsViewModel<TList extends DomainListObject> extends ViewModelBase {

    public interface Listener extends ViewModelBase.Listener {

        void onNameChanged();

        void onAvailableAmountsChanged();

        void onSelectedAmountChanged(double oldValue, double newValue);

        void onAvailableUnitsChanged();

        void onSelectedUnitChanged();

        void onIsHighlightedChanged();

        void onBrandChanged();

        void onCommentChanged();

        void onAvailableGroupsChanged();

        void onSelectedGroupChanged();

        void onImageIdChanged();

        void onLocationChanged();

        void onLastBoughtDateChanged();
    }

    protected enum NullListener implements Listener {

        Instance;

        @Override
        public void onNameChanged() {
        }

        @Override
        public void onAvailableAmountsChanged() {
        }

        @Override
        public void onSelectedAmountChanged(double oldValue, double newValue) {
        }

        @Override
        public void onAvailableUnitsChanged() {
        }

        @Override
        public void onSelectedUnitChanged() {
        }

        @Override
        public void onIsHighlightedChanged() {
        }

        @Override
        public void onBrandChanged() {
        }

        @Override
        public void onCommentChanged() {
        }

        @Override
        public void onAvailableGroupsChanged() {
        }

        @Override
        public void onSelectedGroupChanged() {
        }

        @Override
        public void onFinish() {
        }

        @Override
        public void onImageIdChanged() {

        }

        @Override
        public void onLocationChanged() {
        }

        @Override
        public void onLastBoughtDateChanged() {

        }
    }

    private final ArrayList<Double> naturalAmounts = new ArrayList<>(Arrays.asList(new Double[]{
            1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 12d, 15d, 20d, 25d, 30d,
            40d, 50d, 60d, 70d, 75d, 80d, 90d, 100d, 125d, 150d, 175d, 200d, 250d, 300d, 350d, 400d,
            450d, 500d, 600d, 700d, 750d, 800d, 900d, 1000d}));

    private final ArrayList<Double> allAmounts = new ArrayList<>(Arrays.asList(new Double[]{
            0.25d, 0.5d, 0.75d, 1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 12d, 15d, 20d, 25d, 30d,
            40d, 50d, 60d, 70d, 75d, 80d, 90d, 100d, 125d, 150d, 175d, 200d, 250d, 300d, 350d, 400d,
            450d, 500d, 600d, 700d, 750d, 800d, 900d, 1000d}));


    private boolean initialized = false;
    protected int listId;
    private int itemId;

    // backing fields for properties
    private String name;
    private List<Double> availableAmounts;
    private double selectedAmount;
    private List<Unit> availableUnits;
    private Unit selectedUnit;
    private boolean isHighlighted;
    private String brand;
    private String comment;
    private List<Group> availableGroups;
    private Group selectedGroup;
    private String imageId;
    private LastLocation location;
    private Date lastBoughtDate;

    private final Command<Void> saveItemCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            saveItemCommandExecute();
        }
    };

    private final Command<Void> deleteItemCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            deleteItemCommandExecute();
        }
    };
    private final Command<Void> removeImageCommand = new Command<Void>() {
        @Override
        public void execute(Void parameter) {
            removeImageCommandExecute();
        }
    };

    private final ListManager<TList> listManager;
    private final SimpleStorage<Unit> unitStorage;
    private final SimpleStorage<Group> groupStorage;
    private final ViewLauncher viewLauncher;
    private final AutoCompletionHelper autoCompletionHelper;
    private final ItemMerger<TList> itemMerger;
    private final SharedPreferencesWrapper sharedPreferences;
    private final ResourceProvider resourceProvider;


    public ItemDetailsViewModel(ListManager<TList> listManager, SimpleStorage<Unit> unitStorage,
                                SimpleStorage<Group> groupStorage, ViewLauncher viewLauncher,
                                AutoCompletionHelper autoCompletionHelper, ItemMerger<TList> itemMerger,
                                SharedPreferencesWrapper sharedPreferences, ResourceProvider resourceProvider) {

        if (listManager == null) {
            throw new ArgumentNullException("listManager");
        }
        if (unitStorage == null) {
            throw new ArgumentNullException("unitStorage");
        }
        if (groupStorage == null) {
            throw new ArgumentNullException("groupStorage");
        }
        if (viewLauncher == null) {
            throw new ArgumentNullException("viewLauncher");
        }
        if (autoCompletionHelper == null) {
            throw new ArgumentNullException("autoCompletionHelper");
        }
        if (itemMerger == null) {
            throw new ArgumentNullException("itemMerger");
        }
        if (sharedPreferences == null) {
            throw new ArgumentNullException("sharedPreferences");
        }
        if (resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        this.listManager = listManager;
        this.unitStorage = unitStorage;
        this.groupStorage = groupStorage;
        this.viewLauncher = viewLauncher;
        this.autoCompletionHelper = autoCompletionHelper;
        this.itemMerger = itemMerger;
        this.sharedPreferences = sharedPreferences;
        this.resourceProvider = resourceProvider;

    }


    public void initialize(int listId, int itemId) {

        if (!initialized) {

            setAvailableUnits(unitStorage.getItems());
            setAvailableGroups(groupStorage.getItems());

            //no need to call setAvailableAmounts() because it will be handled automatically by setSelectedUnit()

            this.listId = listId;
            this.itemId = itemId;
            if (itemId == -1) {

               initializeForNewItem();

            } else {
                Item item = listManager.getListItem(listId, itemId);

               initializeForExistingItem(item);
            }

            initialized = true;
        }

    }

    protected void initializeForNewItem() {
        setName("");
        setBrand("");
        setComment("");
        setIsHighlighted(false);
        setImageId(null);
        setLocation(null);
        setLastBoughtDate(null);

        setSelectedGroup(groupStorage.getDefaultValue());
        setSelectedUnit(unitStorage.getDefaultValue());
        setSelectedAmount(1);

        getDeleteItemCommand().setCanExecute(false);
        getDeleteItemCommand().setIsAvailable(false);

        getRemoveImageCommand().setIsAvailable(false);
    }


    protected void initializeForExistingItem(Item item) {
        setName(item.getName());
        setBrand(item.getBrand());
        setComment(item.getComment());
        setIsHighlighted(item.isHighlight());
        setImageId(item.getImageItem());
        setLocation(item.getLocation());
        setLastBoughtDate(item.getLastBought());

        Group group = item.getGroup() != null
                ? item.getGroup()
                : groupStorage.getDefaultValue();
        setSelectedGroup(group);

        Unit unit = item.getUnit() != null
                ? item.getUnit()
                : unitStorage.getDefaultValue();
        setSelectedUnit(unit);


        double amount = item.getAmount();

        if(amount % 1 == 0) {
            if(!naturalAmounts.contains(amount)) {
                naturalAmounts.add(amount);
                Collections.sort(naturalAmounts);
            }
        }

        if(!allAmounts.contains(amount)) {
            allAmounts.add(amount);
            Collections.sort(allAmounts);
        }

        setSelectedAmount(amount);

        getDeleteItemCommand().setCanExecute(true);
        getDeleteItemCommand().setIsAvailable(true);

        getRemoveImageCommand().setIsAvailable(!StringHelper.isNullOrWhiteSpace(item.getImageItem()));
    }


    public boolean isNewItem() {
        return this.itemId == -1;
    }

    public String getName() {
        return this.name != null ? this.name : "";
    }

    public void setName(String value) {

        if (!ObjectHelper.compare(this.name, value)) {
            this.name = value;
            getListener().onNameChanged();

            // require a non whitespace name to be entered in order to be able to save
            getSaveItemCommand().setCanExecute(!StringHelper.isNullOrWhiteSpace(value));

            if(getSelectedGroup() == null || getSelectedGroup() == groupStorage.getDefaultValue()) {

                Group suggestedGroup = autoCompletionHelper.getGroup(value);
                if(suggestedGroup != null) {
                    setSelectedGroup(suggestedGroup);
                }
            }

        }
    }

    public List<Double> getAvailableAmounts() {
        return this.availableAmounts != null ? this.availableAmounts : new ArrayList<Double>();
    }

    private void setAvailableAmounts(List<Double> value) {
        if (value != this.availableAmounts) {
            this.availableAmounts = value;
            getListener().onAvailableAmountsChanged();
        }
    }

    public double getSelectedAmount() {
        return this.selectedAmount;
    }

    public void setSelectedAmount(double value) {
        if (this.selectedAmount != value) {

            double oldValue = this.selectedAmount;

            this.selectedAmount = value;
            getListener().onSelectedAmountChanged(oldValue, value);
        }
    }

    public List<Unit> getAvailableUnits() {
        return this.availableUnits != null ? this.availableUnits : new ArrayList<Unit>();
    }

    private void setAvailableUnits(List<Unit> value) {
        if (this.availableUnits != value) {
            this.availableUnits = value;
            getListener().onAvailableUnitsChanged();
        }
    }

    public Unit getSelectedUnit() {
        return this.selectedUnit;
    }

    public void setSelectedUnit(Unit value) {
        if (!ObjectHelper.compare(this.selectedUnit, value)) {
            this.selectedUnit = value;
            getListener().onSelectedUnitChanged();

            setAvailableAmounts(getAvailableAmounts(this.selectedUnit));
        }
    }

    public boolean getIsHighlighted() {
        return this.isHighlighted;
    }

    public void setIsHighlighted(boolean value) {
        if (this.isHighlighted != value) {
            this.isHighlighted = value;
            getListener().onIsHighlightedChanged();
        }
    }

    public String getBrand() {
        return this.brand != null ? this.brand : "";
    }

    public void setBrand(String value) {
        if (!ObjectHelper.compare(this.brand, value)) {
            this.brand = value;
            getListener().onBrandChanged();
        }
    }

    public String getComment() {
        return this.comment != null ? this.comment : "";
    }

    public void setComment(String value) {
        if (!ObjectHelper.compare(this.comment, value)) {
            this.comment = value;
            getListener().onCommentChanged();
        }
    }

    public List<Group> getAvailableGroups() {
        return this.availableGroups != null ? this.availableGroups : new ArrayList<Group>();
    }

    private void setAvailableGroups(List<Group> value) {
        if (this.availableGroups != value) {
            this.availableGroups = value;
            getListener().onAvailableGroupsChanged();
        }
    }

    public Group getSelectedGroup() {
        return this.selectedGroup;
    }

    public void setSelectedGroup(Group value) {
        if (!ObjectHelper.compare(this.selectedGroup, value)) {
            this.selectedGroup = value;
            getListener().onSelectedGroupChanged();
        }
    }

    public String getImageId() {
        return this.imageId;
    }

    public void setImageId(String value) {
        if(!ObjectHelper.compare(this.imageId, value)) {
            this.imageId = value;
            getListener().onImageIdChanged();

            getRemoveImageCommand().setIsAvailable(this.imageId != null);
        }
    }

    public LastLocation getLocation() {
        return this.location;
    }

    private void setLocation(LastLocation value) {
        if(!ObjectHelper.compare(this.location, value)) {
            this.location = value;
            getListener().onLocationChanged();
        }
    }

    public Date getLastBoughtDate() {
        return this.lastBoughtDate;
    }

    private void setLastBoughtDate(Date value) {
        if(!ObjectHelper.compare(this.lastBoughtDate, value)) {
            this.lastBoughtDate = value;
            getListener().onLastBoughtDateChanged();
        }
    }

    public Command<Void> getSaveItemCommand() {
        return this.saveItemCommand;
    }

    public Command<Void> getDeleteItemCommand() {
        return this.deleteItemCommand;
    }

    public Command<Void> getRemoveImageCommand() {
        return this.removeImageCommand;
    }

    private List<Double> getAvailableAmounts(Unit unit) {
        //TODO: return naturalAmounts for applicable units
        return allAmounts;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent event) {

        if (event.getChangeType() == ItemChangeType.Deleted &&
                event.getListType() == this.getListType() &&
                event.getListId() == this.listId &&
                event.getItemId() == this.itemId) {
            this.finish();
        }
    }

    //TODO: subscribe to events from eventbus
    //TODO: add onEvent for list type in subclasses


    protected abstract ListType getListType();

    //Reimplement getListener() to return ItemDetailsViewModel.Listener
    @Override
    protected abstract Listener getListener();

    /**
     * Extension point for derived view models to set additional properties of item before
     * it is saved
     */
    protected void setAdditionalItemProperties(Item item) {

    }


    private void saveItemCommandExecute() {

        autoCompletionHelper.offerNameAndGroup(getName(), getSelectedGroup());
        autoCompletionHelper.offerBrand(getBrand());

        Item item = this.itemId == -1 ? new Item() : listManager.getListItem(listId, itemId);

        item.setName(getName());
        item.setAmount(getSelectedAmount());
        item.setUnit(getSelectedUnit());
        item.setHighlight(getIsHighlighted());
        item.setBrand(getBrand());
        item.setComment(getComment());
        item.setGroup(getSelectedGroup());
        item.setImageItem(getImageId());

        setAdditionalItemProperties(item);

        if (this.itemId == -1) {
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

        finish();
    }

    private void deleteItemCommandExecute() {

        if (!sharedPreferences.loadBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG, true)) {

            listManager.deleteItem(listId, itemId);
            finish();

        } else {

            viewLauncher.showMessageDialogWithCheckbox(
                    //title
                    resourceProvider.getString(R.string.title_delete_item),
                    //message
                    resourceProvider.getString(R.string.message_delete_item),
                    //positiveMessage
                    resourceProvider.getString(R.string.delete),
                    //positiveCommand
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            listManager.deleteItem(listId, itemId);
                            finish();
                        }
                    },
                    //neutralMessage
                    null,
                    //neutralCommand
                    NullCommand.VoidInstance,
                    //negativeMessage
                    resourceProvider.getString(R.string.cancel),
                    //negativeCommand
                    NullCommand.VoidInstance,
                    //checkBoxMessage
                    resourceProvider.getString(R.string.dont_show_this_message_again),
                    //checkBoxDefaultValue
                    false,
                    //checkedCommand
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            sharedPreferences.saveBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG, false);
                        }
                    },
                    //uncheckedCommand
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            sharedPreferences.saveBoolean(SharedPreferencesHelper.ITEM_DELETION_SHOW_AGAIN_MSG, true);
                        }
                    }
            );
        }

    }

    private void removeImageCommandExecute() {
        setImageId(null);
    }
}
