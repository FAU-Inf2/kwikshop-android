package de.fau.cs.mad.kwikshop.android.util;

import android.content.Context;
import android.content.SharedPreferences;

//TODO: make methods non-static, make usages use a injected instance instead
public class SharedPreferencesHelper {

    //Settings keys
    public static final String SESSION_TOKEN = "SESSION_TOKEN";
    public static final String SESSION_USER  = "SESSION_USER";
    public static final String SHOPPING_MODE = "shopping_mode_setting";
    public static final String SKIP_LOGIN = "SKIPLOGIN";
    public static final String LOCALE = "locale";
    public static final String API_ENDPOINT = "API_ENDPOINT";
    public static final String LOCATION_PERMISSION = "location_permission";
    public static final String ITEM_DELETION_SHOW_AGAIN_MSG = "ITEM_DELETION_SHOW_AGAIN_MSG";
    public static final String SL_DELETION_SHOW_AGAIN_MSG = "SL_DELETION_SHOW_AGAIN_MSG";
    public static final String RECIPE_DELETION_SHOW_AGAIN_MSG = "RECIPE_DELETION_SHOW_AGAIN_MSG";
    public static final String LOCATION_PERMISSION_SHOW_AGAIN_MSG = "location_permission_show_again";
    public static final String ITEM_SEPARATOR_WORD = "ITEM_SEPARATOR_WORD";
    public static final String ENABLE_SYNCHRONIZATION = "ENABLE_SYNCHRONIZATION";
    public static final String SYNCHRONIZATION_INTERVAL = "SYNCHRONIZATION_INTERVAL";

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
