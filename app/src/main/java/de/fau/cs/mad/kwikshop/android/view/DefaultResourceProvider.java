package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.Locale;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;

public class DefaultResourceProvider implements ResourceProvider {

    private final Context context;

    @Inject
    public DefaultResourceProvider(Context context) {

        if (context == null) {
            throw new IllegalArgumentException("'context' must not be null");
        }

        this.context = context;
    }


    @Override
    public String getString(int id) {
        return context.getResources().getString(id);
    }

    @Override
    public int getInteger(int id) {
        return context.getResources().getInteger(id);
    }

    @Override
    public Locale getLocale() {
        return context.getResources().getConfiguration().locale;
    }

    @Override
    public String[] getStringArray(int id) {
        return context.getResources().getStringArray(id);
    }

    @Override
    public Drawable getDrawable(int id) {
        return context.getResources().getDrawable(id);
    }
}
