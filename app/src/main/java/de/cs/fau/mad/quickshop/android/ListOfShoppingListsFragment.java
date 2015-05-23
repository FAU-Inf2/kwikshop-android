package de.cs.fau.mad.quickshop.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;


import java.util.List;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.messages.ShoppingListChangedEvent;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.view.DefaultViewLauncher;
import de.cs.fau.mad.quickshop.android.viewmodel.ListOfShoppingListsViewModel;
import de.greenrobot.event.EventBus;

/**
 * Fragment for list of shopping lists
 */
public class ListOfShoppingListsFragment extends Fragment implements ListOfShoppingListsViewModel.Listener {


    //region Constants

    private static final String ARG_SECTION_NUMBER = "section_number";


    //endregion


    //region Fields

    private ListView m_ListView;
    private ListOfShoppingListsViewModel viewModel;

    //endregion


    //region Construction

    public static ListOfShoppingListsFragment newInstance(int sectionNumber) {

        ListOfShoppingListsFragment fragment = new ListOfShoppingListsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;

    }

    //endregion


    //region Overrides

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity().getSupportFragmentManager(), getActivity().getApplicationContext());

        viewModel = new ListOfShoppingListsViewModel(new DefaultViewLauncher(getActivity()),
                ListStorageFragment.getLocalListStorage());
        viewModel.setListener(this);

        View rootView = inflater.inflate(R.layout.fragment_list_of_shoppinglists, container, false);
        m_ListView = (ListView) rootView.findViewById(android.R.id.list);


        // create adapter for list
        ListOfShoppingListsListRowAdapter listAdapter = new ListOfShoppingListsListRowAdapter(getActivity(), viewModel.getShoppingLists());
        m_ListView.setAdapter(listAdapter);



        // wire up event handlers

        //click on list item
        m_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showToast("Shopping list selected, ID: " + id);

                viewModel.getSelectShoppingListCommand().execute((int) id);

            }
        });

        //long click on list item
        m_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                viewModel.getSelectShoppingListDetailsCommand().execute((int) id);
                return true;
            }
        });

        //click on floating action button (add)
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.getAddShoppingListCommand().execute(null);
            }
        });

        return rootView;
    }




    //endregion





    //region Private Methods

    private void showToast(String text) {

        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(getActivity(), text, duration);
        toast.show();
    }

    //endregion

    @Override
    public void onShoppingListsChanged(List<ShoppingList> newValue) {

        m_ListView.setAdapter(new ListOfShoppingListsListRowAdapter(getActivity(), viewModel.getShoppingLists()));
    }


    @Override
    public void onFinish() {

    }




}
