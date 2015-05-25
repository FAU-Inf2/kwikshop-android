package de.cs.fau.mad.quickshop.android.viewmodel.common;

public abstract class Command<T> {

    private boolean isAvailable = true;
    private boolean canExecute = true;
    private CommandListener listener;


    public void setListener(CommandListener listener) {
        if (listener == null) {
            this.listener = new NullListener();
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


    private class NullListener implements CommandListener {

        @Override
        public void onIsAvailableChanged(boolean newValue) {

        }

        @Override
        public void onCanExecuteChanged(boolean newValue) {

        }
    }


}
