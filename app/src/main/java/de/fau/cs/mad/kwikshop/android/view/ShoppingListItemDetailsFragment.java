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

import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.TimePeriodsEnum;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;


public class ShoppingListItemDetailsFragment extends ItemDetailsFragment<ShoppingList> {


    // fields cannot be moved to base class because Dagger cant handle generics
    @Inject
    ListManager<ShoppingList> listManager;


    /* UI elements */

    @InjectView(R.id.repeat_container)
    View repeat_Container;

    @InjectView(R.id.repeat_checkBox)
    CheckBox repeat_checkbox;

    @InjectView(R.id.repeat_spinner)
    Spinner repeat_spinner;

    @InjectView(R.id.repeat_numberPicker)
    NumberPicker repeat_numberPicker;

    @InjectView(R.id.repeat_fromNow_radioButton)
    RadioButton repeat_fromNow_radioButton;

    @InjectView(R.id.repeat_fromNextPurchase_radioButton)
    RadioButton repeat_fromNextPurchase_radioButton;




    @Inject
    RegularlyRepeatHelper repeatHelper;

    private String additionalToastText;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        repeat_Container.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    protected void saveItem() {

        super.saveItem();

        Toast.makeText(getActivity(), getResources().getString(R.string.itemdetails_saved) + additionalToastText, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void setupUI() {

        super.setupUI();


        if (!isNewItem) {
            boolean repeatItem = item.isRegularlyRepeatItem();
            repeat_checkbox.setChecked(repeatItem);
            if (repeatItem) {
                TimePeriodsEnum timePeriod = item.getPeriodType();
                int spinnerPos = 0;
                switch (timePeriod) {
                    case DAYS:
                        spinnerPos = 0;
                        break;
                    case WEEKS:
                        spinnerPos = 1;
                        break;
                    case MONTHS:
                        spinnerPos = 2;
                        break;
                }
                repeat_spinner.setSelection(spinnerPos);
                int numberPickerVal = item.getSelectedRepeatTime();
                repeat_numberPicker.setValue(numberPickerVal);
                repeat_fromNextPurchase_radioButton.setChecked(item.isRemindFromNextPurchaseOn());
            }
        }


        repeat_numberPicker.setMinValue(1);
        repeat_numberPicker.setMaxValue(10);
        repeat_numberPicker.setWrapSelectorWheel(false);

        ArrayList<String> repeat_spinner_entries = new ArrayList<>(3);
        repeat_spinner_entries.add(0, getString(R.string.days));
        repeat_spinner_entries.add(1, getString(R.string.weeks));
        repeat_spinner_entries.add(2, getString(R.string.months));
        ArrayAdapter<String> repeat_spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, repeat_spinner_entries);
        repeat_spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeat_spinner.setAdapter(repeat_spinnerAdapter);

    }

    @Override
    protected ListType getListType() {
        return ListType.ShoppingList;
    }

    @Override
    protected ListManager<ShoppingList> getListManager() {
        return this.listManager;
    }

    protected void setAdditionalItemProperties() {

        additionalToastText = "";

        if (repeat_checkbox.isChecked()) {
            item.setRegularlyRepeatItem(true);
            int repeatSpinnerPos = repeat_spinner.getSelectedItemPosition();
            switch (repeatSpinnerPos) {
                case 0:
                    item.setPeriodType(TimePeriodsEnum.DAYS);
                    break;
                case 1:
                    item.setPeriodType(TimePeriodsEnum.WEEKS);
                    break;
                case 2:
                    item.setPeriodType(TimePeriodsEnum.MONTHS);
                    break;
                default:
                    break;
            }
            item.setSelectedRepeatTime(repeat_numberPicker.getValue());
            item.setRemindFromNowOn(repeat_fromNow_radioButton.isChecked());

            if (repeat_fromNow_radioButton.isChecked()) {
                Calendar remindDate = Calendar.getInstance();
                switch (item.getPeriodType()) {
                    case DAYS:
                        remindDate.add(Calendar.DAY_OF_MONTH, item.getSelectedRepeatTime());
                        break;
                    case WEEKS:
                        remindDate.add(Calendar.DAY_OF_MONTH, item.getSelectedRepeatTime() * 7);
                        break;
                    case MONTHS:
                        remindDate.add(Calendar.MONTH, item.getSelectedRepeatTime());
                        break;
                }
                item.setRemindAtDate(remindDate.getTime());

                DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.DEFAULT, getResources().getConfiguration().locale);
                additionalToastText += ". " + getString(R.string.reminder_set_msg) + " " + dateFormat.format(remindDate.getTime());

            } else { //repeat from next purchase on
                item.setLastBought(null);
                item.setRemindAtDate(null);
                additionalToastText += ". " + getString(R.string.reminder_nextTimeBought_msg);
            }

            repeatHelper.offerRepeatData(item);

        } else { // repeat_checkbox is not checked
            boolean wasRegularRepeat = item.isRegularlyRepeatItem();
            item.setRegularlyRepeatItem(false);
            if (wasRegularRepeat) { //repeat_checkbox was checked before
                item.setRemindAtDate(null);
                repeatHelper.delete(item);
                additionalToastText += ". " + getString(R.string.reminder_deleted_msg);
            }
        }

    }

}
