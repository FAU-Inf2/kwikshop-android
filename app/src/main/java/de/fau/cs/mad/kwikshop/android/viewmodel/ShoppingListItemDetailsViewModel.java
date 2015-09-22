package de.fau.cs.mad.kwikshop.android.viewmodel;


import java.sql.Time;
import java.util.Calendar;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesWrapper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.RepeatType;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.TimePeriodsEnum;
import de.fau.cs.mad.kwikshop.common.Unit;

public class ShoppingListItemDetailsViewModel extends ItemDetailsViewModel<ShoppingList>{

    public interface Listener extends ItemDetailsViewModel.Listener {

        void onSelectedRepeatTypeChanged();

        void onSelectedRepeatPeriodChanged();

        void onSelectedPeriodTypeChanged();

        void onSelectedRepeatStartTypeChanged();
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
        public void onImageIdChanged() {

        }

        @Override
        public void onLocationChanged() {

        }

        @Override
        public void onLastBoughtDateChanged() {

        }

        @Override
        public void onFinish() {

        }

        @Override
        public void onSelectedRepeatTypeChanged() {

        }

        @Override
        public void onSelectedRepeatPeriodChanged() {

        }

        @Override
        public void onSelectedPeriodTypeChanged() {

        }

        @Override
        public void onSelectedRepeatStartTypeChanged() {

        }
    }


    private final RegularlyRepeatHelper repeatHelper;


    private Listener listener;

    private RepeatType selectedRepeatType;
    private int selectedRepeatPeriod;
    private TimePeriodsEnum selectedPeriodType = TimePeriodsEnum.DAYS;
    private RepeatStartType selectedRepeatStartType;


    @Inject
    public ShoppingListItemDetailsViewModel(ListManager<ShoppingList> listManager, SimpleStorage<Unit> unitStorage,
                                            SimpleStorage<Group> groupStorage, ViewLauncher viewLauncher,
                                            AutoCompletionHelper autoCompletionHelper, ItemMerger<ShoppingList> itemMerger,
                                            SharedPreferencesWrapper sharedPreferences, ResourceProvider resourceProvider,
                                            RegularlyRepeatHelper repeatHelper) {

        super(listManager, unitStorage, groupStorage, viewLauncher, autoCompletionHelper, itemMerger, sharedPreferences, resourceProvider);

        if(repeatHelper == null) {
            throw new ArgumentNullException("repeatHelper");
        }

        this.repeatHelper = repeatHelper;
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected ListType getListType() {
        return ListType.ShoppingList;
    }

    @Override
    protected Listener getListener() {
        return listener != null ? listener : NullListener.Instance;
    }

    @Override
    protected void initializeForNewItem() {
      super.initializeForNewItem();

        setSelectedRepeatType(RepeatType.None);

        setSelectedRepeatPeriodType(TimePeriodsEnum.DAYS);
        setSelectedRepeatPeriod(1);
        setSelectedRepeatStartType(RepeatStartType.Now);
    }

    @Override
    protected void initializeForExistingItem(Item item) {

        super.initializeForExistingItem(item);

        setSelectedRepeatType(item.getRepeatType());

        switch (item.getRepeatType()) {

            case None:
            case ListCreation:
                setSelectedRepeatPeriodType(TimePeriodsEnum.DAYS);
                setSelectedRepeatPeriod(1);
                setSelectedRepeatStartType(RepeatStartType.Now);
                break;

            case Schedule:
                setSelectedRepeatPeriodType(item.getPeriodType());
                setSelectedRepeatPeriod(item.getSelectedRepeatTime());
                if(item.isRemindFromNextPurchaseOn()) {
                    setSelectedRepeatStartType(RepeatStartType.NextPurchase);
                } else {
                    setSelectedRepeatStartType(RepeatStartType.Now);
                }
        }

    }

    @Override
    protected void setAdditionalItemProperties(Item item) {

        super.setAdditionalItemProperties(item);


        switch (getSelectedRepeatType()) {

            case None:

                if(item.getRepeatType() != RepeatType.None) {
                    repeatHelper.delete(item);
                }
                item.setRepeatType(getSelectedRepeatType());
                item.setRemindAtDate(null);
                item.setRemindFromNextPurchaseOn(false);
                item.setSelectedRepeatTime(0);
                break;

            case ListCreation:
                item.setRepeatType(getSelectedRepeatType());
                item.setRemindAtDate(null);
                item.setRemindFromNextPurchaseOn(false);
                item.setSelectedRepeatTime(0);

                repeatHelper.offerRepeatData(item);
                break;

            case Schedule:
                item.setRepeatType(getSelectedRepeatType());

                item.setRemindFromNextPurchaseOn(getSelectedRepeatStartType() == RepeatStartType.NextPurchase);
                item.setPeriodType(getSelectedPeriodType());
                item.setSelectedRepeatTime(getSelectedRepeatPeriod());

                Calendar remindDate = Calendar.getInstance();
                switch (getSelectedPeriodType()) {
                    case DAYS:
                        remindDate.add(Calendar.DAY_OF_MONTH, getSelectedRepeatPeriod());
                        break;
                    case WEEKS:
                        remindDate.add(Calendar.DAY_OF_MONTH, getSelectedRepeatPeriod() * 7);
                        break;
                    case MONTHS:
                        remindDate.add(Calendar.MONTH, getSelectedRepeatPeriod());
                        break;
                }
                item.setRemindAtDate(remindDate.getTime());


                repeatHelper.offerRepeatData(item);
                break;

        }

    }


    public RepeatType getSelectedRepeatType() {
        return this.selectedRepeatType;
    }

    public void setSelectedRepeatType(RepeatType value) {
        if(this.selectedRepeatType != value) {
            this.selectedRepeatType = value;
            getListener().onSelectedRepeatTypeChanged();
        }
    }

    public TimePeriodsEnum getSelectedPeriodType() {
        return this.selectedPeriodType;
    }

    public void setSelectedRepeatPeriodType(TimePeriodsEnum value) {
        if(this.selectedPeriodType != null) {
            this.selectedPeriodType = value;
            getListener().onSelectedPeriodTypeChanged();
        }
    }

    public int getSelectedRepeatPeriod() {
        return this.selectedRepeatPeriod;
    }

    public void setSelectedRepeatPeriod(int value) {
        if(this.selectedRepeatPeriod != value) {
            this.selectedRepeatPeriod = value;
            getListener().onSelectedRepeatPeriodChanged();
        }
    }

    public RepeatStartType getSelectedRepeatStartType() {
        return this.selectedRepeatStartType;
    }

    public void setSelectedRepeatStartType(RepeatStartType value) {
        if(this.selectedRepeatStartType != value) {
            this.selectedRepeatStartType = value;
            getListener().onSelectedRepeatStartTypeChanged();
        }
    }



}
