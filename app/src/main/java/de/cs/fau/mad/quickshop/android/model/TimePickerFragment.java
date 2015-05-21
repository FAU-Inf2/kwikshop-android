package de.cs.fau.mad.quickshop.android.model;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.DateFormat;

import de.cs.fau.mad.quickshop.android.CalendarEventDate;
import de.cs.fau.mad.quickshop.android.ShoppingList;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {


    private CalendarEventDate eventDate;
    private ShoppingList shoppingList;


    public TimePickerFragment(CalendarEventDate eventDate, ShoppingList shoppingList){
        this.eventDate = eventDate;
        this.shoppingList = shoppingList;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        return new TimePickerDialog(getActivity(), this, eventDate.getHour(), eventDate.getMinute(), false);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        eventDate.setHour(hourOfDay);
        eventDate.setMinute(minute);

    }

}
