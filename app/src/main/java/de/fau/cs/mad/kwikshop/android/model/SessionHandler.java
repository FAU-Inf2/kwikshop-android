package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import static de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper.*;

public class SessionHandler {



    public static boolean isAuthenticated(Context context) {
        return (getSessionToken(context) != null && getSessionUser(context) != null);
    }

    public static void setSessionUser(Context context, String token) {
        saveString(SESSION_USER, token, context);
    }

    public static String getSessionUser(Context context) {
        return loadString(SESSION_USER, null, context);
    }

    public static void setSessionToken(Context context, String token) {
        saveString(SESSION_TOKEN, token, context);
    }

    public static String getSessionToken(Context context) {
        return loadString(SESSION_TOKEN, null, context);
    }

    public static void logout(Context context) {
        setSessionUser(context, null);
        setSessionToken(context, null);
    }

}
