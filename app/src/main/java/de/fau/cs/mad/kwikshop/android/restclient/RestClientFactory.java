package de.fau.cs.mad.kwikshop.android.restclient;


import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.android.restclient.LeaseResource;


public interface RestClientFactory {

    ListClient<ShoppingListServer> getShoppingListClient();

    ListClient<RecipeServer> getRecipeClient();

    LeaseResource getLeaseClient();
}

