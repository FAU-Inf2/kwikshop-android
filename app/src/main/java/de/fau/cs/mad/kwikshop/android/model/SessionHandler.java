package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.view.SettingFragment;

public class SessionHandler {

    public static boolean isAuthenticated(Context context) {
        return (getSessionToken(context) != null ? true : false);
    }

    public static void setSessionToken(Context context, String token) {
        SharedPreferences sharedPref = context.getSharedPreferences(SettingFragment.SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.session_token), token);
        editor.commit();
    }

    public static String getSessionToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SettingFragment.SETTINGS, Context.MODE_PRIVATE);
        String sessionToken = sharedPref.getString(context.getString(R.string.session_token), null);
        return sessionToken;
    }

    public static void logout(Context context) {
        setSessionToken(context, null);
    }

}
