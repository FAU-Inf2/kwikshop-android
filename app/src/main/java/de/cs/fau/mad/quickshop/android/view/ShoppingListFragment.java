package de.cs.fau.mad.quickshop.android.view;


//import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.TimedUndoAdapter;

import java.util.ArrayList;

import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.common.Item;
import de.cs.fau.mad.quickshop.android.common.ShoppingList;
import de.cs.fau.mad.quickshop.android.model.messages.ItemChangedEvent;
import de.cs.fau.mad.quickshop.android.model.messages.ShoppingListChangedEvent;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.greenrobot.event.EventBus;


public class ShoppingListFragment extends Fragment {

    //region Constants

    private static final String ARG_LISTID = "list_id";

    //endregion


    //region Fields

    private ListStorageFragment m_ListStorageFragment;
    private ShoppingListAdapter m_ShoppingListAdapter;
    private ShoppingListAdapter m_ShoppingListAdapterBought;
    private DynamicListView shoppingListView;
    private DynamicListView shoppingListViewBought;

    private int listID;

    //endregion


    //region Construction

    public static ShoppingListFragment newInstance(int listID) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LISTID, listID);
        fragment.setArguments(args);
        return fragment;
    }

    //endregion


    //region Overrides

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listID = getArguments().getInt(ARG_LISTID);
        }
    }

    public void justifyListViewHeightBasedOnChildren (DynamicListView listView) {
        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EventBus.getDefault().register(this);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());
        m_ListStorageFragment = ListStorageFragment.getListStorageFragment();

        View rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);
        shoppingListView = (DynamicListView) rootView.findViewById(R.id.list_shoppingList);
        shoppingListViewBought = (DynamicListView) rootView.findViewById(R.id.list_shoppingListBought);

        ShoppingList shoppingList = null;
        try {
            shoppingList = m_ListStorageFragment.getLocalListStorage().loadList(listID);
        } catch (IllegalArgumentException ex) { //TODO: we should probably introduce our own exception types
            showToast(ex.getMessage());
            Intent intent = new Intent(getActivity(), ShoppingListActivity.class);
            startActivity(intent);
        }

        if (shoppingList != null) {
            // Upper list for Items that are not yet bought
            m_ShoppingListAdapter = new ShoppingListAdapter(getActivity(), R.id.list_shoppingList,
                    generateData(shoppingList, false),
                    shoppingList);

            SimpleSwipeUndoAdapter swipeUndoAdapter = new TimedUndoAdapter(m_ShoppingListAdapter, getActivity(),
                    new OnDismissCallback() {
                        @Override
                        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                            for (int position : reverseSortedPositions) {
                                m_ShoppingListAdapter.removeByPosition(position);
                                UpdateLists();
                            }
                        }
                    }
            );
            swipeUndoAdapter.setAbsListView(shoppingListView);
            shoppingListView.setAdapter(swipeUndoAdapter);
            shoppingListView.enableSimpleSwipeUndo();
            justifyListViewHeightBasedOnChildren(shoppingListView);

            // --- //

            // Lower list for Items that are already bought
            m_ShoppingListAdapterBought = new ShoppingListAdapter(getActivity(), R.id.list_shoppingListBought,
                    generateData(shoppingList, true),
                    shoppingList);

            shoppingListViewBought.enableSwipeToDismiss(
                    new OnDismissCallback() {
                        @Override
                        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                            for (int position : reverseSortedPositions) {
                                m_ShoppingListAdapterBought.removeByPosition(position);
                                UpdateLists();
                            }
                        }
                    }
            );

            shoppingListViewBought.setAdapter(m_ShoppingListAdapterBought);
            justifyListViewHeightBasedOnChildren(shoppingListViewBought);

            // OnClickListener to open the item details view
            /*shoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Open item details view
                    Toast.makeText(getActivity(), "ID: " + id + " - PID: " + parent.getItemIdAtPosition(position), Toast.LENGTH_LONG).show();
                    fm.beginTransaction().replace(BaseActivity.frameLayout.getId() , ItemDetailsFragment.newInstance(listID, (int) id))
                            .addToBackStack(null).commit();
                }
            });*/

            //Setting spinner adapter to sort by button
            Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.sort_by_array, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);
        }

        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }


    //endregion


    //region Event Handlers

    public void onEvent(ShoppingListChangedEvent event) {
        if (event.getListId() == this.listID && this.m_ShoppingListAdapter != null) {
            UpdateLists();
        }
    }

    public void onEvent(ItemChangedEvent event) {
        if (event.getShoppingListId() == this.listID && this.m_ShoppingListAdapter != null) {
            UpdateLists();
        }
    }

    //endregion


    //region Private Methods

    private void UpdateLists() {
        m_ShoppingListAdapter.clear();
        m_ShoppingListAdapter.addAll(generateData(m_ListStorageFragment.getLocalListStorage().loadList(listID), false));
        m_ShoppingListAdapter.notifyDataSetChanged();
        justifyListViewHeightBasedOnChildren(shoppingListView);

        m_ShoppingListAdapterBought.clear();
        m_ShoppingListAdapterBought.addAll(generateData(m_ListStorageFragment.getLocalListStorage().loadList(listID), true));
        m_ShoppingListAdapterBought.notifyDataSetChanged();
        justifyListViewHeightBasedOnChildren(shoppingListViewBought);
    }

    private ArrayList<Integer> generateData(ShoppingList shoppingList, boolean isBought) {
        ArrayList<Integer> items = new ArrayList<>();
        for (Item item : shoppingList.getItems()) {
            if(item.isBought() == isBought)
                items.add(item.getId());
        }
        return items;
    }

    //region Private Methods

    private void showToast(String text) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getActivity(), text, duration);
        toast.show();
    }

    //endregion

}
