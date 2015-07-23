package de.fau.cs.mad.kwikshop.android.model;


import de.fau.cs.mad.kwikshop.android.restclient.RecipeResource;
import de.fau.cs.mad.kwikshop.android.restclient.ShoppingListResource;
import retrofit.RestAdapter;


public class RestClientFactoryImplementation implements RestClientFactory {

    @Override
    public ShoppingListResource getShoppingListClient() {

        //TODO: endPoint
        return getRestAdapter("http://HOST:PORT/shoppinglist").create(ShoppingListResource.class);
    }

    @Override
    public RecipeResource getRecipeClient() {
        //TODO: endPoint
        return getRestAdapter("http://HOST:PORT/recipe").create(RecipeResource.class);
    }


    private RestAdapter getRestAdapter(String endPoint) {

        return new RestAdapter.Builder()
                .setEndpoint(endPoint)
                //TODO: get actual username / password
                .setRequestInterceptor(new BasicAuthenticationRequestInterceptor("DEBUG", "DEBUG"))
                .build();
    }

}
