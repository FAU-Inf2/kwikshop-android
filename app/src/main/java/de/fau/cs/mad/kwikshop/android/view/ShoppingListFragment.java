package de.fau.cs.mad.kwikshop.android.view;


//import android.app.Fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ListAdapter;
import android.support.v4.app.Fragment;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.melnykov.fab.FloatingActionButton;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.TimedUndoAdapter;


import butterknife.ButterKnife;
import butterknife.*;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.common.*;
import de.fau.cs.mad.kwikshop.android.model.*;
import de.fau.cs.mad.kwikshop.android.model.messages.*;
import de.fau.cs.mad.kwikshop.android.model.mock.SpaceTokenizer;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.view.binding.ButtonBinding;
import de.fau.cs.mad.kwikshop.android.view.binding.ListViewItemCommandBinding;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.*;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.viewmodel.di.KwikShopViewModelModule;
import de.greenrobot.event.EventBus;


public class ShoppingListFragment
        extends Fragment
        implements ShoppingListViewModel.Listener, ObservableArrayList.Listener<Item> {


    private static final String ARG_LISTID = "list_id";

    public Menu overflow_menu;

    private int listID = -1;

    private AutoCompletionHelper autoCompletion;

    private ShoppingListViewModel viewModel;
    private boolean updatingViewModel;

    @InjectView(R.id.list_shoppingList)
    DynamicListView shoppingListView;

    @InjectView(R.id.list_shoppingListBought)
    DynamicListView shoppingListViewBought;

    @InjectView(R.id.textView_quickAdd)
    MultiAutoCompleteTextView textView_QuickAdd;

    @InjectView(R.id.fab)
    FloatingActionButton floatingActionButton;

    @InjectView(R.id.button_quickAdd)
    View button_QuickAdd;

    @InjectView(R.id.shoppinglist_scrollview)
    ScrollView scrollView;

    @InjectView(R.id.quickAdd)
    RelativeLayout quickAddLayout;



    public static ShoppingListFragment newInstance(int listID) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LISTID, listID);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listID = getArguments().getInt(ARG_LISTID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        EventBus.getDefault().register(this);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);
        ButterKnife.inject(this, rootView);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopViewModelModule(getActivity()));
        DisplayHelper displayHelper = objectGraph.get(DisplayHelper.class);
        viewModel = objectGraph.get(ShoppingListViewModel.class);
        autoCompletion = objectGraph.get(AutoCompletionHelper.class);
        viewModel.initialize(this.listID);


        getActivity().setTitle(viewModel.getName());

        viewModel.addListener(this);
        viewModel.getItems().addListener(this);
        viewModel.getBoughtItems().addListener(this);

        ShoppingListAdapter shoppingListAdapter = new ShoppingListAdapter(getActivity(), viewModel,
                viewModel.getItems(), displayHelper);
        shoppingListView.setAdapter(shoppingListAdapter);

        new ListViewItemCommandBinding(ListViewItemCommandBinding.ListViewItemCommandType.Click,
                shoppingListView,
                viewModel.getSelectItemCommand());

        SimpleSwipeUndoAdapter swipeUndoAdapter = new TimedUndoAdapter(shoppingListAdapter, getActivity(),
                new OnDismissCallback() {
                    @Override
                    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                        Command<Integer> command = viewModel.getToggleIsBoughtCommand();
                        for (int position : reverseSortedPositions) {

                            if (command.getCanExecute()) {
                                try {
                                    Item item = viewModel.getItems().get(position);
                                    command.execute(item.getId());
                                } catch (IndexOutOfBoundsException ex) {
                                    //nothing to do
                                }
                            }
                        }
                    }
                });

        swipeUndoAdapter.setAbsListView(shoppingListView);
        shoppingListView.setAdapter(swipeUndoAdapter);
        shoppingListView.enableSimpleSwipeUndo();
        shoppingListView.enableDragAndDrop();

        shoppingListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                                   final int position, final long id) {
                        //disable events on observable list during drag&drop to prevent lag
                        //IMPORTANT: Make sure to reenable events afterwards
                        viewModel.getItems().disableEvents();
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        shoppingListView.startDragging(position);
                        //sets value of the Spinner to the first entry, in this case Manual
                        return true;
                    }
                }
        );

        shoppingListView.setOnItemMovedListener(new OnItemMovedListener() {
            @Override
            public void onItemMoved(int i, int i1) {
                viewModel.itemsSwapped(i, i1);
                //IMPORTANT: reenable events of the observable list after drag and drop has finished
                viewModel.getItems().enableEvents();
            }
        });

        justifyListViewHeightBasedOnChildren(shoppingListView);


        shoppingListViewBought.enableSwipeToDismiss(
                new OnDismissCallback() {
                    @Override
                    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                        Command<Integer> command = viewModel.getToggleIsBoughtCommand();
                        for (int position : reverseSortedPositions) {
                            if (command.getCanExecute()) {
                                Item item = viewModel.getBoughtItems().get(position);
                                command.execute(item.getId());
                            }
                        }
                    }
                });


        shoppingListViewBought.setAdapter(new ShoppingListAdapter(getActivity(), viewModel, viewModel.getBoughtItems(), displayHelper));
        shoppingListViewBought.setOnItemMovedListener(new OnItemMovedListener() {
            @Override
            public void onItemMoved(int i, int i1) {
                viewModel.boughtItemsSwapped(i, i1);
            }
        });
        shoppingListViewBought.enableDragAndDrop();

        justifyListViewHeightBasedOnChildren(shoppingListViewBought);


        new ButtonBinding(floatingActionButton, viewModel.getAddItemCommand(), false);
        new ButtonBinding(button_QuickAdd, viewModel.getQuickAddCommand());

        //TODO: quick add long click

        textView_QuickAdd.setFocusableInTouchMode(true);
        textView_QuickAdd.requestFocus();

        textView_QuickAdd.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                synchronized (viewModel) {
                    // If the event is a key-down event on the "enter" button
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        viewModel.getQuickAddCommand().execute(null);
                        return true;
                    }
                    return false;
                }
            }
        });
        textView_QuickAdd.setTokenizer(new SpaceTokenizer());

        RegularlyRepeatHelper.getRegularlyRepeatHelper(getActivity()); // to make sure it is initialized when needed in ShoppingListViewModel

        refreshQuickAddAutoCompletion();

        disableFloatingButtonWhileSoftKeyboardIsShown();



        // shopping mode is on
        if(SharedPreferencesHelper.loadBoolean(ShoppingListActivity.SHOPPING_MODE_SETTING, false, getActivity())){
            BaseActivity.overflow_menu.findItem(R.id.overflow_menu).setVisible(false);
            floatingActionButton.setVisibility(View.GONE);
            floatingActionButton.hide();
             // remove quick add view
            ((ViewManager) quickAddLayout.getParent()).removeView(quickAddLayout);
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
        viewModel.onDestroyView();
        EventBus.getDefault().unregister(this);
    }


    /**
     * call this method to initialize or refresh the data used by QuickAdd's auto completion
     */
    private void refreshQuickAddAutoCompletion() {
        textView_QuickAdd.setAdapter(autoCompletion.getNameAdapter(getActivity()));
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
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    @SuppressWarnings("unused")
    public void onEvent(AutoCompletionHistoryDeletedEvent event) {
        if (autoCompletion != null) {
            refreshQuickAddAutoCompletion();
        }
    }


    @OnTextChanged(R.id.textView_quickAdd)
    @SuppressWarnings("unused")
    public void textView_ShoppingListName_OnTextChanged(CharSequence s) {

        synchronized (viewModel) {
            //send updated value for shopping list name to the view model
            updatingViewModel = true;
            viewModel.setQuickAddText(s != null ? s.toString() : "");
            updatingViewModel = false;
        }
    }


    @Override
    public void onQuickAddTextChanged() {
        if (!updatingViewModel) {
            this.textView_QuickAdd.setText(viewModel.getQuickAddText());
        }
    }

    @Override
    public void onItemSortTypeChanged() {

    }

    @Override
    public void onNameChanged(String value) {
        // set title for actionbar
        getActivity().setTitle(viewModel.getName());
    }

    @Override
    public void onFinish() {

    }


    @Override
    public void onItemAdded(Item newItem) {

        //TODO: It might make sense to move autocompletion handling to the view model
        //IMPORTANT
        if(autoCompletion != null) {
            refreshQuickAddAutoCompletion();
        }

        justifyListViewHeightBasedOnChildren(shoppingListView);
        justifyListViewHeightBasedOnChildren(shoppingListViewBought);
    }

    @Override
    public void onItemRemoved(Item removedItem) {
        justifyListViewHeightBasedOnChildren(shoppingListView);
        justifyListViewHeightBasedOnChildren(shoppingListViewBought);
    }

    @Override
    public void onItemModified(Item modifiedItem) {
        justifyListViewHeightBasedOnChildren(shoppingListView);
        justifyListViewHeightBasedOnChildren(shoppingListViewBought);
    }



}
