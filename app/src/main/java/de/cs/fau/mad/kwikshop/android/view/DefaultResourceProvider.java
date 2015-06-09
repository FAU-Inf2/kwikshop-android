package de.cs.fau.mad.kwikshop.android.view;

import android.content.Context;

import javax.inject.Inject;

import de.cs.fau.mad.kwikshop.android.viewmodel.common.ResourceProvider;

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
}
