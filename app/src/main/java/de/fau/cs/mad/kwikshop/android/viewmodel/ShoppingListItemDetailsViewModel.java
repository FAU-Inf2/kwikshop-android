package de.fau.cs.mad.kwikshop.android.viewmodel;


import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesWrapper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;

public class ShoppingListItemDetailsViewModel extends ItemDetailsViewModel<ShoppingList>{

    //TODO: recurrence
    public interface Listener extends ItemDetailsViewModel.Listener {

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
    }


    private Listener listener;



    public ShoppingListItemDetailsViewModel(ListManager<ShoppingList> listManager, SimpleStorage<Unit> unitStorage, SimpleStorage<Group> groupStorage, ViewLauncher viewLauncher, AutoCompletionHelper autoCompletionHelper, ItemMerger<ShoppingList> itemMerger, SharedPreferencesWrapper sharedPreferences, ResourceProvider resourceProvider) {
        super(listManager, unitStorage, groupStorage, viewLauncher, autoCompletionHelper, itemMerger, sharedPreferences, resourceProvider);
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


//    public RegularlyRepeatHelper getRepeatHelper(){ return repeatHelper; }
//
//    public void repeatFromNextPurchaseOn(Item item){
//        item.setRemindAtDate(null);
//    }
//
//    public void repeatOnNewList(Item item){
//        repeatFromNextPurchaseOn(item);
//
//        item.setPeriodType(null);
//        item.setSelectedRepeatTime(0);
//        item.setRemindFromNowOn(false);
//    }
//
//    public void offerRepeatData(Item item){
//        repeatHelper.offerRepeatData(item);
//    }
//
//    public void deleteRepetition(Item item){
//        item.setRemindAtDate(null);
//        repeatHelper.delete(item);
//    }


}
