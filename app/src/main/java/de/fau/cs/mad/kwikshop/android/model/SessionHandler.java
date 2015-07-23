package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import static de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper.*;

public class SessionHandler {



    public static boolean isAuthenticated(Context context) {
        return (getSessionToken(context) != null ? true : false);
    }

    public static void setSessionToken(Context context, String token) {
        saveString(SESSION_TOKEN, token, context);
    }

    public static String getSessionToken(Context context) {
        return loadString(SESSION_TOKEN, null, context);
    }

    public static void logout(Context context) {
        setSessionToken(context, null);
    }

}
