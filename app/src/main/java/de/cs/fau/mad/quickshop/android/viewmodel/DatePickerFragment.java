package de.cs.fau.mad.quickshop.android.viewmodel;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import de.cs.fau.mad.quickshop.android.common.CalendarEventDate;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

    private CalendarEventDate eventDate;
    private ShoppingList shoppingList;


    public DatePickerFragment(){
    }

    public DatePickerFragment(CalendarEventDate eventDate, ShoppingList shoppingList){
        this.eventDate = eventDate;
        this.shoppingList = shoppingList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        return new DatePickerDialog(getActivity(), this, eventDate.getYear(), eventDate.getMonth(), eventDate.getDay());
    }

    public void onDateSet(DatePicker view, int year, int month, int day){
        eventDate.setYear(year);
        eventDate.setMonth(month);
        eventDate.setDay(day);
        DialogFragment newFragment = new TimePickerFragment(eventDate, shoppingList);
        newFragment.show(getActivity().getFragmentManager(), "timePicker");


    }

}

