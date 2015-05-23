package de.cs.fau.mad.quickshop.android.viewmodel;

import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.ListStorage;
import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;

public class ShoppingListDetailsViewModel extends ShoppingListViewModelBase {

    //listener interface
    public interface Listener extends ShoppingListViewModelBase.Listener {

    }

    //other fields
    private Listener listener;
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
    private final OptionalButtonViewModel deleteButton;
    private final OptionalButtonViewModel editCalendarEventButton;
    private final OptionalButtonViewModel createCalendarEventButton;


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
            public void execute() {
                saveCommandExecute();
            }
        };
        this.cancelCommand = new Command() {
            @Override
            public void execute() {
                cancelCommandExecute();
            }
        };
        this.deleteCommand = new Command() {
            @Override
            public void execute() {
                deleteCommandExecute();
            }
        };
        this.editCalendarEventCommand = new Command() {
            @Override
            public void execute() {
                editCalendarEventCommandExecute();
            }
        };
        this.createCalendarEventCommand = new Command() {
            @Override
            public void execute() {
                createCalendarEventCommandExecute();
            }
        };
        this.deleteButton = new OptionalButtonViewModel(deleteCommand);
        this.editCalendarEventButton = new OptionalButtonViewModel(editCalendarEventCommand);
        this.createCalendarEventButton = new OptionalButtonViewModel(createCalendarEventCommand);
        setUp();


    }


    public void setListener(Listener value) {
        this.listener = value;
    }

    // Getters / Setters

    public Command getSaveCommand() {
        return saveCommand;
    }

    public Command getCancelCommand() {
        return cancelCommand;
    }

    public OptionalButtonViewModel getDeleteButton() {
        return deleteButton;
    }

    public OptionalButtonViewModel getEditCalendarEventButton() {
        return editCalendarEventButton;
    }

    public OptionalButtonViewModel getCreateCalendarEventButton() {
        return createCalendarEventButton;
    }



    @Override
    protected ShoppingListViewModelBase.Listener getListener() {
        return listener;
    }

    private void setUp() {

        if (!newShoppingList) {

            //TODO: handle exception when list is not found
            this.shoppingList = listStorage.loadList(shoppingListId);
            setName(shoppingList.getName());
            getDeleteButton().setIsAvailable(true);

            boolean calendarEventExists = shoppingList.getCalendarEventDate().getCalendarEventId() != -1;
            getEditCalendarEventButton().setIsAvailable(calendarEventExists);
            getCreateCalendarEventButton().setIsAvailable(!calendarEventExists);
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
