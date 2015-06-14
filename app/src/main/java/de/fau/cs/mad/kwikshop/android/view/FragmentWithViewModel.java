package de.fau.cs.mad.kwikshop.android.view;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.kwikshop.android.view.binding.Binding;
import de.fau.cs.mad.kwikshop.android.view.binding.ButtonBinding;
import de.fau.cs.mad.kwikshop.android.view.binding.ListViewItemCommandBinding;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.Command;

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


    protected void bindListViewItem(ListView listView, ListViewItemCommandBinding.ListViewItemCommandType type, Command<Integer> command) {

        Binding binding = new ListViewItemCommandBinding(type, listView, command);
        this.bindings.add(binding);

    }


}
