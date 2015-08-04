package de.fau.cs.mad.kwikshop.android.restclient;

import de.fau.cs.mad.kwikshop.android.restclient.RecipeResource;
import de.fau.cs.mad.kwikshop.android.restclient.ShoppingListResource;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;

public interface RestClientFactory {

    ListClient<ShoppingListServer> getShoppingListClient();

    ListClient<RecipeServer> getRecipeClient();

}

