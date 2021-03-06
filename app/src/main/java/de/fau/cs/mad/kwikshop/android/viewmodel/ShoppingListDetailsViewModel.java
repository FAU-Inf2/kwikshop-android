package de.fau.cs.mad.kwikshop.android.viewmodel;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;

import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangeType;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.tasks.RedeemSharingCodeTask;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.view.ReminderFragment;
import de.fau.cs.mad.kwikshop.common.CalendarEventDate;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.RepeatType;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.NullCommand;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

public class ShoppingListDetailsViewModel extends ListDetailsViewModel<ShoppingList> {

    //other fields
    private final Context context;
    private final SimpleStorage<CalendarEventDate> calendarEventStorage;

    private ShoppingList shoppingList;

    // backing fields for properties
    private Command editCalendarEventCommand;
    private Command createCalendarEventCommand;
    private Command deleteCalendarEventCommand;
    private CalendarEventDate calendarEventDate;


    /**
     * Initializes a new instance of ShoppingListDetailsViewModel for the specified shopping list
     * (will modify the shopping list on save)
     */
    @Inject
    public ShoppingListDetailsViewModel(final Context context,
                                        final ViewLauncher viewLauncher,
                                        final ResourceProvider resourceProvider,
                                        final ListManager<ShoppingList> listManager,
                                        final SimpleStorage<CalendarEventDate> calendarEventStorage) {

        super(viewLauncher, resourceProvider, listManager);

        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        if(calendarEventStorage == null) {
            throw new IllegalArgumentException("'calendarEventStorage' must not be null");
        }

        this.context = context;
        this.calendarEventStorage = calendarEventStorage;

    }


    // Getters / Setters

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

    private boolean ownsRepeatOnNewListItems() {
        Iterator<Item> itr = shoppingList.getItems().iterator();
        while(itr.hasNext()){
            if(itr.next().getRepeatType() == RepeatType.ListCreation)
                return true;
        }
        return false;
    }

    private void deleteRepeatOnNewListItemsCommandExecute() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Iterator<Item> itr = shoppingList.getItems().iterator();
                while(itr.hasNext()){
                    Item item = itr.next();
                    if(item.getRepeatType() == RepeatType.ListCreation) {
                        EventBus.getDefault().post(new ItemChangedEvent(ListType.ShoppingList,
                                ItemChangeType.Deleted,
                                listId, item.getId()));
                        item.setRepeatType(RepeatType.None);
                        item.setRemindAtDate(null);
                        item.setRemindFromNextPurchaseOn(false);
                        listManager.saveListItem(shoppingList.getId(), item);
                    }
                }
                listManager.saveList(shoppingList.getId());
            }
        });

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CalendarEventDate eventDate) {

        if(getCalendarEventDate() != null) {
            eventDate.setAndroidCalendarId(getCalendarEventDate().getAndroidCalendarId());
            calendarEventStorage.deleteSingleItem(getCalendarEventDate());
        }
        setCalendarEventDate(eventDate);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void initializeCommands() {

        super.initializeCommands();

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
    }

    @Override
    protected void setUp() {

        super.setUp();

        if (isNewList) {

            this.deleteCommand.setIsAvailable(false);
            this.editCalendarEventCommand.setIsAvailable(false);
            this.createCalendarEventCommand.setIsAvailable(true);
            this.deleteCalendarEventCommand.setIsAvailable(false);

            setName("");

        } else {

            this.deleteCommand.setIsAvailable(true);

            //TODO: handle exception when list is not found
            this.shoppingList = listManager.getList(listId);
            setName(shoppingList.getName());
            setCalendarEventDate(shoppingList.getCalendarEventDate());

            boolean calendarEventExists = shoppingList.getCalendarEventDate() != null;
            this.editCalendarEventCommand.setIsAvailable(calendarEventExists);
            this.createCalendarEventCommand.setIsAvailable(!calendarEventExists);
            this.deleteCalendarEventCommand.setIsAvailable(calendarEventExists);

        }

        EventBus.getDefault().register(this);
    }


    @Override
    protected void saveCommandExecute() {
        if (isNewList) {
            listId = listManager.createList();
            shoppingList = listManager.getList(listId);
        }

        shoppingList.setName(this.getName());

        if (getCalendarEventDate() != null) {

            writeEventToCalendar();

            calendarEventStorage.addItem(getCalendarEventDate());
        }

        shoppingList.setCalendarEventDate(getCalendarEventDate());

        listManager.saveList(listId);

        // if we just created a shopping list, open it right away,
        // when a existing shopping list was edited, just close the current view and go back to
        // whatever the previous screen was
        if (isNewList) {
            viewLauncher.showShoppingListWithSupermarketDialog(listId);
        }

        finish();
    }

    @Override
    protected void deleteCommandExecute() {

        if (isNewList) {
            throw new UnsupportedOperationException();
        }

        if(SharedPreferencesHelper.loadBoolean(SharedPreferencesHelper.SL_DELETION_SHOW_AGAIN_MSG, true, context)) {
            viewLauncher.showMessageDialogWithCheckbox(resourceProvider.getString(R.string.deleteShoppingList_DialogTitle),
                    resourceProvider.getString(R.string.deleteShoppingList_DialogText), resourceProvider.getString(R.string.delete),
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {

                            if (getCalendarEventDate() != null) {
                                deleteCalendarEventCommandExecute();
                            }

                            listManager.deleteList(listId);
                            finish();
                        }
                    },
                    null, null, resourceProvider.getString(R.string.cancel),
                    NullCommand.VoidInstance, resourceProvider.getString(R.string.dont_show_this_message_again), false,
                    new Command<Void>() {
                        @Override
                        public void execute(Void parameter) {
                            SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.SL_DELETION_SHOW_AGAIN_MSG, false, context);
                        }
                    }, null);
           if (ownsRepeatOnNewListItems()){
                    viewLauncher.showMessageDialogWithCheckbox(resourceProvider.getString(R.string.deleteShoppingList_repeatingItemsTitle),
                            resourceProvider.getString(R.string.deleteShoppingList_repeatingItemsText), resourceProvider.getString(R.string.delete),
                            new Command<Void>() {
                                @Override
                                public void execute(Void parameter) {
                                    deleteRepeatOnNewListItemsCommandExecute();
                                }
                            },
                            null, null, resourceProvider.getString(R.string.cancel),
                            NullCommand.VoidInstance, resourceProvider.getString(R.string.dont_show_this_message_again), false,
                            new Command<Void>() {
                                @Override
                                public void execute(Void parameter) {
                                    SharedPreferencesHelper.saveBoolean(SharedPreferencesHelper.REPEAT_DELETION_SHOW_AGAIN_MSG, false, context);
                                }
                            }, null);
            }
        }else{
            if (getCalendarEventDate() != null) {
                deleteCalendarEventCommandExecute();
            }

            listManager.deleteList(listId);
            finish();
        }
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

    public void updateSharingCode(String value, Activity activity) {
        String[] sharingCode = value.split("#");
        if (sharingCode.length == 3 ||
                (sharingCode.length == 2 && value.substring(value.length() - 1).equals("#")) ) {
            new RedeemSharingCodeTask(context, activity).execute(sharingCode[1]);
        }
    }

}
