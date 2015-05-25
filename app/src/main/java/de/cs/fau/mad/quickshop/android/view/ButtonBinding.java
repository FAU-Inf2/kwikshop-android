package de.cs.fau.mad.quickshop.android.view;

import android.view.View;
import android.widget.Button;

import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;
import de.cs.fau.mad.quickshop.android.viewmodel.common.CommandListener;

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

        this.button.setOnClickListener(this);
        this.command.setListener(this);
    }


    @Override
    public void onIsAvailableChanged(boolean newValue) {
        button.setVisibility(newValue ? View.GONE : View.VISIBLE);
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
