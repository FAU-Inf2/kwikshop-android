
package de.cs.fau.mad.kwikshop.android.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    public DatePickerFragment(){

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        this.year = getArguments() != null ? getArguments().getInt("year") : c.get(Calendar.YEAR);
        this.month = getArguments() != null ? getArguments().getInt("month") : c.get(Calendar.MONTH);
        this.day = getArguments() != null ? getArguments().getInt("day") : c.get(Calendar.DAY_OF_MONTH);
        this.hour = getArguments() != null ? getArguments().getInt("hour") : c.get(Calendar.HOUR_OF_DAY);
        this.minute = getArguments() != null ? getArguments().getInt("minute") : c.get(Calendar.MINUTE);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;

        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        args.putInt("hour", hour);
        args.putInt("minute", minute);

        DialogFragment newFragment = new TimePickerFragment();
        newFragment.setArguments(args);
        newFragment.show(getActivity().getFragmentManager(), "timePicker");



    }

}
