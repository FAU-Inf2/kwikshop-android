package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;

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
}
