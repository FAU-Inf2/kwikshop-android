package de.fau.cs.mad.kwikshop.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;

/**
 * Wrapper around SharedPreferencesHelper that gets the context passed through the constructor
 * so it does not have to be specified for each call
 *
 * Using this, most view models do not need to hold a reference to a Conext object
 */
public class SharedPreferencesWrapper {


    private final Context context;

    @Inject
    public SharedPreferencesWrapper(Context context) {

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        this.context = context;
    }

    public void saveString(String key, String value) {
      SharedPreferencesHelper.saveString(key, value, context);
    }

    public String loadString(String key, String defaultValue) {
        return SharedPreferencesHelper.loadString(key, defaultValue, context);
    }

    public void saveInt(String key, int value) {
        SharedPreferencesHelper.saveInt(key, value, context);
    }

    public int loadInt(String key, int defaultValue) {
        return SharedPreferencesHelper.loadInt(key, defaultValue, context);
    }

    public void saveBoolean(String key, Boolean value) {
        SharedPreferencesHelper.saveBoolean(key, value, context);
    }

    public boolean loadBoolean(String key, Boolean defaultValue) {
        return SharedPreferencesHelper.loadBoolean(key, defaultValue, context);
    }


}
