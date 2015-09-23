package de.fau.cs.mad.kwikshop.android.di;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;


import de.fau.cs.mad.kwikshop.android.viewmodel.TutorialViewModel;
import de.fau.cs.mad.kwikshop.common.ArgumentNullException;
import de.fau.cs.mad.kwikshop.android.model.tasks.RedeemSharingCodeTask;

import de.fau.cs.mad.kwikshop.android.util.DefaultRecipesHelper;
import de.fau.cs.mad.kwikshop.android.util.StackTraceReporter;
import de.fau.cs.mad.kwikshop.android.view.BarcodeScannerFragment;
import de.fau.cs.mad.kwikshop.android.view.LocationActivity;
import de.fau.cs.mad.kwikshop.android.view.LocationFragment;
import de.fau.cs.mad.kwikshop.android.view.ServerIntegrationDebugActivity;
import de.fau.cs.mad.kwikshop.android.view.SettingFragment;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListActivity;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListFragment;
import de.fau.cs.mad.kwikshop.android.viewmodel.BarcodeScannerViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.LocationViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.BaseViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;

import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.view.DefaultViewLauncher;
import de.fau.cs.mad.kwikshop.android.view.DisplayHelper;
import de.fau.cs.mad.kwikshop.android.view.ShoppingListItemDetailsFragment;
import de.fau.cs.mad.kwikshop.android.view.RecipeItemDetailsFragment;
import de.fau.cs.mad.kwikshop.android.view.ReminderFragment;
import de.fau.cs.mad.kwikshop.android.viewmodel.ListOfRecipesViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ListOfShoppingListsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipesDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ViewLauncher;


/**
 * Module for use in all of the app's foreground componentes
 * (module can inject types that depend on Activity)
 */
@Module(
        includes = KwikShopBaseModule.class,
        overrides = true,
        library = true,
        injects = {

                // view models
                ListOfShoppingListsViewModel.class,
                ShoppingListDetailsViewModel.class,
                ShoppingListViewModel.class,
                ListOfRecipesViewModel.class,
                RecipesDetailsViewModel.class,
                RecipeViewModel.class,
                ItemDetailsViewModel.class,
                ShoppingListItemDetailsViewModel.class,
                RecipeItemDetailsViewModel.class,
                LocationViewModel.class,
                BarcodeScannerViewModel.class,
                BaseViewModel.class,

                //helpers
                DisplayHelper.class,
                AutoCompletionHelper.class,
                StackTraceReporter.class,
                RegularlyRepeatHelper.class,
                DefaultRecipesHelper.class,

                //activities & fragments
                ShoppingListActivity.class,
                ServerIntegrationDebugActivity.class,
                ShoppingListItemDetailsFragment.class,
                RecipeItemDetailsFragment.class,
                ReminderFragment.class,
                SettingFragment.class,
                ShoppingListFragment.class,
                LocationActivity.class,
                LocationFragment.class,
                BarcodeScannerFragment.class,

                RedeemSharingCodeTask.class,
                DefaultViewLauncher.class,
                TutorialViewModel.class
        })
@SuppressWarnings("unused")
public class KwikShopModule {

    private final Activity activity;


    public KwikShopModule(Activity activity) {

        if (activity == null) {
            throw new ArgumentNullException("activity");
        }

        this.activity = activity;
    }


    @Provides
    public Activity provideActivity() {
        return activity;
    }

    @Provides
    public Context provideContext() {
        return activity;
    }


    @Provides
    public ViewLauncher provideViewLauncher(DefaultViewLauncher viewLauncher) {
        return viewLauncher;
    }

    @Provides
    public AutoCompletionHelper provideAutoCompletionHelper(Activity activity) {
        return AutoCompletionHelper.getAutoCompletionHelper(activity.getBaseContext());
    }

}
