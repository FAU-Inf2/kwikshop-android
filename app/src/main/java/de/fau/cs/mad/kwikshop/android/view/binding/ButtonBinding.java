package de.fau.cs.mad.kwikshop.android.view.binding;

import android.view.View;
import android.widget.Button;

import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.CommandListener;

/**
 * Support class to easily wire up a Button in a view with a Command from the view model
 * TODO: Figure out a way to support commands with parameters
 */
public class ButtonBinding extends Binding implements CommandListener, View.OnClickListener {


    private final View button;
    private final Command<?> command;
    private final boolean bindIsAvailable;
    private final boolean bindCanExecute;

    /**
     * Creates a new binding between the specified button and command
     * @param button The button to be bound
     * @param command The command to be associated with the button
     */
    public ButtonBinding(View button, Command<?> command) {
        this(button, command, true, true);
    }

    /**
     * Create a new binding between the specified button and command
     * @param button The button to be bound
     * @param command The command to be associated with the button
     * @param bindIsAvailable Specify whether to hide the button if the command becomes unavailable
     */
    public ButtonBinding(View button, Command<?> command, boolean bindIsAvailable) {
        this(button, command, bindIsAvailable, true);
    }

    /**
     * Create a new binding between the specified button and command
     * @param button The button to be bound
     * @param command The command to be associated with the button
     * @param bindIsAvailable Specify whether to hide the button if the command becomes unavailable
     * @param bindCanExecute Specify whether to disable the button is the command can longer execute
     */
    public ButtonBinding(View button, Command<?> command, boolean bindIsAvailable, boolean bindCanExecute) {

        if (button == null) {
            throw new IllegalArgumentException("'button' must not be null");
        }

        if (command == null) {
            throw new IllegalArgumentException("'command' must not be null");
        }

        this.button = button;
        this.command = command;
        this.bindIsAvailable = bindIsAvailable;
        this.bindCanExecute = bindCanExecute;

        button.setVisibility(command.getIsAvailable() ? View.VISIBLE : View.GONE);
        button.setEnabled(command.getCanExecute());

        this.button.setOnClickListener(this);
        this.command.setListener(this);
    }





    @Override
    public void onIsAvailableChanged(boolean newValue) {
        if(bindIsAvailable) {
            button.setVisibility(newValue ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onCanExecuteChanged(boolean newValue) {
        if(bindCanExecute) {
            button.setEnabled(newValue);
        }
    }

    @Override
    public void onClick(View v) {

        if (command.getCanExecute()) {
            command.execute(null);
        }

    }

}
