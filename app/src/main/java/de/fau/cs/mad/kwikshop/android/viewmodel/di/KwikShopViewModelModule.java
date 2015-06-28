package de.fau.cs.mad.kwikshop.android.viewmodel.di;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import de.fau.cs.mad.kwikshop.android.common.CalendarEventDate;
import de.fau.cs.mad.kwikshop.android.common.Group;
import de.fau.cs.mad.kwikshop.android.common.ShoppingList;
import de.fau.cs.mad.kwikshop.android.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.DefaultDataProvider;
import de.fau.cs.mad.kwikshop.android.model.ListStorage;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.RecipeStorage;
import de.fau.cs.mad.kwikshop.android.model.ShoppingListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.view.DefaultResourceProvider;
import de.fau.cs.mad.kwikshop.android.view.DefaultViewLauncher;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.AddRecipeToShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ListOfRecipesViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ListOfShoppingListsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipesDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;

@Module(injects = {
        ListOfShoppingListsViewModel.class,
        ShoppingListDetailsViewModel.class,
        DisplayHelper.class,
        ShoppingListViewModel.class,
        ListOfRecipesViewModel.class,
        RecipesDetailsViewModel.class,
        RecipeViewModel.class,
        AutoCompletionHelper.class,
        AddRecipeToShoppingListViewModel.class
},
        library = true)
@SuppressWarnings("unused")
public class KwikShopViewModelModule {

    private final Activity currentActivity;

    public KwikShopViewModelModule(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    @Provides
    public ViewLauncher provideViewLauncher(Activity activity) {
        return new DefaultViewLauncher(activity);
    }

    @Provides
    public Activity provideActivity() {
        return currentActivity;
    }

    @Provides
    public ListStorage provideListStorage() {
        return ListStorageFragment.getLocalListStorage();
    }

    @Provides
    public RecipeStorage provideRecipeStorage() {
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
    public SimpleStorage<CalendarEventDate> provideCalendarEventStorage() {
        return ListStorageFragment.getCalendarEventStorage();
    }

    @Provides
    public DefaultDataProvider provideDefaultDataProvider(Context context) {
        return new DefaultDataProvider(context);
    }

    @Provides
    public DisplayHelper provideDisplayHelper(Context context) {
        return new DisplayHelper(context);
    }

    @Provides
    public AutoCompletionHelper provideAutoCompletionHelper(Activity activity) {
        return AutoCompletionHelper.getAutoCompletionHelper(activity.getBaseContext());
    }

    @Provides
    public ListManager<ShoppingList> provideShoppingListManager(ListStorage listStorage) {
        return new ShoppingListManager(listStorage);
    }
}
