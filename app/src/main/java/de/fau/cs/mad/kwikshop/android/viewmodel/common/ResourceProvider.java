package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import java.util.Locale;

public interface ResourceProvider {

    String getString(int id);

    int getInteger(int id);

    Locale getLocale();
}
