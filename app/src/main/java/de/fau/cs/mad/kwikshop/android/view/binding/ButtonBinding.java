package de.fau.cs.mad.kwikshop.android.view.binding;

import android.view.View;

import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.CommandListener;

/**
 * Support class to easily wire up a Button in a view with a Command from the view model
 * TODO: Figure out a way to support commands with parameters
 */
public class ButtonBinding extends Binding implements CommandListener, View.OnClickListener {


    private final View button;
    private final Command<?> command;


    public ButtonBinding(View button, Command<?> command) {

        if (button == null) {
            throw new IllegalArgumentException("'button' must not be null");
        }

        if (command == null) {
            throw new IllegalArgumentException("'command' must not be null");
        }

        this.button = button;
        this.command = command;


        button.setVisibility(command.getIsAvailable() ? View.VISIBLE : View.GONE);
        button.setEnabled(command.getCanExecute());

        this.button.setOnClickListener(this);
        this.command.setListener(this);
    }


    @Override
    public void onIsAvailableChanged(boolean newValue) {
        button.setVisibility(newValue ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCanExecuteChanged(boolean newValue) {
        button.setEnabled(newValue);
    }

    @Override
    public void onClick(View v) {

        if (command.getCanExecute()) {
            command.execute(null);
        }

    }

}
