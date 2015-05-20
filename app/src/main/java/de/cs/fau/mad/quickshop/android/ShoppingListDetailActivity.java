package de.cs.fau.mad.quickshop.android;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.TimeZone;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.model.mock.ListStorageMock;

public class ShoppingListDetailActivity extends ActionBarActivity {


    //region Constants

    public static final String EXTRA_SHOPPINGLISTID = "extra_ShoppingListId";

    //endregion


    //region Fields

    private boolean m_IsNewList = false;
    private ListStorageFragment m_ListStorageFragment;
    private ShoppingList m_ShoppingList;

    private TextView m_TextView_ShoppingListName;

    private CalendarEventDate m_EventDate = new CalendarEventDate();

    static final int TIME_DIALOG_ID = 1;
    static final int DATE_DIALOG_ID = 0;


    //endregion


    //region Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_detail);


        FragmentManager fm = getSupportFragmentManager();
        m_ListStorageFragment = (ListStorageFragment) fm.findFragmentByTag(ListStorageFragment.TAG_LISTSTORAGE);
        if (m_ListStorageFragment == null) {
            m_ListStorageFragment = new ListStorageFragment();
            m_ListStorageFragment.setListStorage(new ListStorageMock());
            fm.beginTransaction().add(
                    m_ListStorageFragment, ListStorageFragment.TAG_LISTSTORAGE)
                    .commit();
        }


        Intent intent = getIntent();

        int shoppingListId;
        if (intent.hasExtra(EXTRA_SHOPPINGLISTID)) {
            shoppingListId = ((Long) getIntent().getExtras().get(EXTRA_SHOPPINGLISTID)).intValue();
            m_IsNewList = false;

        } else {

            shoppingListId = m_ListStorageFragment.getListStorage().createList();
            m_IsNewList = true;
        }

        m_ShoppingList = m_ListStorageFragment.getListStorage().loadList(shoppingListId);

        showActionBar();


        m_TextView_ShoppingListName = (TextView) findViewById(R.id.textView_ShoppingListName);
        m_TextView_ShoppingListName.setFocusable(true);
        m_TextView_ShoppingListName.setFocusableInTouchMode(true);
        m_TextView_ShoppingListName.requestFocus();
        m_TextView_ShoppingListName.setText(m_ShoppingList.getName());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //if a new list is created the create Button is displayed, else edit
        Button createCalendarEvent = (Button) findViewById(R.id.create_calendar_event);
        Button editCalendarEvent = (Button) findViewById(R.id.edit_calendar_event);


        Button deleteButton = (Button) findViewById(R.id.button_delete);
        if (m_IsNewList) {
            deleteButton.setVisibility(View.GONE);
            editCalendarEvent.setVisibility(View.GONE);
            createCalendarEvent.setVisibility(View.VISIBLE);
        } else {
            createCalendarEvent.setVisibility(View.GONE);
            editCalendarEvent.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        }

        attachEventHandlers();

    }

    //endregion


    //region Private Methods

    private void showActionBar() {

        //hide default action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);


        //show custom action bar
        View view = getLayoutInflater().inflate(R.layout.actionbar_save_cancel, null);
        actionBar.setCustomView(view);
    }


    private void attachEventHandlers() {

        ActionBar actionBar = getSupportActionBar();

        Button cancelButton = (Button) actionBar.getCustomView().findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });

        Button saveButton= (Button) actionBar.getCustomView().findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });

        Button deleteButton = (Button) findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_ListStorageFragment.getListStorage().deleteList(m_ShoppingList.getId());
                removeCalendarEvent();
                finish();
            }
        });

        m_EventDate.inittialize(m_ShoppingList.getCalendarEventDate());

        Button createCalendarEvent = (Button) findViewById(R.id.create_calendar_event);
        Button editCalendarEvent = (Button) findViewById(R.id.edit_calendar_event);

        if(m_ShoppingList.getCalendarEventDate().getCalendarEventId() == -1) {
            createCalendarEvent.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    showDialog(DATE_DIALOG_ID);
                }
            });
        }else{
            editCalendarEvent.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    showDialog(DATE_DIALOG_ID);
                }
            });
        }

    }


    private void onCancel() {
        finish();
    }

    private void onSave() {
        m_ShoppingList.setName(m_TextView_ShoppingListName.getText().toString());
        m_ListStorageFragment.getListStorage().saveList(m_ShoppingList);
        writeEventToCalendar();
        finish();
    }

    //functionality for calendar use


    //set value of the pickers
    private void initializePickers(){
            //set current time as default value
        if(m_ShoppingList.getCalendarEventDate().getCalendarEventId() == -1) {
            final Calendar c = Calendar.getInstance();
            m_EventDate.setYear(c.get(Calendar.YEAR));
            m_EventDate.setMonth(c.get(Calendar.MONTH));
            m_EventDate.setDay(c.get(Calendar.DAY_OF_MONTH));
            m_EventDate.setHour(c.get(Calendar.HOUR_OF_DAY));
            m_EventDate.setMinute(c.get(Calendar.MINUTE));
        }else{
            m_EventDate.setYear(m_ShoppingList.getCalendarEventDate().getYear());
            m_EventDate.setMonth(m_ShoppingList.getCalendarEventDate().getMonth());
            m_EventDate.setDay(m_ShoppingList.getCalendarEventDate().getDay());
            m_EventDate.setHour(m_ShoppingList.getCalendarEventDate().getHour());
            m_EventDate.setMinute(m_ShoppingList.getCalendarEventDate().getMinute());
        }
    }

    //update date
    private void updateDate() {
        showDialog(TIME_DIALOG_ID);
    }


    public void writeEventToCalendar() {

        if(m_ShoppingList.getCalendarEventDate().getCalendarEventId() == -1) {
            //create Event
            long calID = 1;
            long startMillis;
            long endMillis;
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(m_EventDate.getYear(), m_EventDate.getMonth(), m_EventDate.getDay(),
                    m_EventDate.getHour(), m_EventDate.getMinute());
            startMillis = beginTime.getTimeInMillis();
            Calendar endTime = Calendar.getInstance();
            endTime.set(m_EventDate.getYear(), m_EventDate.getMonth(), m_EventDate.getDay(),
                    m_EventDate.getHour(), m_EventDate.getMinute() + 30);
            endMillis = endTime.getTimeInMillis();

            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.HAS_ALARM, true);
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, m_ShoppingList.getName());
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            TimeZone defaultTimeZone = TimeZone.getDefault();
            values.put(CalendarContract.Events.EVENT_TIMEZONE, defaultTimeZone.getID());
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            m_ShoppingList.getCalendarEventDate().setCalendarEventId((Long.parseLong(uri.getLastPathSegment())));


            //sets alarm
            ContentValues reminders = new ContentValues();
            reminders.put(CalendarContract.Reminders.EVENT_ID, m_ShoppingList.getCalendarEventDate().getCalendarEventId());
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            reminders.put(CalendarContract.Reminders.MINUTES, 0);

            Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);

        }else{
            //update Event
            long startMillis;
            long endMillis;

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(m_EventDate.getYear(), m_EventDate.getMonth(), m_EventDate.getDay(),
                    m_EventDate.getHour(), m_EventDate.getMinute());
            startMillis = beginTime.getTimeInMillis();
            Calendar endTime = Calendar.getInstance();
            endTime.set(m_EventDate.getYear(), m_EventDate.getMonth(), m_EventDate.getDay(),
                    m_EventDate.getHour(), m_EventDate.getMinute() + 30);
            endMillis = endTime.getTimeInMillis();

            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, m_ShoppingList.getName());
            Uri updateUri = null;
            updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, m_ShoppingList.
                    getCalendarEventDate().getCalendarEventId());
            int rows = getContentResolver().update(updateUri, values, null, null);


            //sets alarm
            ContentValues reminders = new ContentValues();
            reminders.put(CalendarContract.Reminders.EVENT_ID, m_ShoppingList.getCalendarEventDate().getCalendarEventId());
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            reminders.put(CalendarContract.Reminders.MINUTES, 0);

            Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);

        }
        m_ShoppingList.getCalendarEventDate().setYear(m_EventDate.getYear());
        m_ShoppingList.getCalendarEventDate().setMonth(m_EventDate.getMonth());
        m_ShoppingList.getCalendarEventDate().setDay(m_EventDate.getDay());
        m_ShoppingList.getCalendarEventDate().setHour(m_EventDate.getHour());
        m_ShoppingList.getCalendarEventDate().setMinute(m_EventDate.getMinute());

    }

    public void removeCalendarEvent() {
        //remove Event
        if (m_ShoppingList.getCalendarEventDate().getCalendarEventId() != -1) {
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            Uri deleteUri = null;
            deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, m_ShoppingList.
                    getCalendarEventDate().getCalendarEventId());
            int rows = getContentResolver().delete(deleteUri, null, null);
        }
    }


    //Datepicker dialog generation
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    m_EventDate.setYear(year);
                    m_EventDate.setMonth(monthOfYear);
                    m_EventDate.setDay(dayOfMonth);
                    updateDate();
                }
            };


    // Timepicker dialog generation
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    m_EventDate.setHour(hourOfDay);
                    m_EventDate.setMinute(minute);
                }
            };


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        m_EventDate.getYear(), m_EventDate.getMonth(), m_EventDate.getDay());

            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, m_EventDate.getHour(), m_EventDate.getMinute(), false);

        }
        return null;
    }




    //endregion



}
