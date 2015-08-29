package de.fau.cs.mad.kwikshop.android.viewmodel;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;

public class ShoppingListItemDetailsViewModel extends ItemDetailsViewModel{

    private final RegularlyRepeatHelper repeatHelper;

    @Inject
    public ShoppingListItemDetailsViewModel(ViewLauncher viewLauncher, ListManager<ShoppingList> listManager, SimpleStorage<Unit> unitStorage,
                                            SimpleStorage<Group> groupStorage, DisplayHelper displayHelper, AutoCompletionHelper autoCompletionHelper,
                                            RegularlyRepeatHelper repeatHelper){

        super(viewLauncher, unitStorage, groupStorage, displayHelper, autoCompletionHelper);

        if(repeatHelper == null) throw new ArgumentNullException("repeatHelper");

        if(listManager == null) throw new ArgumentNullException("listManager");

        this.repeatHelper = repeatHelper;
    }

    public RegularlyRepeatHelper getRepeatHelper(){ return repeatHelper; }

    public void repeatFromNextPurchaseOn(Item item){
        item.setRemindAtDate(null);
    }

    public void repeatOnNewList(Item item){
        repeatFromNextPurchaseOn(item);

        item.setPeriodType(null);
        item.setSelectedRepeatTime(0);
        item.setRemindFromNowOn(false);
    }

    public void offerRepeatData(Item item){
        repeatHelper.offerRepeatData(item);
    }

    public void deleteRepetition(Item item){
        item.setRemindAtDate(null);
        repeatHelper.delete(item);
    }


}
