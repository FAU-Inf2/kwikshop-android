package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.CalendarEventDate;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;

public class ShoppingListDetailsViewModel extends ShoppingListViewModelBase {

    //listener interface
    public interface Listener extends ShoppingListViewModelBase.Listener {

    }

    private class CompositeListener implements Listener {

        @Override
        public void onNameChanged(String value) {
            for (Listener l : listeners) {
                l.onNameChanged(value);
            }
        }

        @Override
        public void onFinish() {
            for (Listener l : listeners) {
                l.onFinish();
            }
        }
    }


    //other fields
    private List<Listener> listeners = new LinkedList<>();
    private Listener compositeListener = new CompositeListener();

    private final Context context;
    private final ListStorage listStorage;
    private final SimpleStorage<CalendarEventDate> calendarEventStorage;
    private final ViewLauncher viewLauncher;
    private final ResourceProvider resourceProvider;

    private int shoppingListId;
    private boolean isNewShoppingList;
    private ShoppingList shoppingList;

    // backing fields for properties
    private Command saveCommand;
    private Command cancelCommand;
    private Command deleteCommand;
    private Command editCalendarEventCommand;
    private Command createCalendarEventCommand;
    private Command deleteCalendarEventCommand;
    private CalendarEventDate calendarEventDate;



    /**
     * Initializes a new instance of ShoppingListDetailsViewModel for the specified shopping list
     * (will modify the shopping list on save)
     */
    @Inject
    public ShoppingListDetailsViewModel(final Context context, final ViewLauncher viewLauncher,
                                        final ResourceProvider resourceProvider,
                                        final ListStorage listStorage, final SimpleStorage<CalendarEventDate> calendarEventStorage) {

        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        if (viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }

        if (resourceProvider == null) {
            throw new IllegalArgumentException("'resourceProvider' must not be null");
        }

        if (listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        if(calendarEventStorage == null) {
            throw new IllegalArgumentException("'calendarEventStorage' must not be null");
        }

        this.context = context;
        this.viewLauncher = viewLauncher;
        this.resourceProvider = resourceProvider;
        this.listStorage = listStorage;
        this.calendarEventStorage = calendarEventStorage;

    }

    public void initialize() {
        initialize(-1);
    }

    public void initialize(int shoppingListId) {
        this.shoppingListId = shoppingListId;
        this.isNewShoppingList = shoppingListId == -1;

        this.saveCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                saveCommandExecute();
            }
        };
        this.cancelCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                cancelCommandExecute();
            }
        };
        this.deleteCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                deleteCommandExecute();
            }
        };
        this.editCalendarEventCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                editCalendarEventCommandExecute();
            }
        };
        this.createCalendarEventCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                createCalendarEventCommandExecute();
            }
        };
        this.deleteCalendarEventCommand = new Command() {
            @Override
            public void execute(Object parameter) {
                deleteCalendarEventCommandExecute();
            }
        };


        setUp();
    }

    public void addListener(Listener value) {
        this.listeners.add(value);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }


    // Getters / Setters

    public Command getSaveCommand() {
        return saveCommand;
    }

    public Command getCancelCommand() {
        return cancelCommand;
    }

    public Command getDeleteCommand() {
        return this.deleteCommand;
    }

    public Command getEditCalendarEventCommand() {
        return this.editCalendarEventCommand;
    }

    public Command getCreateCalendarEventCommand() {
        return this.createCalendarEventCommand;
    }

    public Command getDeleteCalendarEventCommand() {
        return deleteCalendarEventCommand;
    }

    private CalendarEventDate getCalendarEventDate() {
        return this.calendarEventDate;
    }

    private void setCalendarEventDate(CalendarEventDate value) {
        this.calendarEventDate = value;
    }

    @Override
    public void setName(String value) {
        super.setName(value);
        getSaveCommand().setCanExecute(getName() != null && getName().trim().length() > 0);
    }

    public boolean getIsNewList() {
        return isNewShoppingList;
    }


    @Override
    public void finish() {
        EventBus.getDefault().unregister(this);
        super.finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(CalendarEventDate eventDate) {

        if(getCalendarEventDate() != null) {
            eventDate.setAndroidCalendarId(getCalendarEventDate().getAndroidCalendarId());
            calendarEventStorage.deleteSingleItem(getCalendarEventDate());
        }
        setCalendarEventDate(eventDate);
    }

    @Override
    protected ShoppingListViewModelBase.Listener getListener() {
        return compositeListener;
    }


    private void setUp() {

        this.saveCommand.setIsAvailable(true);
        this.cancelCommand.setIsAvailable(true);

        if (isNewShoppingList) {

            this.deleteCommand.setIsAvailable(false);
            this.editCalendarEventCommand.setIsAvailable(false);
            this.createCalendarEventCommand.setIsAvailable(true);
            this.deleteCalendarEventCommand.setIsAvailable(false);

            setName("");

        } else {

            this.deleteCommand.setIsAvailable(true);

            //TODO: handle exception when list is not found
            this.shoppingList = listStorage.loadList(shoppingListId);
            setName(shoppingList.getName());
            setCalendarEventDate(shoppingList.getCalendarEventDate());

            boolean calendarEventExists = shoppingList.getCalendarEventDate() != null;
            this.editCalendarEventCommand.setIsAvailable(calendarEventExists);
            this.createCalendarEventCommand.setIsAvailable(!calendarEventExists);
            this.deleteCalendarEventCommand.setIsAvailable(calendarEventExists);

        }

        EventBus.getDefault().register(this);
    }

    private void saveCommandExecute() {
        if (isNewShoppingList) {
            this.shoppingListId = listStorage.createList();
            shoppingList = listStorage.loadList(shoppingListId);
        }

        shoppingList.setName(this.getName());

        if (getCalendarEventDate() != null) {

            writeEventToCalendar();

            calendarEventStorage.addItem(getCalendarEventDate());
        }

        shoppingList.setCalendarEventDate(getCalendarEventDate());

        listStorage.saveList(shoppingList);

        ShoppingListChangeType changeType = isNewShoppingList
                ? ShoppingListChangeType.Added
                : ShoppingListChangeType.PropertiesModified;

        EventBus.getDefault().post(new ShoppingListChangedEvent(changeType, shoppingList.getId()));

        // if we just created a shopping list, open it right away,
        // when a existing shopping list was edited, just close the current view and go back to
        // whatever the previous screen was
        if (isNewShoppingList) {
            viewLauncher.showShoppingList(shoppingListId);
        }


        finish();

    }

    private void cancelCommandExecute() {
        finish();
    }

    private void deleteCommandExecute() {

        if (isNewShoppingList) {
            throw new UnsupportedOperationException();
        }

        viewLauncher.showYesNoDialog(
                resourceProvider.getString(R.string.deleteShoppingList_DialogTitle),
                resourceProvider.getString(R.string.deleteShoppingList_DialogText),
                new Command() {
                    @Override
                    public void execute(Object parameter) {

                        if (getCalendarEventDate() != null) {
                            deleteCalendarEventCommandExecute();
                        }

                        listStorage.deleteList(shoppingListId);
                        EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.Deleted, shoppingListId));
                        finish();
                    }
                },
                NullCommand.Instance);
    }

    private void editCalendarEventCommandExecute() {
        createCalendarEventCommandExecute();
    }

    private void deleteCalendarEventCommandExecute() {

        if(getCalendarEventDate() != null && getCalendarEventDate().getAndroidCalendarId() != -1) {
            Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,
                                                       getCalendarEventDate().getAndroidCalendarId());
            int rows = context.getContentResolver().delete(deleteUri, null, null);
        }
    }

    private void createCalendarEventCommandExecute() {

        CalendarEventDate eventDate = getCalendarEventDate() != null
                ? getCalendarEventDate()
                : CalendarEventDate.now();

        viewLauncher.showDatePicker(eventDate.getYear(), eventDate.getMonth(), eventDate.getDay(),
                eventDate.getHour(), eventDate.getMinute());
    }

    private void writeEventToCalendar() {

        CalendarEventDate eventDate = getCalendarEventDate();

        if(eventDate== null) {
            return;
        }

        if (eventDate.getAndroidCalendarId() == -1) {

            //create Event
            long calID = 1;
            long startMillis;
            long endMillis;
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(eventDate.getYear(), eventDate.getMonth(), eventDate.getDay(),
                    eventDate.getHour(), eventDate.getMinute());
            startMillis = beginTime.getTimeInMillis();
            Calendar endTime = Calendar.getInstance();
            endTime.set(eventDate.getYear(), eventDate.getMonth(), eventDate.getDay(),
                    eventDate.getHour(), eventDate.getMinute() + 30);
            endMillis = endTime.getTimeInMillis();

            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.HAS_ALARM, true);
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, "[Kwik Shop] " + shoppingList.getName());
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            TimeZone defaultTimeZone = TimeZone.getDefault();
            values.put(CalendarContract.Events.EVENT_TIMEZONE, defaultTimeZone.getID());
            values.put(CalendarContract.Events.DESCRIPTION, context.getString(R.string.intent_calendar_description) + Integer.toString(shoppingList.getId()));
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            eventDate.setAndroidCalendarId((Long.parseLong(uri.getLastPathSegment())));

            //sets alarm
            ContentValues reminders = new ContentValues();
            reminders.put(CalendarContract.Reminders.EVENT_ID, eventDate.getAndroidCalendarId());
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            reminders.put(CalendarContract.Reminders.MINUTES, 0);

            Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);


        } else {

            //update Event
            long startMillis;
            long endMillis;

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(eventDate.getYear(), eventDate.getMonth(), eventDate.getDay(),
                    eventDate.getHour(), eventDate.getMinute());
            startMillis = beginTime.getTimeInMillis();
            Calendar endTime = Calendar.getInstance();
            endTime.set(eventDate.getYear(), eventDate.getMonth(), eventDate.getDay(),
                    eventDate.getHour(), eventDate.getMinute() + 30);
            endMillis = endTime.getTimeInMillis();

            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, "[Kwik Shop] " + shoppingList.getName());
            values.put(CalendarContract.Events.DESCRIPTION, context.getString(R.string.intent_calendar_description) + Integer.toString(shoppingList.getId()));
            Uri updateUri;
            updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventDate.getAndroidCalendarId());
            int rows = context.getContentResolver().update(updateUri, values, null, null);


            //sets alarm
            ContentValues reminders = new ContentValues();
            reminders.put(CalendarContract.Reminders.EVENT_ID, eventDate.getAndroidCalendarId());
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            reminders.put(CalendarContract.Reminders.MINUTES, 0);

            try {
                Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);
            } catch(android.database.sqlite.SQLiteException e){

                //todo: maybe remove CalendarEventDate, so the user is able to set a new one?
                setCalendarEventDate(null);
                e.printStackTrace();
            }
        }

    }
}
