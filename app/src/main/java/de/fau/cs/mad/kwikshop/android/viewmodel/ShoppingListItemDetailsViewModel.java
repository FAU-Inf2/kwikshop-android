package de.fau.cs.mad.kwikshop.android.viewmodel;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.*;
import de.fau.cs.mad.kwikshop.common.ItemViewModel;

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

    public void repeatFromNextPurchaseOn(ItemViewModel item){
        item.setLastBought(null);
        item.setRemindAtDate(null);
    }

    public void repeatOnNewList(ItemViewModel item){
        repeatFromNextPurchaseOn(item);

        item.setPeriodType(null);
        item.setSelectedRepeatTime(0);
        item.setRemindFromNowOn(false);
    }

    public void offerRepeatData(ItemViewModel item){
        repeatHelper.offerRepeatData(item);
    }

    public void deleteRepetition(de.fau.cs.mad.kwikshop.common.ItemViewModel item){
        item.setRemindAtDate(null);
        repeatHelper.delete(item);
    }


}
