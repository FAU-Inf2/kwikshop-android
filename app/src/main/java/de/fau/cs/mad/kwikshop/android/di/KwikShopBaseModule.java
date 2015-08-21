package de.fau.cs.mad.kwikshop.android.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import de.fau.cs.mad.kwikshop.android.model.DatabaseHelper;
import de.fau.cs.mad.kwikshop.android.model.DefaultDataProvider;
import de.fau.cs.mad.kwikshop.android.model.DeletedItem;
import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.RecipeManager;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.ShoppingListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.synchronization.CompositeSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.ItemSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.ListSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.RecipeItemSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.RecipeSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.ShoppingListItemSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.ShoppingListSynchronizer;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactoryImplementation;
import de.fau.cs.mad.kwikshop.android.util.ClientEqualityComparer;
import de.fau.cs.mad.kwikshop.android.view.DefaultClipboardHelper;
import de.fau.cs.mad.kwikshop.android.view.DefaultResourceProvider;
import de.fau.cs.mad.kwikshop.android.view.IoServiceImplementation;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ClipboardHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.IoService;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.common.CalendarEventDate;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.conversion.ObjectConverter;
import de.fau.cs.mad.kwikshop.common.conversion.RecipeConverter;
import de.fau.cs.mad.kwikshop.common.conversion.ShoppingListConverter;
import de.fau.cs.mad.kwikshop.common.util.EqualityComparer;

@SuppressWarnings("unused")
@Module(library = true, injects = {
        DefaultResourceProvider.class,
        DefaultClipboardHelper.class,
        IoServiceImplementation.class,
        DefaultDataProvider.class,

        LocationFinderHelper.class,
        DatabaseHelper.class,

        RestClientFactoryImplementation.class,

        ClientEqualityComparer.class,
        ShoppingListConverter.class,
        RecipeConverter.class,
        ShoppingListSynchronizer.class,
        RecipeSynchronizer.class,
        ShoppingListItemSynchronizer.class,
        RecipeItemSynchronizer.class,
        CompositeSynchronizer.class
})
public class KwikShopBaseModule {

    private static ListManager<ShoppingList> shoppingListManager;
    private static ListManager<Recipe> recipeManager;
    private static RegularlyRepeatHelper regularlyRepeatHelper;


    //All modules that include this module must provide an actual provideContext() methods
    // in order for this to work.
    // remember to include "overrides = true" in the @Module annotation
    @Provides
    public Context provideContext() {
        throw new UnsupportedOperationException();
    }


    //region Android System wrappers

    @Provides
    public ResourceProvider provideResourceProvider(DefaultResourceProvider resourceProvider) {
        return resourceProvider;
    }

    @Provides
    public ClipboardHelper provideClipboardHelper(DefaultClipboardHelper implementation) {
        return implementation;
    }


    @Provides
    public IoService provideIoService(IoServiceImplementation ioService) {
        return ioService;
    }

    //endregion


    //region Database Access

    @Provides
    public ListStorage<ShoppingList> provideShoppingListStorage(Context context) {
        ListStorageFragment.SetupLocalListStorageFragment(context);
        return ListStorageFragment.getLocalListStorage();
    }

    @Provides
    @Deprecated
    public ListStorage<Recipe> provideRecipeStorage(Context context) {
        ListStorageFragment.SetupLocalListStorageFragment(context);
        return ListStorageFragment.getRecipeStorage();
    }

    @Provides
    public SimpleStorage<Unit> provideUnitStorage(Context context) {
        ListStorageFragment.SetupLocalListStorageFragment(context);
        return ListStorageFragment.getUnitStorage();
    }

    @Provides
    public SimpleStorage<Group> provideGroupStorage(Context context) {
        ListStorageFragment.SetupLocalListStorageFragment(context);
        return ListStorageFragment.getGroupStorage();
    }

    @Provides
    public SimpleStorage<LastLocation> provideLocationStorage(Context context) {
        ListStorageFragment.SetupLocalListStorageFragment(context);
        return ListStorageFragment.getLastLocationStorage();
    }

    @Provides
    public SimpleStorage<CalendarEventDate> provideCalendarEventStorage(Context context) {
        ListStorageFragment.SetupLocalListStorageFragment(context);
        return ListStorageFragment.getCalendarEventStorage();
    }


    @Provides
    public SimpleStorage<DeletedList> provideDeletedListStorage(Context context) {
        ListStorageFragment.SetupLocalListStorageFragment(context);
        return ListStorageFragment.getDeletedListStorage();
    }

    @Provides
    public SimpleStorage<DeletedItem> provideDeletedItemStorage(Context context) {
        ListStorageFragment.SetupLocalListStorageFragment(context);
        return ListStorageFragment.getDeletedItemStorage();
    }

    //endregion


    //region List Managers

    @Provides
    public ListManager<ShoppingList> provideShoppingListManager(ListStorage<ShoppingList> listStorage,
                                                                RegularlyRepeatHelper repeatHelper,
                                                                EqualityComparer equalityComparer,
                                                                SimpleStorage<DeletedList> deletedListStorage,
                                                                SimpleStorage<DeletedItem> deletedItemStorage) {
        if (shoppingListManager == null) {
            shoppingListManager = new ShoppingListManager(listStorage, repeatHelper, equalityComparer,
                    deletedListStorage, deletedItemStorage);
        }
        return shoppingListManager;
    }

    @Provides
    public ListManager<Recipe> provideRecipeManager(ListStorage<Recipe> listStorage,
                                                    EqualityComparer equalityComparer,
                                                    SimpleStorage<DeletedList> deletedListStorage,
                                                    SimpleStorage<DeletedItem> deltedItemStorage) {
        if (recipeManager == null) {
            recipeManager = new RecipeManager(listStorage, equalityComparer,
                    deletedListStorage, deltedItemStorage);
        }
        return recipeManager;
    }


    //endregion


    //region Helpers

    @Provides
    public RegularlyRepeatHelper provideRegularlyRepeatHelper(DatabaseHelper databaseHelper) {

        if (regularlyRepeatHelper == null) {
            regularlyRepeatHelper = new RegularlyRepeatHelper(databaseHelper);
        }
        return regularlyRepeatHelper;
    }

    //endregion


    //region REST

    @Provides
    public RestClientFactory provideRestClientFactory(RestClientFactoryImplementation implementation) {
        return implementation;
    }

    @Provides
    public ListClient<ShoppingListServer> provideShoppingListClient(RestClientFactory clientFactory) {
        return clientFactory.getShoppingListClient();
    }

    @Provides
    public ListClient<RecipeServer> provideRecipeClient(RestClientFactory clientFactory) {
        return clientFactory.getRecipeClient();
    }


    //endregion


    //region Synchronization

    @Provides
    public EqualityComparer provideEqualityComparer(ClientEqualityComparer implementation) {
        return implementation;
    }

    @Provides
    public ObjectConverter<ShoppingList, ShoppingListServer> provideShoppingListConverter(ShoppingListConverter converter) {
        return converter;
    }

    @Provides
    public ObjectConverter<Recipe, RecipeServer> provideRecipeConverter(RecipeConverter converter) {
        return converter;
    }

    @Provides
    public ListSynchronizer<ShoppingList, ShoppingListServer> provideShoppingListSynchronizer(ShoppingListSynchronizer shoppingListSynchronizer) {
        return shoppingListSynchronizer;
    }

    @Provides
    public ListSynchronizer<Recipe, RecipeServer> provideRecipeSynchronizer(RecipeSynchronizer recipeSynchronizer) {
        return recipeSynchronizer;
    }

    @Provides
    public ItemSynchronizer<ShoppingList, ShoppingListServer> provideShoppingListItemSynchronizer(ShoppingListItemSynchronizer synchronizer) {
        return synchronizer;
    }

    @Provides
    public ItemSynchronizer<Recipe, RecipeServer> provideRecipeItemSynchronizer(RecipeItemSynchronizer synchronizer) {
        return synchronizer;
    }

    //endregion



}
