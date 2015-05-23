package de.cs.fau.mad.quickshop.android.viewmodel.common;

public interface Command<T> {

    void execute(T parameter);

}
