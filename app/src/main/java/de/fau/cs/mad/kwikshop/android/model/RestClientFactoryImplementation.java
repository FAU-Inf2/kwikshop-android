package de.fau.cs.mad.kwikshop.android.model;


import android.content.Context;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.restclient.RecipeResource;
import de.fau.cs.mad.kwikshop.android.restclient.ShoppingListResource;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import retrofit.RestAdapter;


public class RestClientFactoryImplementation implements RestClientFactory {

    private final Context context;
    private final ResourceProvider resourceProvider;

    @Inject
    public RestClientFactoryImplementation(Context context, ResourceProvider resourceProvider){

        if(context == null) {
            throw new ArgumentNullException("context");
        }

        if(resourceProvider == null) {
            throw new ArgumentNullException("resourceProvider");
        }

        this.context = context;
        this.resourceProvider = resourceProvider;
    }


    @Override
    public ShoppingListResource getShoppingListClient() {
        return getRestAdapter().create(ShoppingListResource.class);
    }

    @Override
    public RecipeResource getRecipeClient() {
        return getRestAdapter().create(RecipeResource.class);
    }


    private RestAdapter getRestAdapter() {

        return new RestAdapter.Builder()
                .setEndpoint(getApiEndPoint())
                //TODO: get actual username / password
                .setRequestInterceptor(new BasicAuthenticationRequestInterceptor("DEBUG", "DEBUG"))
                .build();
    }

    private String getApiEndPoint() {
        return SharedPreferencesHelper.loadString(SharedPreferencesHelper.API_ENDPOINT,
                resourceProvider.getString(R.string.API_URL),
                context);
    }

}
