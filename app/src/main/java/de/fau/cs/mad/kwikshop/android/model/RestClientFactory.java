package de.fau.cs.mad.kwikshop.android.model;

import de.fau.cs.mad.kwikshop.android.restclient.RecipeResource;
import de.fau.cs.mad.kwikshop.android.restclient.ShoppingListResource;

public interface RestClientFactory {

    ShoppingListResource getShoppingListClient();

    RecipeResource getRecipeClient();

}

