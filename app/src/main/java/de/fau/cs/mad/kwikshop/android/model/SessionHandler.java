package de.fau.cs.mad.kwikshop.android.model;

import android.content.Context;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;

public class SessionHandler {

    private static final String SESSION_TOKEN = "SESSION_TOKEN";

    private static final String authenticationEndpoint = "http://ec2-52-28-159-181.eu-central-1.compute.amazonaws.com:8080/users/auth";

    // TODO: production client_id
    private static final String client_id = "974373376910-mg6fm7feie2rn0v9qj2nmi1jpeftr47u.apps.googleusercontent.com";

    public static String getAuthenticationEndpoint() {
        return authenticationEndpoint;
    }

    public static String getClient_id() {
        return client_id;
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
