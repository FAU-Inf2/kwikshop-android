package de.cs.fau.mad.kwikshop.android.viewmodel.common;

public interface CommandListener {

    void onIsAvailableChanged(boolean newValue);

    void onCanExecuteChanged(boolean newValue);

}
