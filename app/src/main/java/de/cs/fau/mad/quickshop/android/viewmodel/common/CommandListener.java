package de.cs.fau.mad.quickshop.android.viewmodel.common;

public interface CommandListener {

    void onIsAvailableChanged(boolean newValue);

    void onCanExecuteChanged(boolean newValue);

}
