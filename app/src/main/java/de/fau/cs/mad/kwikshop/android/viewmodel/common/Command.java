package de.fau.cs.mad.kwikshop.android.viewmodel.common;

public abstract class Command<T> {

    private static CommandListener nullListener = new NullListener();

    private boolean isAvailable = true;
    private boolean canExecute = true;
    private CommandListener listener = nullListener;


    public void setListener(CommandListener listener) {
        if (listener == null) {
            this.listener = nullListener;
        } else {
            this.listener = listener;
        }
    }


    public boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean value) {
        if (value != isAvailable) {
            isAvailable = value;
            listener.onIsAvailableChanged(value);
        }
    }

    public boolean getCanExecute() {
        return canExecute && getIsAvailable();
    }

    public void setCanExecute(boolean value) {
        if (value != canExecute) {
            canExecute = value;
            listener.onCanExecuteChanged(value);
        }
    }

    public abstract void execute(T parameter);


    private static class NullListener implements CommandListener {

        @Override
        public void onIsAvailableChanged(boolean newValue) {

        }

        @Override
        public void onCanExecuteChanged(boolean newValue) {

        }
    }


}
