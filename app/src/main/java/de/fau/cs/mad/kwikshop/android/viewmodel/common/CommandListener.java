package de.fau.cs.mad.kwikshop.android.viewmodel.common;

public interface CommandListener {

    void onIsAvailableChanged(boolean newValue);

    void onCanExecuteChanged(boolean newValue);

}
