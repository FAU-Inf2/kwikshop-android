package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.inject.Inject;
import javax.ws.rs.NotSupportedException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.common.RepeatType;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.TimePeriodsEnum;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.greenrobot.event.EventBus;


public class ShoppingListItemDetailsFragment extends ItemDetailsFragment<ShoppingList> {

    /* UI elements */

//    @InjectView(R.id.repeat_container)
//    View repeat_Container;
//
//    @InjectView(R.id.repeat_checkBox)
//    CheckBox repeat_checkbox;
//
//    @InjectView(R.id.repeat_spinner)
//    Spinner repeat_spinner;
//
//    @InjectView(R.id.repeat_numberPicker)
//    NumberPicker repeat_numberPicker;
//
//    @InjectView(R.id.repeat_fromNow_radioButton)
//    RadioButton repeat_fromNow_radioButton;
//
//    @InjectView(R.id.repeat_fromNextPurchase_radioButton)
//    RadioButton repeat_fromNextPurchase_radioButton;
//
//    @InjectView(R.id.repeat_radioGroup_repeatType)
//    View repeat_radioGroup_repeatType;
//
//    @InjectView(R.id.repeat_row_scheduleSelection)
//    View repeat_row_scheduleSelection;
//
//    @InjectView(R.id.repeat_radioGroup_scheduleStart)
//    View repeat_radioGroup_scheduleStart;
//
//    @InjectView(R.id.repeat_radioButton_repeatType_schedule)
//    RadioButton repeat_radioButton_repeatType_schedule;
//
//    @InjectView(R.id.repeat_radioButton_repeatType_listCreation)
//    RadioButton repeat_radioButton_repeatType_listCreation;

//    private String additionalToastText;


    /**
     * Creates a new instance of ItemDetailsFragment for a new shopping list item in the specified list
     */
    public static ShoppingListItemDetailsFragment newInstance(int listID) {
        return newInstance(listID, -1);
    }

    /**
     * Creates a new instance of ItemDetailsFragment for the specified shopping list item
     */
    public static ShoppingListItemDetailsFragment newInstance(int listID, int itemID) {
        ShoppingListItemDetailsFragment fragment = new ShoppingListItemDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LISTID, listID);
        args.putInt(ARG_ITEMID, itemID);
        fragment.setArguments(args);

        return fragment;
    }


    public ShoppingListItemDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    protected ItemDetailsViewModel<ShoppingList> createViewModel(ObjectGraph objectGraph) {
        return  objectGraph.get(ShoppingListItemDetailsViewModel.class);
    }
}
