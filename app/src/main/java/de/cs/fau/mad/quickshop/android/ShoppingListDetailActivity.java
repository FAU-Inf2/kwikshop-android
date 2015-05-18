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
import de.cs.fau.mad.quickshop.android.messages.ChangeType;
import de.cs.fau.mad.quickshop.android.messages.ShoppingListChangedEvent;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.model.mock.ListStorageMock;
import de.greenrobot.event.EventBus;

public class ShoppingListDetailActivity extends ActionBarActivity {


    //region Constants

    public static final String EXTRA_SHOPPINGLISTID = "extra_ShoppingListId";

    //endregion


    //region Fields

    private boolean m_IsNewList = false;
    private ListStorageFragment m_ListStorageFragment;
    private ShoppingList m_ShoppingList;

    private TextView m_TextView_ShoppingListName;

    private int m_Year;
    private int m_Month;
    private int m_Day;
    private int m_Hour;
    private int m_Minute;

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

                int listId = m_ShoppingList.getId();

                m_ListStorageFragment.getListStorage().deleteList(listId);
                removeCalendarEvent();

                EventBus.getDefault().post(new ShoppingListChangedEvent(listId, ChangeType.Deleted));

                finish();
            }
        });

        initializePickers();

        Button createCalendarEvent = (Button) findViewById(R.id.create_calendar_event);
        Button editCalendarEvent = (Button) findViewById(R.id.edit_calendar_event);

        if(m_ShoppingList.getCalendarEventId() == -1) {
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

        if(m_IsNewList) {
            m_ListStorageFragment.getListStorage().deleteList(m_ShoppingList.getId());
        }

        finish();
    }

    private void onSave() {
        m_ShoppingList.setName(m_TextView_ShoppingListName.getText().toString());
        m_ListStorageFragment.getListStorage().saveList(m_ShoppingList);
        writeEventToCalendar();

        ChangeType changeType = m_IsNewList
                ? ChangeType.Added
                : ChangeType.PropertiesModified;

        EventBus.getDefault().post(new ShoppingListChangedEvent(m_ShoppingList.getId(), changeType));

        finish();
    }

    //functionality for calendar use


    //set value of the pickers
    private void initializePickers(){
            //set current time as default value
            final Calendar c = Calendar.getInstance();
            m_Year = c.get(Calendar.YEAR);
            m_Month = c.get(Calendar.MONTH);
            m_Day = c.get(Calendar.DAY_OF_MONTH);
            m_Hour = c.get(Calendar.HOUR_OF_DAY);
            m_Minute = c.get(Calendar.MINUTE);
            //TODO: set old value for update
    }

    //update date
    private void updateDate() {
        showDialog(TIME_DIALOG_ID);
    }


    public void writeEventToCalendar() {

        if(m_ShoppingList.getCalendarEventId() == -1) {
            //create Event
            long calID = 1;
            long startMillis = 0;
            long endMillis = 0;
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(m_Year, m_Month, m_Day, m_Hour, m_Minute);
            startMillis = beginTime.getTimeInMillis();
            Calendar endTime = Calendar.getInstance();
            endTime.set(m_Year, m_Month, m_Day, m_Hour, m_Minute + 30);
            endMillis = endTime.getTimeInMillis();

            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, m_ShoppingList.getName());
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            TimeZone defaultTimeZone = TimeZone.getDefault();
            values.put(CalendarContract.Events.EVENT_TIMEZONE, defaultTimeZone.getID());
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            m_ShoppingList.setCalendarEventId(Long.parseLong(uri.getLastPathSegment()));
        }else{
            //update Event
            long startMillis = 0;
            long endMillis = 0;

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(m_Year, m_Month, m_Day, m_Hour, m_Minute);
            startMillis = beginTime.getTimeInMillis();
            Calendar endTime = Calendar.getInstance();
            endTime.set(m_Year, m_Month, m_Day, m_Hour, m_Minute + 30);
            endMillis = endTime.getTimeInMillis();

            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, m_ShoppingList.getName());
            Uri updateUri = null;
            updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, m_ShoppingList.getCalendarEventId());
            int rows = getContentResolver().update(updateUri, values, null, null);
        }
    }

    public void removeCalendarEvent() {
        //remove Event
        if (m_ShoppingList.getCalendarEventId() != -1) {
            ContentResolver cr = getContentResolver();
            ContentValues values = new ContentValues();
            Uri deleteUri = null;
            deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, m_ShoppingList.getCalendarEventId());
            int rows = getContentResolver().delete(deleteUri, null, null);
        }
    }


    //Datepicker dialog generation
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    m_Year = year;
                    m_Month = monthOfYear;
                    m_Day = dayOfMonth;
                    updateDate();
                }
            };


    // Timepicker dialog generation
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    m_Hour = hourOfDay;
                    m_Minute = minute;
                }
            };


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        m_Year, m_Month, m_Day);

            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, m_Hour, m_Minute, false);

        }
        return null;
    }




    //endregion

}
