package de.fau.cs.mad.kwikshop.android.restclient;

import java.util.UUID;

import de.fau.cs.mad.kwikshop.common.rest.Constants;
import retrofit.RequestInterceptor;

/**
 * Interceptor that adds a custom kwikshop-client-id HTTP header uniquely identifying a client process
 */
public class ClientIdRequestInterceptor implements RequestInterceptor {

    // client id that stays the same for the lifetime of the process
    private final static String clientId = UUID.randomUUID().toString();



    public void intercept(RequestFacade request) {
        request.addHeader(Constants.KWIKSHOP_CLIENT_ID, clientId);

    }

}
