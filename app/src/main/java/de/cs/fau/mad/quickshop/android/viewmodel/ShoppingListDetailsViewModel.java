package de.cs.fau.mad.quickshop.android.viewmodel;

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

import de.cs.fau.mad.quickshop.android.common.CalendarEventDate;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.ListStorage;
import de.cs.fau.mad.quickshop.android.model.messages.ShoppingListChangeType;
import de.cs.fau.mad.quickshop.android.model.messages.ShoppingListChangedEvent;
import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ViewLauncher;
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
    private final ViewLauncher viewLauncher;
    private int shoppingListId;
    private final boolean newShoppingList;
    private ShoppingList shoppingList;

    // backing fields for properties
    private final Command saveCommand;
    private final Command cancelCommand;
    private final Command deleteCommand;
    private final Command editCalendarEventCommand;
    private final Command createCalendarEventCommand;
    private final Command deleteCalendarEventCommand;
    private CalendarEventDate calendarEventDate = new CalendarEventDate();


    /**
     * Initializes a new instance of ShoppingListDetailsViewModel without an associated shopping list
     * (will create a new list on save)
     */
    public ShoppingListDetailsViewModel(final Context context, final ViewLauncher viewLauncher,
                                        final ListStorage listStorage) {
        this(context, viewLauncher, listStorage, -1);
    }

    /**
     * Initializes a new instance of ShoppingListDetailsViewModel for the specified shopping list
     * (will modify the shopping lsit on save)
     *
     * @param shoppingListId The id of the shopping list to create a view model for
     */
    public ShoppingListDetailsViewModel(final Context context, final ViewLauncher viewLauncher,
                                        final ListStorage listStorage, final int shoppingListId) {

        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        if (viewLauncher == null) {
            throw new IllegalArgumentException("'viewLauncher' must not be null");
        }

        if (listStorage == null) {
            throw new IllegalArgumentException("'listStorage' must not be null");
        }

        this.context = context;
        this.viewLauncher = viewLauncher;
        this.listStorage = listStorage;
        this.shoppingListId = shoppingListId;
        this.newShoppingList = shoppingListId == -1;

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

    public void onEvent(CalendarEventDate eventDate) {
        setCalendarEventDate(eventDate);
    }

    @Override
    protected ShoppingListViewModelBase.Listener getListener() {
        return compositeListener;
    }


    private void setUp() {

        this.saveCommand.setIsAvailable(true);
        this.cancelCommand.setIsAvailable(true);

        if (newShoppingList) {

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

            boolean calendarEventExists = shoppingList.getCalendarEventDate().getCalendarEventId() != -1;
            this.editCalendarEventCommand.setIsAvailable(calendarEventExists);
            this.createCalendarEventCommand.setIsAvailable(!calendarEventExists);
            this.deleteCalendarEventCommand.setIsAvailable(calendarEventExists);

            setName(shoppingList.getName());
        }

        EventBus.getDefault().register(this);
    }

    private void saveCommandExecute() {

        if (newShoppingList) {
            this.shoppingListId = listStorage.createList();
            shoppingList = listStorage.loadList(shoppingListId);
        }
        CalendarEventDate calendarEventDate = getCalendarEventDate();


        shoppingList.setName(this.getName());
        if (calendarEventDate.getIsSet()) {
            writeEventToCalendar();
            shoppingList.getCalendarEventDate().setYear(calendarEventDate.getYear());
            shoppingList.getCalendarEventDate().setMonth(calendarEventDate.getMonth());
            shoppingList.getCalendarEventDate().setDay(calendarEventDate.getDay());
            shoppingList.getCalendarEventDate().setHour(calendarEventDate.getHour());
            shoppingList.getCalendarEventDate().setMinute(calendarEventDate.getMinute());

        }

        listStorage.saveList(shoppingList);

        EventBus.getDefault().post(new ShoppingListChangedEvent(shoppingList.getId(), ShoppingListChangeType.PropertiesModified));

        finish();

    }

    private void cancelCommandExecute() {
        finish();
    }

    private void deleteCommandExecute() {

        if (newShoppingList) {
            throw new UnsupportedOperationException();
        }

        if (shoppingList.getCalendarEventDate().getCalendarEventId() != -1) {
            deleteCalendarEventCommandExecute();
        }

        listStorage.deleteList(this.shoppingListId);
        EventBus.getDefault().post(new ShoppingListChangedEvent(this.shoppingListId, ShoppingListChangeType.Deleted));
        finish();

    }

    private void editCalendarEventCommandExecute() {
        createCalendarEventCommandExecute();
    }

    private void deleteCalendarEventCommandExecute() {

        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,
                shoppingList.getCalendarEventDate().getCalendarEventId());
        int rows = context.getContentResolver().delete(deleteUri, null, null);
    }

    private void createCalendarEventCommandExecute() {

        setCalendarEventDate(new CalendarEventDate());
        CalendarEventDate eventDate = getCalendarEventDate();
        eventDate.initialize();

        viewLauncher.showDatePicker(eventDate.getYear(), eventDate.getMonth(), eventDate.getDay(),
                eventDate.getHour(), eventDate.getMinute());

    }

    private void writeEventToCalendar() {

        CalendarEventDate eventDate = getCalendarEventDate();

        if (shoppingList.getCalendarEventDate().getCalendarEventId() == -1) {

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
            values.put(CalendarContract.Events.TITLE, shoppingList.getName());
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            TimeZone defaultTimeZone = TimeZone.getDefault();
            values.put(CalendarContract.Events.EVENT_TIMEZONE, defaultTimeZone.getID());
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            shoppingList.getCalendarEventDate().setCalendarEventId((Long.parseLong(uri.getLastPathSegment())));

            //sets alarm
            ContentValues reminders = new ContentValues();
            reminders.put(CalendarContract.Reminders.EVENT_ID, shoppingList.getCalendarEventDate().getCalendarEventId());
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
            values.put(CalendarContract.Events.TITLE, shoppingList.getName());
            Uri updateUri;
            updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, shoppingList.
                    getCalendarEventDate().getCalendarEventId());
            int rows = context.getContentResolver().update(updateUri, values, null, null);


            //sets alarm
            ContentValues reminders = new ContentValues();
            reminders.put(CalendarContract.Reminders.EVENT_ID, shoppingList.getCalendarEventDate().getCalendarEventId());
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            reminders.put(CalendarContract.Reminders.MINUTES, 0);

            Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);


        }
        //todo: does not get saved
        shoppingList.getCalendarEventDate().setYear(eventDate.getYear());
        shoppingList.getCalendarEventDate().setMonth(eventDate.getMonth());
        shoppingList.getCalendarEventDate().setDay(eventDate.getDay());
        shoppingList.getCalendarEventDate().setHour(eventDate.getHour());
        shoppingList.getCalendarEventDate().setMinute(eventDate.getMinute());

    }
}
