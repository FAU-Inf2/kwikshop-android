package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import de.fau.cs.mad.kwikshop.android.model.ArgumentNullException;

/**
 * Wrappes a Void Command in another command of any parameter type.
 * All Methods are redirected to the wrapped instance.
 * The parameter passed to the wrapped instance will always be null.
 *
 * This allows to pass a Command<Void> to be passed to methods that expect a different command parameter
 */
public class ParameterLessCommandWrapper<T> extends Command<T> {



    private final Command<Void> wrappedCommand;


    public ParameterLessCommandWrapper(Command<Void> wrappedCommand) {
        if(wrappedCommand == null) {
            throw new ArgumentNullException("wrappedCommand");
        }

        this.wrappedCommand = wrappedCommand;
    }


    @Override
    public void setListener(CommandListener listener) {
        wrappedCommand.setListener(listener);
    }

    @Override
    public boolean getIsAvailable() {
        return wrappedCommand.getIsAvailable();
    }

    @Override
    public void setIsAvailable(boolean value) {
        wrappedCommand.setIsAvailable(value);
    }

    @Override
    public boolean getCanExecute() {
        return wrappedCommand.getCanExecute();
    }

    @Override
    public void setCanExecute(boolean value) {
        wrappedCommand.setCanExecute(value);
    }

    @Override
    public void execute(T parameter) {

        wrappedCommand.execute(null);
    }


}
