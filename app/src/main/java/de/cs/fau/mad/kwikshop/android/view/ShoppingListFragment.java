package de.cs.fau.mad.kwikshop.android.view;


//import android.app.Fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

import com.melnykov.fab.FloatingActionButton;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.TimedUndoAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cs.fau.mad.kwikshop_android.R;
import de.cs.fau.mad.kwikshop.android.common.Group;
import de.cs.fau.mad.kwikshop.android.common.Item;
import de.cs.fau.mad.kwikshop.android.common.ShoppingList;
import de.cs.fau.mad.kwikshop.android.common.Unit;
import de.cs.fau.mad.kwikshop.android.model.AutoCompletionHelper;
import de.cs.fau.mad.kwikshop.android.model.DefaultDataProvider;
import de.cs.fau.mad.kwikshop.android.model.ListStorage;
import de.cs.fau.mad.kwikshop.android.model.SimpleStorage;
import de.cs.fau.mad.kwikshop.android.model.messages.AutoCompletionHistoryDeletedEvent;
import de.cs.fau.mad.kwikshop.android.model.messages.ItemChangeType;
import de.cs.fau.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.cs.fau.mad.kwikshop.android.model.messages.ShoppingListChangeType;
import de.cs.fau.mad.kwikshop.android.model.messages.ShoppingListChangedEvent;
import de.cs.fau.mad.kwikshop.android.model.ListStorageFragment;
import de.cs.fau.mad.kwikshop.android.model.mock.SpaceTokenizer;
import de.cs.fau.mad.kwikshop.android.util.ItemComparatorHelper;
import de.cs.fau.mad.kwikshop.android.util.StringHelper;
import de.greenrobot.event.EventBus;


public class ShoppingListFragment extends Fragment {

    //region Constants

    private static final String ARG_LISTID = "list_id";


    //endregion


    //region Fields

    private final Object lock = new Object();

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
    private int sortTypeInt = 0;
    private DefaultDataProvider dataProvider;

    @InjectView(R.id.textView_quickAdd)
    MultiAutoCompleteTextView textView_QuickAdd;

    @InjectView(R.id.fab)
    FloatingActionButton floatingActionButton;

    @InjectView(R.id.button_quickAdd)
    View button_QuickAdd;

    private static AutoCompletionHelper autoCompletion;

    private View rootView;
    private boolean hasView = false;

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


    private void disableFloatingButtonWhileSoftKeyboardIsShown() {

        final View activityRootView = BaseActivity.frameLayout;
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                //r will be populated with the coordinates of your view that area still visible.
                Rect r = new Rect();
                activityRootView.getWindowVisibleDisplayFrame(r);

                int screenHeight = activityRootView.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    //hide right away (fade out animation looks weird if the keyboard is showing up at the same moment)
                    floatingActionButton.setVisibility(View.GONE);
                    floatingActionButton.hide();
                } else {
                    //make visible so ew can see the fade in animation
                    floatingActionButton.setVisibility(View.VISIBLE);
                    floatingActionButton.show();
                }
            }
        });

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


        hasView = true;

        // enable go back arrow
        //((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EventBus.getDefault().register(this);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        listStorage = ListStorageFragment.getLocalListStorage();
        unitStorage = ListStorageFragment.getUnitStorage();
        groupStorage = ListStorageFragment.getGroupStorage();
        dataProvider = new DefaultDataProvider(getActivity());

        rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);
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

            sortTypeInt = shoppingList.getSortTypeInt();
            switch (sortTypeInt){
                case 1: sortType = ItemSortType.GROUP;break;
                case 2: sortType = ItemSortType.ALPHABETICALLY; break;
                default: sortType = ItemSortType.MANUAL;
            }

            // Upper list for Items that are not yet bought
            shoppingListAdapter = new ShoppingListAdapter(getActivity(),
                    generateData(shoppingList, false),
                    listStorage,
                    listID,
                    getItemSortType() == ItemSortType.GROUP);

            SimpleSwipeUndoAdapter swipeUndoAdapter = new TimedUndoAdapter(shoppingListAdapter, getActivity(),
                    new OnDismissCallback() {
                        @Override
                        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {

                            for (int position : reverseSortedPositions) {
                                shoppingListAdapter.removeByPosition(position);
                                if (hasView) {
                                    UpdateLists();
                                }
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
                    startActivity(ItemDetailsActivity.getIntent(getActivity(), listID, (int) id));
                }
            });

            justifyListViewHeightBasedOnChildren(shoppingListView);

            // --- //

            // Lower list for Items that are already bought
            shoppingListAdapterBought = new ShoppingListAdapter(getActivity(),
                    generateData(shoppingList, true),
                    listStorage,
                    listID,
                    getItemSortType() == ItemSortType.GROUP);

            shoppingListAdapterBought.setLock(lock); // Needed to synchronize deleteItem

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
                    startActivity(ItemDetailsActivity.getIntent(getActivity(), listID));
                }
            });


            button_QuickAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addItem();
                }
            });

            button_QuickAdd.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //todo: this is just copy paste from addItem() with returning the id

                    final String name = textView_QuickAdd.getText().toString();
                    //adding empty items without a name is not supported
                    long id = -1;
                    if (!StringHelper.isNullOrWhiteSpace(name)) {


                        //reset quick add text
                        textView_QuickAdd.setText("");

                        Item newItem = new Item();
                        newItem.setName(name);
                        newItem.setUnit(unitStorage.getDefaultValue());
                        newItem = parseAmountAndUnit(newItem);
                        newItem.setGroup(groupStorage.getDefaultValue());

                        shoppingList.addItem(newItem);
                        id = newItem.getId();
                        listStorage.saveList(shoppingList);

                        autoCompletion.offerName(newItem.getName());

                        EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.ItemsAdded, shoppingList.getId()));
                        EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.Added, shoppingList.getId(), newItem.getId()));

                        refreshQuickAddAutoCompletion();

                    }
                    //this starts the new activity, if we want it to only open item details
                    // when a text is inserted, move it to the if case above
                    startActivity(ItemDetailsActivity.getIntent(getActivity(), listID, (int) id));
                    return true;
                }
            });

            textView_QuickAdd.setFocusableInTouchMode(true);
            textView_QuickAdd.requestFocus();

            textView_QuickAdd.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    synchronized (lock) {
                        // If the event is a key-down event on the "enter" button
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            addItem();
                            return true;
                        }
                        return false;
                    }
                }
            });
            textView_QuickAdd.setTokenizer(new SpaceTokenizer());

            //wire up auto-complete for product name
            if (autoCompletion == null)
                autoCompletion = AutoCompletionHelper.getAutoCompletionHelper(getActivity().getBaseContext());

            refreshQuickAddAutoCompletion();

        }

        disableFloatingButtonWhileSoftKeyboardIsShown();

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
        hasView = false;
    }

    public Item parseAmountAndUnit(Item item) {

        String input = item.getName();
        String output = "";
        String amount = "";
        String thisCanBeUnitOrName = "";
        boolean lastCharWasANumber = false;
        boolean charWasReadAfterAmount = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            //only parses the first number found to amount
            if (c > 47 && c < 58 && (lastCharWasANumber == true || amount == "")) {
                amount = amount + c;
                lastCharWasANumber = true;
            } else if (lastCharWasANumber && c == ' ') {
                //ignore all white spaces between the amount and the next char
            } else if (lastCharWasANumber || charWasReadAfterAmount && c != ' ') {
                //String from amount to next whitespace, this should be unit or name
                thisCanBeUnitOrName = thisCanBeUnitOrName + c;
                lastCharWasANumber = false;
                charWasReadAfterAmount = true;
            } else if (charWasReadAfterAmount && c == ' ') {
                //whitespace after possible unit
                charWasReadAfterAmount = false;
            } else {
                output = output + c;
                lastCharWasANumber = false;
            }
        }


        boolean unitMatchFound = false;
        DisplayHelper displayHelper = new DisplayHelper(getActivity());
        List<Unit> unitsList = ListStorageFragment.getUnitStorage().getItems();
        for (Unit unit : unitsList) {
            if (displayHelper.getDisplayName(unit).equalsIgnoreCase(thisCanBeUnitOrName) ||
                    displayHelper.getShortDisplayName(unit).equalsIgnoreCase(thisCanBeUnitOrName)) {
                item.setUnit(unit);
                unitMatchFound = true;
                break;
            }
        }


        if (unitMatchFound == false && thisCanBeUnitOrName != "") {
            //if no unit was found complete string has to be restored
            if (output != "") {
                output = thisCanBeUnitOrName + " " + output;
            } else {
                output = thisCanBeUnitOrName;
            }
        }

        if (!StringHelper.isNullOrWhiteSpace(output)) {
            if (amount != "") item.setAmount(Integer.parseInt(amount));
            item.setName(output);
        }
        return item;


    }


    public void deleteItem(final Item deleteItem) {

        synchronized (lock) {

            AsyncTask task = new AsyncTask() {

                @Override
                protected Object doInBackground(Object[] params) {

                    shoppingList.removeItem(deleteItem.getId());

                    listStorage.saveList(shoppingList);

                    EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.ItemsRemoved, shoppingList.getId()));
                    EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.Deleted, shoppingList.getId(), deleteItem.getId()));

                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    UpdateLists();
                }

            };

            task.execute();


        }

    }


    public void addItem() {

        synchronized (lock) {
            final String name = textView_QuickAdd.getText().toString();

            //adding empty items without a name is not supported
            if (!StringHelper.isNullOrWhiteSpace(name)) {


                //reset quick add text
                textView_QuickAdd.setText("");

                AsyncTask task = new AsyncTask() {


                    @Override
                    protected Object doInBackground(Object[] params) {
                        Item newItem = new Item();
                        newItem.setName(name);
                        newItem.setUnit(unitStorage.getDefaultValue());
                        newItem = parseAmountAndUnit(newItem);
                        newItem.setGroup(groupStorage.getDefaultValue());

                        shoppingList.addItem(newItem);

                        listStorage.saveList(shoppingList);

                        autoCompletion.offerName(newItem.getName());

                        EventBus.getDefault().post(new ShoppingListChangedEvent(ShoppingListChangeType.ItemsAdded, shoppingList.getId()));
                        EventBus.getDefault().post(new ItemChangedEvent(ItemChangeType.Added, shoppingList.getId(), newItem.getId()));

                        return null;

                    }
                };

                task.execute();


                refreshQuickAddAutoCompletion();

            }
        }

    }

    /**
     * call this method to initialize or refresh the data used by QuickAdd's auto completion
     */
    private void refreshQuickAddAutoCompletion() {
        textView_QuickAdd.setAdapter(autoCompletion.getNameAdapter(getActivity()));
    }


    //endregion


    //region Event Handlers

    public void onEventMainThread(ShoppingListChangedEvent event) {
        if (event.getListId() == this.listID && this.shoppingListAdapter != null) {
            UpdateLists();
        }
    }

    public void onEventMainThread(ItemChangedEvent event) {
        if (event.getShoppingListId() == this.listID && this.shoppingListAdapter != null) {
            UpdateLists();
        }
    }

    public void onEventMainThread(ItemSortType sortType) {
        this.sortType = sortType;
        //only sort the list if a automatic sorting is chosen
        if (sortType == ItemSortType.MANUAL) {
            shoppingList.setSortTypeInt(0);
            shoppingListAdapter.setGroupItems(false);
            shoppingListAdapterBought.setGroupItems(false);
        } else {
            if(sortType == ItemSortType.GROUP) shoppingList.setSortTypeInt(1);
            if(sortType == ItemSortType.ALPHABETICALLY) shoppingList.setSortTypeInt(2);
            listStorage.saveList(shoppingList);
            UpdateLists();
        }
    }

    public void onEvent(AutoCompletionHistoryDeletedEvent event) {
        if (autoCompletion != null) {
            refreshQuickAddAutoCompletion();
        }
    }

    //endregion


    //region Private Methods
    private Item getItemFromShoppingListAdapter(AdapterView adapter, int position) {
        return (shoppingList.getItem((int) adapter.getItemIdAtPosition(position)));
    }

    private void UpdateLists() {
        shoppingList = listStorage.loadList(listID); // Reload the ShoppingList - needed if Items were edited / removed

        shoppingListAdapter.setGroupItems(getItemSortType() == ItemSortType.GROUP);
        shoppingListAdapter.clear();
        shoppingListAdapter.addAll(generateData(shoppingList, false));
        shoppingListAdapter.updateOrderOfList();
        shoppingListAdapter.notifyDataSetChanged();
        justifyListViewHeightBasedOnChildren(shoppingListView);

        shoppingListAdapterBought.setGroupItems(getItemSortType() == ItemSortType.GROUP);
        shoppingListAdapterBought.clear();
        shoppingListAdapterBought.addAll(generateData(shoppingList, true));
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
