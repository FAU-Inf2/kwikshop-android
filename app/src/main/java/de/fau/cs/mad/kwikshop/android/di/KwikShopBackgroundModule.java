package de.fau.cs.mad.kwikshop.android.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;

/**
 * Module for background components
 * (no dependencies on Activity)
 */
@SuppressWarnings("unused")
@Module(overrides = true, library = true, includes = KwikShopBaseModule.class)
public class KwikShopBackgroundModule {

    private final Context context;


    public KwikShopBackgroundModule(Context context) {

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        this.context =context;
    }


    @Provides
    public Context provideContext() {
        return this.context;
    }

}
