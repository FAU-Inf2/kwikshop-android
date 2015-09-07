package de.fau.cs.mad.kwikshop.android.viewmodel.common;

import android.graphics.drawable.Drawable;

import java.util.Locale;

public interface ResourceProvider {

    String getString(int id);

    int getInteger(int id);

    Locale getLocale();

    String[] getStringArray(int id);

    Drawable getDrawable(int id);
}
