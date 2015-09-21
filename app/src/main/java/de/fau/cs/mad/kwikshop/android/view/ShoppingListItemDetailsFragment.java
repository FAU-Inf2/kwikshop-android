package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.common.ShoppingList;


public class ShoppingListItemDetailsFragment extends ItemDetailsFragment<ShoppingList> implements ShoppingListItemDetailsViewModel.Listener {

    /* UI elements */
    private ShoppingListItemDetailsViewModel viewModel;


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
    protected ItemDetailsViewModel<ShoppingList> getViewModel(ObjectGraph objectGraph) {
        if(this.viewModel == null ) {
            this.viewModel = objectGraph.get(ShoppingListItemDetailsViewModel.class);
        }

        return this.viewModel;
    }

    @Override
    protected void subscribeToViewModelEvents() {
        viewModel.setListener(this);
    }
}
