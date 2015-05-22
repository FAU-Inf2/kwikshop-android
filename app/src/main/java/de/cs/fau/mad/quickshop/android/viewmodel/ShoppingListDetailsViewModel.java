package de.cs.fau.mad.quickshop.android.viewmodel;

import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;

public class ShoppingListDetailsViewModel extends ShoppingListViewModelBase {

    //listener interface
    public interface Listener extends ShoppingListViewModelBase.Listener {

    }

    //infrastructure references
    private Listener listener;

    // backing fields for properties
    private final Command saveCommand = null;   //TODO: initialize
    private final Command cancelCommand = null; //TODO: initialize
    private final Command deleteCommand = null; //TODO: initialize


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

    public Command getDeleteCommand() {
        return deleteCommand;
    }


    @Override
    public ShoppingListViewModelBase.Listener getListener() {
        return listener;
    }
}
