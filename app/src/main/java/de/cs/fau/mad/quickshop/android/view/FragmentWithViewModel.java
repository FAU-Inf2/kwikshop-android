package de.cs.fau.mad.quickshop.android.view;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.cs.fau.mad.quickshop.android.view.binding.Binding;
import de.cs.fau.mad.quickshop.android.view.binding.ButtonBinding;
import de.cs.fau.mad.quickshop.android.view.binding.ListViewItemCommandBinding;
import de.cs.fau.mad.quickshop.android.viewmodel.common.Command;

/**
 * Base class for fragments that have a view model.
 * Provides helper methods for easier interaction with view models
 */
public abstract class FragmentWithViewModel extends Fragment {

    private List<Binding> bindings = new ArrayList<>();


    protected void bindButton(View button, Command command) {
        ButtonBinding binding = new ButtonBinding(button, command);
        this.bindings.add(binding);
    }

    protected void bindButton(int buttonId, Command command) {
        View button = getRootView().findViewById(buttonId);
        bindButton(button, command);
    }


    protected void bindListViewItem(int listViewId, ListViewItemCommandBinding.ListViewItemCommandType type, Command<Integer> command) {

        ListView listView = (ListView) getRootView().findViewById(listViewId);
        Binding binding = new ListViewItemCommandBinding(type, listView, command);
        this.bindings.add(binding);

    }

    protected abstract View getRootView();

}
