package de.cs.fau.mad.quickshop.android.viewmodel.di;

import android.app.Activity;

import dagger.Module;
import dagger.Provides;
import de.cs.fau.mad.quickshop.android.model.ListStorage;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.view.DefaultViewLauncher;
import de.cs.fau.mad.quickshop.android.viewmodel.ListOfShoppingListsViewModel;
import de.cs.fau.mad.quickshop.android.viewmodel.ShoppingListDetailsViewModel;
import de.cs.fau.mad.quickshop.android.viewmodel.common.ViewLauncher;

@Module(injects = {ListOfShoppingListsViewModel.class})
public class QuickshopViewModelModule {

    private final Activity currentActivity;

    public QuickshopViewModelModule(Activity currentActivity) {
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
}
