package de.fau.cs.mad.kwikshop.android.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String sharedPreferencesName = "settings";

    public static void saveString(String key, String value, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String loadString(String key, String defaultValue, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        String value = sharedPref.getString(key, defaultValue);
        return value;
    }

    public static void saveInt(String key, int value, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int loadInt(String key, int defaultValue, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        int value = sharedPref.getInt(key, defaultValue);
        return value;
    }

    public static void saveBoolean(String key, Boolean value, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static Boolean loadBoolean(String key, Boolean defaultValue, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        Boolean value = sharedPref.getBoolean(key, defaultValue);
        return value;
    }


}
