package de.fau.cs.mad.kwikshop.android.di;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import de.fau.cs.mad.kwikshop.android.model.DatabaseHelper;
import de.fau.cs.mad.kwikshop.android.model.DeletedItem;
import de.fau.cs.mad.kwikshop.android.model.DeletedList;
import de.fau.cs.mad.kwikshop.android.model.synchronization.CompositeSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.ItemSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.ListSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.RecipeSynchronizer;
import de.fau.cs.mad.kwikshop.android.model.synchronization.ShoppingListSynchronizer;
import de.fau.cs.mad.kwikshop.android.restclient.ListClient;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactory;
import de.fau.cs.mad.kwikshop.android.restclient.RestClientFactoryImplementation;
import de.fau.cs.mad.kwikshop.android.util.ClientEqualityComparer;
import de.fau.cs.mad.kwikshop.android.util.StackTraceReporter;
import de.fau.cs.mad.kwikshop.android.view.DefaultClipboardHelper;
import de.fau.cs.mad.kwikshop.android.view.IoServiceImplementation;
import de.fau.cs.mad.kwikshop.android.view.LocationFragment;
import de.fau.cs.mad.kwikshop.android.view.ServerIntegrationDebugActivity;
import de.fau.cs.mad.kwikshop.android.view.SettingFragment;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListFragment;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.LocationViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ClipboardHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.IoService;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.common.CalendarEventDate;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RecipeServer;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.ShoppingListServer;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.DefaultDataProvider;
import de.fau.cs.mad.kwikshop.android.model.LocationFinderHelper;
import de.fau.cs.mad.kwikshop.android.model.RecipeManager;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.ShoppingListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.view.DefaultResourceProvider;
import de.fau.cs.mad.kwikshop.android.view.DefaultViewLauncher;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListItemDetailsFragment;
import de.fau.cs.mad.kwikshop.android.view.RecipeItemDetailsFragment;
import de.fau.cs.mad.kwikshop.android.view.ReminderFragment;
import de.fau.cs.mad.kwikshop.android.viewmodel.AddRecipeToShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ListOfRecipesViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ListOfShoppingListsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipesDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;
import de.fau.cs.mad.kwikshop.common.conversion.ObjectConverter;
import de.fau.cs.mad.kwikshop.common.conversion.RecipeConverter;
import de.fau.cs.mad.kwikshop.common.conversion.ShoppingListConverter;
import de.fau.cs.mad.kwikshop.common.util.EqualityComparer;

@Module(injects = {
        ListOfShoppingListsViewModel.class,
        ShoppingListDetailsViewModel.class,
        DisplayHelper.class,
        ShoppingListViewModel.class,
        ListOfRecipesViewModel.class,
        RecipesDetailsViewModel.class,
        RecipeViewModel.class,
        AutoCompletionHelper.class,
        LocationFinderHelper.class,
        AddRecipeToShoppingListViewModel.class,
        ShoppingListItemDetailsFragment.class,
        RecipeItemDetailsFragment.class,
        ReminderFragment.class,
        StackTraceReporter.class,
        SettingFragment.class,
        ServerIntegrationDebugActivity.class,
        RegularlyRepeatHelper.class,
        ItemDetailsViewModel.class,
        ShoppingListItemDetailsViewModel.class,
        RecipeItemDetailsViewModel.class,
        ShoppingListFragment.class,
        LocationFragment.class,
        LocationViewModel.class,
        ShoppingListSynchronizer.class,
        RecipeSynchronizer.class,
        CompositeSynchronizer.class,
        ItemSynchronizer.class
},
        library = true)
@SuppressWarnings("unused")
public class KwikShopModule {

    private static ListManager<ShoppingList> shoppingListManager;
    private static ListManager<Recipe> recipeManager;
    private static RegularlyRepeatHelper regularlyRepeatHelper;
    private final Activity currentActivity;

    public KwikShopModule(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    @Provides
    public ViewLauncher provideViewLauncher(Activity activity, ResourceProvider resourceProvider) {
        return new DefaultViewLauncher(activity, resourceProvider);
    }

    @Provides
    public Activity provideActivity() {
        return currentActivity;
    }

    @Provides
    public ListStorage<ShoppingList> provideShoppingListStorage() {
        return ListStorageFragment.getLocalListStorage();
    }

    @Provides
    @Deprecated
    public ListStorage<Recipe> provideRecipeStorage() {
        return ListStorageFragment.getRecipeStorage();
    }


    @Provides
    public Context provideContext() {
        return currentActivity;
    }

    @Provides
    public ResourceProvider provideResourceProvider(Context context) {
        return new DefaultResourceProvider(context);
    }

    @Provides
    public SimpleStorage<Unit> provideUnitStorage() {
        return ListStorageFragment.getUnitStorage();
    }

    @Provides
    public SimpleStorage<Group> provideGroupStorage() {
        return ListStorageFragment.getGroupStorage();
    }

    @Provides
    public SimpleStorage<LastLocation> provideLocationStorage() {
        return ListStorageFragment.getLastLocationStorage();
    }

    @Provides
    public SimpleStorage<CalendarEventDate> provideCalendarEventStorage() {
        return ListStorageFragment.getCalendarEventStorage();
    }

    @Provides
    public DefaultDataProvider provideDefaultDataProvider(Context context) {
        return new DefaultDataProvider(context);
    }

    @Provides
    public DisplayHelper provideDisplayHelper(ResourceProvider resourceProvider) {
        return new DisplayHelper(resourceProvider);
    }

    @Provides
    public AutoCompletionHelper provideAutoCompletionHelper(Activity activity) {
        return AutoCompletionHelper.getAutoCompletionHelper(activity.getBaseContext());
    }

    @Provides
    public LocationFinderHelper provideLocationFinderHelper(Context context) {
        return new LocationFinderHelper(context);
    }


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

    @Provides
    public RegularlyRepeatHelper provideRegularlyRepeatHelper(DatabaseHelper databaseHelper) {

        if (regularlyRepeatHelper == null) {
            regularlyRepeatHelper = new RegularlyRepeatHelper(databaseHelper);
        }
        return regularlyRepeatHelper;
    }

    @Provides
    public ClipboardHelper provideClipboardHelper(Activity activity) {
        return new DefaultClipboardHelper(activity);
    }

    @Provides
    public IoService provideIoService(Activity activity) {
        return new IoServiceImplementation(activity);
    }

    @Provides
    public RestClientFactory provideRestClientFactory(Context context, ResourceProvider resourceProvider) {
        return new RestClientFactoryImplementation(context, resourceProvider);
    }

    @Provides
    public DatabaseHelper provideDatabaseHelper(Context context) {
        return new DatabaseHelper(context);
    }

    @Provides
    public EqualityComparer provideEqualityComparer() {
        return new ClientEqualityComparer();
    }

    @Provides
    public SimpleStorage<DeletedList> provideDeletedListStorage() {
        return ListStorageFragment.getDeletedListStorage();
    }

    @Provides
    public SimpleStorage<DeletedItem> provideDeletedItemStorage() {
        return ListStorageFragment.getDeletedItemStorage();
    }

    @Provides
    public ListClient<ShoppingListServer> provideShoppingListClient(RestClientFactory clientFactory) {
        return clientFactory.getShoppingListClient();
    }

    @Provides
    public ObjectConverter<ShoppingList, ShoppingListServer> provideShoppingListConverter() {
        return new ShoppingListConverter();
    }

    @Provides
    public ListClient<RecipeServer> provideRecipeClient(RestClientFactory clientFactory) {
        return clientFactory.getRecipeClient();
    }

    @Provides
    public ObjectConverter<Recipe, RecipeServer> provideRecipeConverter() {
        return new RecipeConverter();
    }


    @Provides
    public ListSynchronizer<ShoppingList, ShoppingListServer> provideShoppingListSynchronizer(ShoppingListSynchronizer shoppingListSynchronizer) {
        return shoppingListSynchronizer;
    }

    @Provides
    public ListSynchronizer<Recipe, RecipeServer> provideRecipeSynchronizer(RecipeSynchronizer recipeSynchronizer) {
        return recipeSynchronizer;
    }

}
