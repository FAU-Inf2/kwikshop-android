package de.cs.fau.mad.quickshop.android;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.widget.TimePicker;

import java.util.Calendar;

import de.cs.fau.mad.quickshop.android.common.CalendarEventDate;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    public TimePickerFragment(){

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        this.year = getArguments() != null ? getArguments().getInt("year") : c.get(Calendar.YEAR);
        this.month = getArguments() != null ? getArguments().getInt("month") : c.get(Calendar.MONTH);
        this.day = getArguments() != null ? getArguments().getInt("day") : c.get(Calendar.DAY_OF_MONTH);
        this.hour = getArguments() != null ? getArguments().getInt("hour") : c.get(Calendar.HOUR_OF_DAY);
        this.minute = getArguments() != null ? getArguments().getInt("minute") : c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute, false);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        this.hour = hourOfDay;
        this.minute = minute;

        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        args.putInt("hour", hourOfDay);
        args.putInt("minute", minute);
    }

}

