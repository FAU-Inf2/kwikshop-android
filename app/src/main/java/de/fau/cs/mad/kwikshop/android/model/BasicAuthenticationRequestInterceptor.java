package de.fau.cs.mad.kwikshop.android.model;

import android.util.Base64;

import retrofit.RequestInterceptor;

public class BasicAuthenticationRequestInterceptor implements RequestInterceptor {

    private final String userName;
    private final String password;


    public BasicAuthenticationRequestInterceptor(String userName, String password) {

        if(userName == null) {
            throw new ArgumentNullException("userName");
        }

        if(password == null) {
            throw new ArgumentNullException("password");
        }

        this.userName = userName;
        this.password = password;
    }



    @Override
    public void intercept(RequestFacade requestFacade) {

        final String authorizationValue = encodeCredentialsForBasicAuthorization();
        requestFacade.addHeader("Authorization", authorizationValue);

    }

    private String encodeCredentialsForBasicAuthorization() {
        final String userAndPassword = userName + ":" + password;
        return "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);
    }



}
