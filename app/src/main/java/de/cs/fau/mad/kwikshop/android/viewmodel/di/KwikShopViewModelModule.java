package de.cs.fau.mad.kwikshop.android.viewmodel.di;

import android.app.Activity;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import de.cs.fau.mad.kwikshop.android.model.ListStorage;
import de.cs.fau.mad.kwikshop.android.model.ListStorageFragment;
import de.cs.fau.mad.kwikshop.android.view.DefaultResourceProvider;
import de.cs.fau.mad.kwikshop.android.view.DefaultViewLauncher;
import de.cs.fau.mad.kwikshop.android.viewmodel.ListOfShoppingListsViewModel;
import de.cs.fau.mad.kwikshop.android.viewmodel.ShoppingListDetailsViewModel;
import de.cs.fau.mad.kwikshop.android.viewmodel.common.ResourceProvider;
import de.cs.fau.mad.kwikshop.android.viewmodel.common.ViewLauncher;

@Module(injects = {ListOfShoppingListsViewModel.class, ShoppingListDetailsViewModel.class})
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
    public Context provideContext() {
        return currentActivity;
    }

    @Provides
    public ResourceProvider provideResourceProvider(Context context) {
        return new DefaultResourceProvider(context);
    }
}
