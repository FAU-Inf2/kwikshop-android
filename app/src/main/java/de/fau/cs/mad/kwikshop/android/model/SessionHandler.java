package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import android.content.SharedPreferences;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.view.SettingFragment;

public class SessionHandler {

    private static final String SESSION_TOKEN = "SESSION_TOKEN";

    public static boolean isAuthenticated(Context context) {
        return (getSessionToken(context) != null ? true : false);
    }

    public static void setSessionToken(Context context, String token) {
        SharedPreferencesHelper.saveString(SESSION_TOKEN, token, context);
    }

    public static String getSessionToken(Context context) {
        return SharedPreferencesHelper.loadString(SESSION_TOKEN, null,context);
    }

    public static void logout(Context context) {
        setSessionToken(context, null);
    }

}
