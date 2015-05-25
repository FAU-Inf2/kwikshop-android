package de.cs.fau.mad.quickshop.android.viewmodel;

import java.util.LinkedList;
import java.util.List;

import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.ListStorage;
import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;

public class ShoppingListDetailsViewModel extends ShoppingListViewModelBase {

    //listener interface
    public interface Listener extends ShoppingListViewModelBase.Listener {

    }

    private class CompositeListener implements Listener {

        @Override
        public void onNameChanged(String value) {
            for (Listener l : listeners) {
                onNameChanged(value);
            }
        }

        @Override
        public void onFinish() {
            for (Listener l : listeners) {
                onFinish();
            }
        }
    }


    //other fields
    private List<Listener> listeners = new LinkedList<>();
    private Listener compositeListener = new CompositeListener();

    private final ListStorage listStorage = null;  //TODO: initialize
    private final int shoppingListId;
    private final boolean newShoppingList;
    private ShoppingList shoppingList;

    // backing fields for properties
    private final Command saveCommand;
    private final Command cancelCommand;
    private final Command deleteCommand;
    private final Command editCalendarEventCommand;
    private final Command createCalendarEventCommand;


    /**
     * Initializes a new instance of ShoppingListDetailsViewModel without an associated shopping list
     * (will create a new list on save)
     */
    public ShoppingListDetailsViewModel() {
        this(-1);
    }

    /**
     * Initializes a new instance of ShoppingListDetailsViewModel for the specified shopping list
     * (will modify the shopping lsit on save)
     *
     * @param shoppingListId The id of the shopping list to create a view model for
     */
    public ShoppingListDetailsViewModel(final int shoppingListId) {

        this.newShoppingList = shoppingListId == -1;
        this.shoppingListId = shoppingListId;

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
            this.createCalendarEventCommand.setIsAvailable(false);

        } else {

            this.deleteCommand.setIsAvailable(true);

            //TODO: handle exception when list is not found
            this.shoppingList = listStorage.loadList(shoppingListId);
            setName(shoppingList.getName());

            boolean calendarEventExists = shoppingList.getCalendarEventDate().getCalendarEventId() != -1;
            this.editCalendarEventCommand.setIsAvailable(calendarEventExists);
            this.createCalendarEventCommand.setIsAvailable(!calendarEventExists);
        }
    }

    private void saveCommandExecute() {

        if (newShoppingList) {
            int listId = listStorage.createList();
            shoppingList = listStorage.loadList(listId);
        }

        shoppingList.setName(this.getName());
        listStorage.saveList(shoppingList);

        finish();

    }

    private void cancelCommandExecute() {
        finish();
    }

    private void deleteCommandExecute() {

        if (newShoppingList) {
            throw new UnsupportedOperationException();
        }

        listStorage.deleteList(this.shoppingListId);
        finish();

    }

    private void editCalendarEventCommandExecute() {
        //TODO
    }

    private void createCalendarEventCommandExecute() {
        //TODO
    }


}
