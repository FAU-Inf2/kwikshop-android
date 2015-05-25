package de.cs.fau.mad.quickshop.android.view;

import android.support.v4.app.Fragment;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;

/**
 * Base class for fragments that have a view model.
 * Provides helper methods for easier interaction with view models
 */
public abstract class FragmentWithViewModel extends Fragment {

    private List<ButtonBinding> buttonBindings = new ArrayList<>();


    protected void bindButton(int buttonId, Command command) {

        View button = getRootView().findViewById(buttonId);
        ButtonBinding binding = new ButtonBinding(button, command);
        buttonBindings.add(binding);

    }


    protected abstract View getRootView();

}
