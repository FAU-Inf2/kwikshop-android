package de.cs.fau.mad.quickshop.android;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.TimeZone;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.CalendarEventDate;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.messages.ShoppingListChangeType;
import de.cs.fau.mad.quickshop.android.model.messages.ShoppingListChangedEvent;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.view.interfaces.SaveCancelActivity;
import de.greenrobot.event.EventBus;

public class ShoppingListDetailFragment extends Fragment {


    //region Constants

    public static final String EXTRA_SHOPPINGLISTID = "extra_ShoppingListId";
    private static final String ARG_SECTION_NUMBER = "section_number";


    //endregion


    //region Fields

    private boolean m_IsNewList = false;
    private ListStorageFragment m_ListStorageFragment;
    private ShoppingList m_ShoppingList;

    private TextView m_TextView_ShoppingListName;

    private CalendarEventDate m_EventDate = new CalendarEventDate();

    private View rootView;

    //endregion


    public static ShoppingListDetailFragment newInstance(int sectionNumber) {

        ShoppingListDetailFragment fragment = new ShoppingListDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;

    }

    //region Overrides

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.activity_shopping_list_detail, container, false);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity().getSupportFragmentManager(), getActivity().getApplicationContext());

        /*FragmentManager fm = getActivity().getSupportFragmentManager();
        m_ListStorageFragment = (ListStorageFragment) fm.findFragmentByTag(ListStorageFragment.TAG_LISTSTORAGE);
        if (m_ListStorageFragment == null) {
            m_ListStorageFragment = new ListStorageFragment();
            //m_ListStorageFragment.setListStorage(new ListStorageMock());
            fm.beginTransaction().add(
                    m_ListStorageFragment, ListStorageFragment.TAG_LISTSTORAGE)
                    .commit();
        }*/

        Intent intent = getActivity().getIntent();

        int shoppingListId;
        if (intent.hasExtra(EXTRA_SHOPPINGLISTID)) {
            shoppingListId = ((Long) intent.getExtras().get(EXTRA_SHOPPINGLISTID)).intValue();
            m_IsNewList = false;

        } else {

            shoppingListId = m_ListStorageFragment.getLocalListStorage().createList();
            m_IsNewList = true;
        }

        m_ShoppingList = m_ListStorageFragment.getLocalListStorage().loadList(shoppingListId);

        m_TextView_ShoppingListName = (TextView) rootView.findViewById(R.id.textView_ShoppingListName);
        m_TextView_ShoppingListName.setFocusable(true);
        m_TextView_ShoppingListName.setFocusableInTouchMode(true);
        m_TextView_ShoppingListName.requestFocus();
        m_TextView_ShoppingListName.setText(m_ShoppingList.getName());

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //if a new list is created the create Button is displayed, else edit
        Button createCalendarEvent = (Button) rootView.findViewById(R.id.create_calendar_event);

        if (m_ShoppingList.getCalendarEventDate().getCalendarEventId() != -1) {
            createCalendarEvent.setText(R.string.edit_calendar_event);
        }

        EventBus.getDefault().register(this);

        Button deleteButton = (Button) rootView.findViewById(R.id.button_delete);
        if (m_IsNewList) {
            deleteButton.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.VISIBLE);
        }

        attachEventHandlers();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    //endregion


    //region Private Methods


    private void attachEventHandlers() {
        Activity activity = getActivity();
        if (activity instanceof SaveCancelActivity) {
            SaveCancelActivity saveCancelActivity = (SaveCancelActivity) activity;

            saveCancelActivity.setOnSaveClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSave();
                }
            });
            saveCancelActivity.setOnCancelClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCancel();
                }
            });

        }

        Button deleteButton = (Button) rootView.findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = m_ShoppingList.getId();
                removeCalendarEvent();
                m_ListStorageFragment.getLocalListStorage().deleteList(id);
                EventBus.getDefault().post(new ShoppingListChangedEvent(id, ShoppingListChangeType.Deleted));
                getActivity().finish();
            }
        });
        Button createCalendarEvent = (Button) rootView.findViewById(R.id.create_calendar_event);

        createCalendarEvent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                m_EventDate.initialize(m_ShoppingList.getCalendarEventDate());
                DialogFragment newFragment = new DatePickerFragment();
                Bundle args = new Bundle();
                args.putInt("year", m_EventDate.getYear());
                args.putInt("month", m_EventDate.getMonth());
                args.putInt("day", m_EventDate.getDay());
                args.putInt("hour", m_EventDate.getHour());
                args.putInt("minute", m_EventDate.getMinute());
                newFragment.setArguments(args);
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
            }
        });

    }


    private void onCancel() {
        getActivity().finish();
    }

    private void onSave() {
        m_ShoppingList.setName(m_TextView_ShoppingListName.getText().toString());
        if (m_EventDate.getIsSet()) {
            writeEventToCalendar();
            m_ShoppingList.getCalendarEventDate().setYear(m_EventDate.getYear());
            m_ShoppingList.getCalendarEventDate().setMonth(m_EventDate.getMonth());
            m_ShoppingList.getCalendarEventDate().setDay(m_EventDate.getDay());
            m_ShoppingList.getCalendarEventDate().setHour(m_EventDate.getHour());
            m_ShoppingList.getCalendarEventDate().setMinute(m_EventDate.getMinute());

        }
        m_ListStorageFragment.getLocalListStorage().saveList(m_ShoppingList);

        EventBus.getDefault().post(new ShoppingListChangedEvent(m_ShoppingList.getId(), ShoppingListChangeType.PropertiesModified));

        getActivity().finish();
    }

    public void onEvent(CalendarEventDate eventDate){
        this.m_EventDate = eventDate;
    }

    public void writeEventToCalendar() {


        if (m_ShoppingList.getCalendarEventDate().getCalendarEventId() == -1) {
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

            ContentResolver cr = getActivity().getContentResolver();
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


        } else {
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

            ContentResolver cr = getActivity().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, m_ShoppingList.getName());
            Uri updateUri;
            updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, m_ShoppingList.
                    getCalendarEventDate().getCalendarEventId());
            int rows = getActivity().getContentResolver().update(updateUri, values, null, null);


            //sets alarm
            ContentValues reminders = new ContentValues();
            reminders.put(CalendarContract.Reminders.EVENT_ID, m_ShoppingList.getCalendarEventDate().getCalendarEventId());
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            reminders.put(CalendarContract.Reminders.MINUTES, 0);

            Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);


        }
        //todo: does not get saved
        m_ShoppingList.getCalendarEventDate().setYear(m_EventDate.getYear());
        m_ShoppingList.getCalendarEventDate().setMonth(m_EventDate.getMonth());
        m_ShoppingList.getCalendarEventDate().setDay(m_EventDate.getDay());
        m_ShoppingList.getCalendarEventDate().setHour(m_EventDate.getHour());
        m_ShoppingList.getCalendarEventDate().setMinute(m_EventDate.getMinute());



    }


    public void removeCalendarEvent() {
        //remove Event
        if (m_ShoppingList.getCalendarEventDate().getCalendarEventId() != -1) {
            ContentResolver cr = getActivity().getContentResolver();
            ContentValues values = new ContentValues();
            Uri deleteUri;
            deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, m_ShoppingList.
                    getCalendarEventDate().getCalendarEventId());
            int rows = getActivity().getContentResolver().delete(deleteUri, null, null);

        }

    }


    //endregion


}
