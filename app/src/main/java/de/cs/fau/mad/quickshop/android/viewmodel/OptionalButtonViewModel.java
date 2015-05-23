package de.cs.fau.mad.quickshop.android.viewmodel;

import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;

public class OptionalButtonViewModel<T> {

    //listener interface
    public interface Listener {

        void onIsAvailableChanged(boolean newValue);

    }

    private Listener listener;

    private boolean isAvailable;
    private final Command<T> command;


    public OptionalButtonViewModel(Command<T> command) {
        if (command == null) {
            throw new IllegalArgumentException("'command' must not be null");
        }

        this.command = command;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean value) {
        if (value != isAvailable) {
            isAvailable = value;
            if (listener != null) {
                listener.onIsAvailableChanged(value);
            }
        }
    }


    public Command<T> getCommand() {
        return command;
    }
}



