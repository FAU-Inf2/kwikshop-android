package de.fau.cs.mad.kwikshop.android.restclient;

import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import retrofit.RequestInterceptor;

public class CompositeRequestInterceptor implements RequestInterceptor {


    private final RequestInterceptor[] interceptors;



    public CompositeRequestInterceptor(RequestInterceptor... interceptors) {

        if(interceptors == null) {
            throw new ArgumentNullException("interceptors");
        }

        for(int i = 0; i < interceptors.length; i++) {
            if(interceptors[i] == null) {
                throw new ArgumentNullException(String.format("interceptors[%s]", i));
            }
        }

        this.interceptors = interceptors;
    }



    @Override
    public void intercept(RequestFacade request) {
        for(RequestInterceptor interceptor : interceptors) {
            interceptor.intercept(request);
        }
    }

}
