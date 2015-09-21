package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;

import java.util.ArrayList;

import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RepeatStartType;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.common.RepeatType;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.TimePeriodsEnum;


public class ShoppingListItemDetailsFragment extends ItemDetailsFragment<ShoppingList> implements ShoppingListItemDetailsViewModel.Listener {

    private ShoppingListItemDetailsViewModel viewModel;

    private boolean updatingRepeatType = false;
    private boolean updatingStartType = false;
    private boolean updatingPeriodType = false;
    private boolean updatingPeriod = false;

    private boolean enableEvents = false;



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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // show controls for item recurrence
        repeat_Container.setVisibility(View.VISIBLE);

        onSelectedRepeatTypeChanged();

        ArrayList<String> repeat_spinner_entries = new ArrayList<>(3);
        repeat_spinner_entries.add(0, getString(R.string.days));
        repeat_spinner_entries.add(1, getString(R.string.weeks));
        repeat_spinner_entries.add(2, getString(R.string.months));
        ArrayAdapter<String> repeat_spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, repeat_spinner_entries);
        repeat_spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeat_spinner.setAdapter(repeat_spinnerAdapter);

        onSelectedPeriodTypeChanged();

        repeat_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                if(!enableEvents) {
                    return;
                }

                updatingPeriodType = true;

                switch (position) {

                    case 0:
                        viewModel.setSelectedRepeatPeriodType(TimePeriodsEnum.DAYS);
                        break;
                    case 1:
                        viewModel.setSelectedRepeatPeriodType(TimePeriodsEnum.WEEKS);
                        break;
                    case 2:
                        viewModel.setSelectedRepeatPeriodType(TimePeriodsEnum.MONTHS);
                        break;
                    default:
                        viewModel.setSelectedRepeatPeriodType(TimePeriodsEnum.DAYS);
                        break;
                }

                updatingPeriodType = false;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                if(!enableEvents) {
                    return;
                }

                updatingPeriodType = true;
                viewModel.setSelectedRepeatPeriodType(TimePeriodsEnum.DAYS);
                updatingPeriodType = false;

            }
        });


        repeat_numberPicker.setMinValue(1);
        repeat_numberPicker.setMaxValue(10);
        repeat_numberPicker.setWrapSelectorWheel(false);


        onSelectedRepeatPeriodChanged();

        repeat_numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                if(!enableEvents) {
                    return;
                }

                updatingPeriod = true;
                viewModel.setSelectedRepeatPeriod(newVal);
                updatingPeriod = false;
            }
        });


        onSelectedRepeatStartTypeChanged();

        enableEvents = true;

        return view;
    }

    @Override
    protected ItemDetailsViewModel<ShoppingList> getViewModel(ObjectGraph objectGraph) {
        if (this.viewModel == null) {
            this.viewModel = objectGraph.get(ShoppingListItemDetailsViewModel.class);
        }

        return this.viewModel;
    }

    @Override
    protected void subscribeToViewModelEvents() {
        viewModel.setListener(this);
    }

    @Override
    public void onSelectedRepeatTypeChanged() {

        switch (viewModel.getSelectedRepeatType()) {

            case None:

                if (!updatingRepeatType) {
                    repeat_checkbox.setChecked(false);
                }

                repeat_radioGroup_repeatType.setVisibility(View.GONE);
                repeat_row_scheduleSelection.setVisibility(View.GONE);
                repeat_radioGroup_scheduleStart.setVisibility(View.GONE);

                break;

            case ListCreation:
                if (!updatingRepeatType) {
                    repeat_checkbox.setChecked(true);
                    repeat_radioButton_repeatType_listCreation.setChecked(true);
                }

                repeat_radioGroup_repeatType.setVisibility(View.VISIBLE);
                repeat_row_scheduleSelection.setVisibility(View.GONE);
                repeat_radioGroup_scheduleStart.setVisibility(View.GONE);
                break;

            case Schedule:

                if (!updatingRepeatType) {
                    repeat_checkbox.setChecked(true);
                    repeat_radioButton_repeatType_schedule.setChecked(true);
                }

                repeat_radioGroup_repeatType.setVisibility(View.VISIBLE);
                repeat_row_scheduleSelection.setVisibility(View.VISIBLE);
                repeat_radioGroup_scheduleStart.setVisibility(View.VISIBLE);

                break;
        }

    }

    @Override
    public void onSelectedRepeatPeriodChanged() {

        if (!updatingPeriod) {
            repeat_numberPicker.setValue(viewModel.getSelectedRepeatPeriod());
        }

    }

    @Override
    public void onSelectedPeriodTypeChanged() {

        if (viewModel.getSelectedPeriodType() == null) {
            return;
        }

        if (!updatingPeriodType) {

            switch (viewModel.getSelectedPeriodType()) {

                case DAYS:
                    repeat_spinner.setSelection(0);
                    break;
                case WEEKS:
                    repeat_spinner.setSelection(1);
                    break;
                case MONTHS:
                    repeat_spinner.setSelection(2);
                    break;
            }
        }

    }

    @Override
    public void onSelectedRepeatStartTypeChanged() {

        if (!updatingStartType) {

            switch (viewModel.getSelectedRepeatStartType()) {
                case Now:
                    repeat_fromNow_radioButton.setChecked(true);
                    break;
                case NextPurchase:
                    repeat_fromNextPurchase_radioButton.setChecked(true);
            }
        }
    }


    @OnCheckedChanged(R.id.repeat_checkBox)
    @SuppressWarnings("unused")
    void checkBox_Repeat_OnCheckedChanged() {


        if(!enableEvents) {
            return;
        }


        updatingRepeatType = true;

        if (repeat_checkbox.isChecked()) {

            if (repeat_radioButton_repeatType_schedule.isChecked()) {
                viewModel.setSelectedRepeatType(RepeatType.Schedule);
            } else {
                viewModel.setSelectedRepeatType(RepeatType.ListCreation);
            }
        } else {
            viewModel.setSelectedRepeatType(RepeatType.None);
        }


        updatingRepeatType = false;
    }

    @OnClick({R.id.repeat_radioButton_repeatType_listCreation, R.id.repeat_radioButton_repeatType_schedule})
    @SuppressWarnings("unused")
    void radioButton_RepeatType_OnClick() {


        if(!enableEvents) {
            return;
        }

        updatingRepeatType = true;

        if (repeat_radioButton_repeatType_schedule.isChecked()) {
            viewModel.setSelectedRepeatType(RepeatType.Schedule);
        } else {
            viewModel.setSelectedRepeatType(RepeatType.ListCreation);
        }

        updatingRepeatType = false;
    }

    @OnClick({R.id.repeat_fromNow_radioButton, R.id.repeat_fromNextPurchase_radioButton})
    @SuppressWarnings("unused")
    void radioButton_StartType_OnClick() {


        if(!enableEvents) {
            return;
        }

        updatingStartType = true;

        if (repeat_fromNow_radioButton.isChecked()) {
            viewModel.setSelectedRepeatStartType(RepeatStartType.Now);
        } else {
            viewModel.setSelectedRepeatStartType(RepeatStartType.NextPurchase);
        }

        updatingStartType = false;
    }

}
