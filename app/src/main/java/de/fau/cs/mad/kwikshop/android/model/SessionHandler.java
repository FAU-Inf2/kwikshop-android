package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;

public class SessionHandler {

    private static final String SESSION_TOKEN = "SESSION_TOKEN";

    private static final String authenticationEndpoint = "http://ec2-52-28-159-181.eu-central-1.compute.amazonaws.com:8080/users/auth";

    public static String getAuthenticationEndpoint() {
        return authenticationEndpoint;
    }

    public static boolean isAuthenticated(Context context) {
        return (getSessionToken(context) != null ? true : false);
    }

    public static void setSessionToken(Context context, String token) {
        SharedPreferencesHelper.saveString(SESSION_TOKEN, token, context);
    }

    public static String getSessionToken(Context context) {
        return SharedPreferencesHelper.loadString(SESSION_TOKEN, null, context);
    }

    public static void logout(Context context) {
        setSessionToken(context, null);
    }

}
