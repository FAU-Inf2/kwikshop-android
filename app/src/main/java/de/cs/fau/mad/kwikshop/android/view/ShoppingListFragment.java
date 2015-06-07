package de.cs.fau.mad.kwikshop.android.view;


//import android.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.TimedUndoAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cs.fau.mad.kwikshop_android.R;
import de.cs.fau.mad.kwikshop.android.common.AutoCompletionData;
import de.cs.fau.mad.kwikshop.android.common.Group;
import de.cs.fau.mad.kwikshop.android.common.Item;
import de.cs.fau.mad.kwikshop.android.common.ShoppingList;
import de.cs.fau.mad.kwikshop.android.common.Unit;
import de.cs.fau.mad.kwikshop.android.model.DatabaseHelper;
import de.cs.fau.mad.kwikshop.android.model.ListStorage;
import de.cs.fau.mad.kwikshop.android.model.SimpleStorage;
import de.cs.fau.mad.kwikshop.android.model.messages.ItemChangeType;
import de.cs.fau.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.cs.fau.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.cs.fau.mad.kwikshop.android.model.ListStorageFragment;
import de.cs.fau.mad.kwikshop.android.util.ItemComparatorHelper;
import de.cs.fau.mad.kwikshop.android.util.StringHelper;
import de.greenrobot.event.EventBus;


public class ShoppingListFragment extends Fragment {

    //region Constants

    private static final String ARG_LISTID = "list_id";

    //endregion


    //region Fields


    private ShoppingListAdapter shoppingListAdapter;
    private ShoppingListAdapter shoppingListAdapterBought;

    @InjectView(R.id.list_shoppingList)
    DynamicListView shoppingListView;

    @InjectView(R.id.list_shoppingListBought)
    DynamicListView shoppingListViewBought;

    private ListStorage listStorage;
    private SimpleStorage<Unit> unitStorage;
    private SimpleStorage<Group> groupStorage;
    private ShoppingList shoppingList = null;
    private int listID;
    private ItemSortType sortType = ItemSortType.MANUAL;

    @InjectView(R.id.textView_quickAdd)
    EditText textView_QuickAdd;

    @InjectView(R.id.fab)
    View floatingActionButton;

    @InjectView(R.id.button_quickAdd)
    View button_QuickAdd;

    private static SimpleStorage<AutoCompletionData> autoCompletionStorage;
    private static DatabaseHelper databaseHelper;
    private static ArrayList<String> autocompleteSuggestions = null;

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

    public void justifyListViewHeightBasedOnChildren(DynamicListView listView) {
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

        // enable go back arrow
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EventBus.getDefault().register(this);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        listStorage = ListStorageFragment.getLocalListStorage();
        unitStorage = ListStorageFragment.getUnitStorage();
        groupStorage = ListStorageFragment.getGroupStorage();


        View rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);
        ButterKnife.inject(this, rootView);



        try {
            shoppingList = listStorage.loadList(listID);
        } catch (IllegalArgumentException ex) { //TODO: we should probably introduce our own exception types
            showToast(ex.getMessage());
            Intent intent = new Intent(getActivity(), ShoppingListActivity.class);
            startActivity(intent);
        }

        if (shoppingList != null) {

            // set title for actionbar
            getActivity().setTitle(shoppingList.getName());


            // Upper list for Items that are not yet bought
            shoppingListAdapter = new ShoppingListAdapter(getActivity(),
                    generateData(shoppingList, false),
                    shoppingList,
                    getItemSortType() == ItemSortType.GROUP);

            SimpleSwipeUndoAdapter swipeUndoAdapter = new TimedUndoAdapter(shoppingListAdapter, getActivity(),
                    new OnDismissCallback() {
                        @Override
                        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                            for (int position : reverseSortedPositions) {
                                shoppingListAdapter.removeByPosition(position);
                                UpdateLists();
                            }
                        }
                    }
            );


            swipeUndoAdapter.setAbsListView(shoppingListView);
            shoppingListView.setAdapter(swipeUndoAdapter);
            shoppingListView.enableSimpleSwipeUndo();
            shoppingListView.enableDragAndDrop();
            shoppingListView.setOnItemLongClickListener(
                    new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                                       final int position, final long id) {
                            ScrollView sview = (ScrollView) getView().findViewById(R.id.shoppinglist_scrollview);
                            sview.requestDisallowInterceptTouchEvent(true);
                            shoppingListView.startDragging(position);
                            //sets value of the Spinner to the first entry, in this case Manual
                            return true;
                        }
                    }
            );

            shoppingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Open item details view
                    getFragmentManager().beginTransaction().replace(BaseActivity.frameLayout.getId(), ItemDetailsFragment.newInstance(listID, (int) id))
                            .addToBackStack(null).commit();
                    //return true;
                }
            });

            justifyListViewHeightBasedOnChildren(shoppingListView);

            // --- //

            // Lower list for Items that are already bought
            shoppingListAdapterBought = new ShoppingListAdapter(getActivity(),
                    generateData(shoppingList, true),
                    shoppingList,
                    getItemSortType() == ItemSortType.GROUP);

            shoppingListViewBought.enableSwipeToDismiss(
                    new OnDismissCallback() {
                        @Override
                        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                            for (int position : reverseSortedPositions) {
                                shoppingListAdapterBought.removeByPosition(position);
                                UpdateLists();
                            }
                        }
                    }
            );


            shoppingListViewBought.setAdapter(shoppingListAdapterBought);
            shoppingListViewBought.enableDragAndDrop();
            justifyListViewHeightBasedOnChildren(shoppingListViewBought);


            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment newItemFragment = ItemDetailsFragment.newInstance(listID);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(BaseActivity.frameLayout.getId(), newItemFragment)
                            .addToBackStack(null).commit();
                }
            });


            button_QuickAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addItem();

                }
            });


            textView_QuickAdd.setFocusableInTouchMode(true);
            textView_QuickAdd.requestFocus();

            textView_QuickAdd.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // If the event is a key-down event on the "enter" button
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        addItem();
                        return true;
                    }
                    return false;
                }
            });

            //wire up auto-complete for product name
            if(databaseHelper == null) {
                Context context = getActivity().getBaseContext();
                databaseHelper = new DatabaseHelper(context);
            }
            if (autoCompletionStorage == null)
                try {
                    //create local autocompletion storage
                    autoCompletionStorage = new SimpleStorage<>(databaseHelper.getAutoCompletionDao());
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            List<AutoCompletionData> autoCompletionData = autoCompletionStorage.getItems();
            autocompleteSuggestions = new ArrayList<String>(autoCompletionData.size());
            for (AutoCompletionData data : autoCompletionData) {
                autocompleteSuggestions.add(data.getText());
            }

        }

        return rootView;
    }

    @Override
    public void onResume() {

        super.onResume();

        Window window = getActivity().getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }
    
    @Override
    public void onDestroyView() {

        super.onDestroyView();
        EventBus.getDefault().unregister(this);

    }


    public void addItem() {

        //adding empty items without a name is not supported
        if (!StringHelper.isNullOrWhiteSpace(textView_QuickAdd.getText())) {

            Item newItem = new Item();
            newItem.setName(textView_QuickAdd.getText().toString());
            newItem.setUnit(unitStorage.getDefaultValue());
            newItem.setGroup(groupStorage.getDefaultValue());

            shoppingList.addItem(newItem);

            listStorage.saveList(shoppingList);

            if (!autocompleteSuggestions.contains(textView_QuickAdd.getText().toString())) {
                autocompleteSuggestions.add(textView_QuickAdd.getText().toString());
                autoCompletionStorage.addItem(new AutoCompletionData(textView_QuickAdd.getText().toString()));
            }

            EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.Added, shoppingList.getId(), newItem.getId()));

            //reset quick add text
            textView_QuickAdd.setText("");
        }


    }


    //endregion


    //region Event Handlers

    public void onEvent(ShoppingListChangedEvent event) {
        if (event.getListId() == this.listID && this.shoppingListAdapter != null) {
            UpdateLists();
        }
    }

    public void onEvent(ItemChangedEvent event) {
        if (event.getShoppingListId() == this.listID && this.shoppingListAdapter != null) {
            UpdateLists();
        }
    }

    public void onEvent(ItemSortType sortType) {
        this.sortType = sortType;
        //only sort the list if a automatic sorting is chosen
        if (sortType == ItemSortType.MANUAL) {
            shoppingListAdapter.setGroupItems(false);
            shoppingListAdapterBought.setGroupItems(false);
        } else {
            UpdateLists();
        }
    }

    //endregion


    //region Private Methods

    private void UpdateLists() {
        shoppingListAdapter.setGroupItems(getItemSortType() == ItemSortType.GROUP);
        shoppingListAdapter.clear();
        shoppingListAdapter.addAll(generateData(listStorage.loadList(listID), false));
        shoppingListAdapter.updateOrderOfList();
        shoppingListAdapter.notifyDataSetChanged();
        justifyListViewHeightBasedOnChildren(shoppingListView);

        shoppingListAdapterBought.setGroupItems(getItemSortType() == ItemSortType.GROUP);
        shoppingListAdapterBought.clear();
        shoppingListAdapterBought.addAll(generateData(listStorage.loadList(listID), true));
        shoppingListAdapterBought.updateOrderOfList();
        shoppingListAdapterBought.notifyDataSetChanged();
        justifyListViewHeightBasedOnChildren(shoppingListViewBought);
    }

    private ArrayList<Integer> generateData(ShoppingList shoppingList, boolean isBought) {
        ArrayList<Integer> items = new ArrayList<>();
        int initOrder = 1;

        for (Item item : shoppingList.getItems()) {
            if (item.isBought() == isBought) {
                items.add(item.getId());

                // Set order if it is not yet set
                if (item.getOrder() == -1)
                    item.setOrder(initOrder);
            }
            initOrder++;
        }
        Collections.sort(items, new ItemComparatorHelper(shoppingList, new DisplayHelper(getActivity()), getItemSortType()));
        return items;
    }

    private void showToast(String text) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getActivity(), text, duration);
        toast.show();
    }

    private ItemSortType getItemSortType() {
        return this.sortType;
    }

    //endregion

}
