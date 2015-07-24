package de.fau.cs.mad.kwikshop.android.model;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Date;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.restclient.RecipeResource;
import de.fau.cs.mad.kwikshop.android.restclient.ShoppingListResource;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;


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

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {

                @Override
                public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

                    return new Date(json.getAsLong());
                }
            })
            .create();



        return new RestAdapter.Builder()
                .setEndpoint(getApiEndPoint())
                .setConverter(new GsonConverter(gson))
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
