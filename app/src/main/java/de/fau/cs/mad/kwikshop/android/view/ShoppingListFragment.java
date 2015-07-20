package de.fau.cs.mad.kwikshop.android.view;


//import android.app.Fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.support.v4.app.Fragment;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.TimedUndoAdapter;


import java.util.List;

import butterknife.ButterKnife;
import butterknife.*;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.*;
import de.fau.cs.mad.kwikshop.android.model.messages.*;
import de.fau.cs.mad.kwikshop.android.model.mock.SpaceTokenizer;
import de.fau.cs.mad.kwikshop.android.util.SharedPreferencesHelper;
import de.fau.cs.mad.kwikshop.android.view.binding.ButtonBinding;
import de.fau.cs.mad.kwikshop.android.view.binding.ListViewItemCommandBinding;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.*;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.common.Item;
import de.greenrobot.event.EventBus;


public class ShoppingListFragment
        extends Fragment
        implements ShoppingListViewModel.Listener, ObservableArrayList.Listener<Item> {


    private static final String ARG_LISTID = "list_id";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;


    public Menu overflow_menu;

    private int listID = -1;

    private AutoCompletionHelper autoCompletion;

    private Integer sizeBoughtItems;
    private ShoppingListViewModel viewModel;
    private boolean updatingViewModel;

    @InjectView(R.id.list_shoppingList)
    DynamicListView shoppingListView;

    @InjectView(R.id.textView_quickAdd)
    MultiAutoCompleteTextView textView_QuickAdd;

    @InjectView(R.id.fab)
    FloatingActionButton floatingActionButton;

    @InjectView(R.id.button_quickAdd)
    View button_QuickAdd;

    @InjectView(R.id.quickAdd)
    RelativeLayout quickAddLayout;

    @InjectView(R.id.micButton)
    ImageButton micButton;


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
        setRetainInstance(true);
        if (getArguments() != null) {
            listID = getArguments().getInt(ARG_LISTID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        EventBus.getDefault().register(this);

        final String googleBrowserApiKey = getResources().getString(R.string.google_browser_api_key);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);
        ButterKnife.inject(this, rootView);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        DisplayHelper displayHelper = objectGraph.get(DisplayHelper.class);
        viewModel = objectGraph.get(ShoppingListViewModel.class);
        autoCompletion = objectGraph.get(AutoCompletionHelper.class);
        viewModel.initialize(this.listID);


        getActivity().setTitle(viewModel.getName());

        viewModel.addListener(this);
        viewModel.getItems().addListener(this);
        //viewModel.getBoughtItems().addListener(this);
        //sizeBoughtItems = viewModel.getBoughtItems().size();
        //cartCounter.setText(String.valueOf(sizeBoughtItems));

        //ObservableArrayList list = (ObservableArrayList)viewModel.getItems().clone();
        //list.addAll(viewModel.getBoughtItems());

        final ShoppingListAdapter shoppingListAdapter = new ShoppingListAdapter(getActivity(), viewModel,
                viewModel.getItems(), displayHelper);
        shoppingListView.setAdapter(shoppingListAdapter);

        new ListViewItemCommandBinding(ListViewItemCommandBinding.ListViewItemCommandType.Click,
                shoppingListView,
                viewModel.getSelectItemCommand());

        /*TimedUndoAdapter swipeUndoAdapter = new TimedUndoAdapter(shoppingListAdapter, getActivity(),
                new OnDismissCallback() {
                    @Override
                    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                        Command<Integer> command = viewModel.getToggleIsBoughtCommand();
                        for (int position : reverseSortedPositions) {

                            if (command.getCanExecute()) {
                                try {
                                    //Item item = viewModel.getItems().get(position);
                                    Item item = shoppingListAdapter.getItem(position);
                                    command.execute(item.getId());

                                } catch (IndexOutOfBoundsException ex) {
                                    //nothing to do
                                }
                            }
                        }
                        viewModel.moveBoughtItemsToEnd();
                    }
                });

        swipeUndoAdapter.setAbsListView(shoppingListView);
        swipeUndoAdapter.setTimeoutMs(1000);
        shoppingListView.setAdapter(swipeUndoAdapter);
        shoppingListView.enableSimpleSwipeUndo();*/

        shoppingListView.enableSwipeToDismiss(
                new OnDismissCallback() {
                    @Override
                    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                        Command<Integer> command = viewModel.getToggleIsBoughtCommand();
                        for (int position : reverseSortedPositions) {
                            if (command.getCanExecute()) {
                                try {
                                    //Item item = viewModel.getItems().get(position);
                                    Item item = shoppingListAdapter.getItem(position);
                                    command.execute(item.getId());
                                    viewModel.setLocationOnItemBought(item.getId(), googleBrowserApiKey);
                                } catch (IndexOutOfBoundsException ex) {
                                    //nothing to do
                                }
                            }
                        }
                        viewModel.moveBoughtItemsToEnd();
                    }
                }
        );

        shoppingListView.enableDragAndDrop();
        shoppingListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                                   final int position, final long id) {
                        // Bought Items are not draggable
                        if(shoppingListAdapter.getItem(position).isBought())
                            return true;

                        //disable events on observable list during drag&drop to prevent lag
                        //IMPORTANT: Make sure to reenable events afterwards
                        viewModel.getItems().disableEvents();
                        shoppingListView.startDragging(position);
                        return true;
                    }
                }
        );

        shoppingListView.setOnItemMovedListener(new OnItemMovedListener() {
            @Override
            public void onItemMoved(int i, int i1) {
                //IMPORTANT: reenable events of the observable list after drag and drop has finished
                viewModel.getItems().enableEvents();
                viewModel.itemsSwapped(i, i1);
                viewModel.moveBoughtItemsToEnd();
            }
        });

        //View footer = inflater.inflate(R.layout.listview_footerspace, container, false);
        //shoppingListView.addFooterView(footer);

        //justifyListViewHeightBasedOnChildren(shoppingListView);

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

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechRecognitionHelper.run(getActivity());
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);

            }

        });

        // shopping mode is on
        if(SharedPreferencesHelper.loadBoolean(ShoppingListActivity.SHOPPING_MODE_SETTING, false, getActivity())){
             // remove quick add view

            ((ViewManager) quickAddLayout.getParent()).removeView(quickAddLayout);
            ((ViewManager) floatingActionButton.getParent()).removeView(floatingActionButton);
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

    /*public void justifyListViewHeightBasedOnChildren(DynamicListView listView) {
        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }

        listView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        int listWidth = listView.getMeasuredWidth();
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(MeasureSpec.makeMeasureSpec(listWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }*/

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

        //justifyListViewHeightBasedOnChildren(shoppingListView);
    }

    @Override
    public void onItemRemoved(Item removedItem) {
        //justifyListViewHeightBasedOnChildren(shoppingListView);

        //sizeBoughtItems = viewModel.getBoughtItems().size();
        //cartCounter.setText(String.valueOf(sizeBoughtItems));
    }

    @Override
    public void onItemModified(Item modifiedItem) {
        //justifyListViewHeightBasedOnChildren(shoppingListView);

        //sizeBoughtItems = viewModel.getBoughtItems().size();
        //cartCounter.setText(String.valueOf(sizeBoughtItems));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            textView_QuickAdd.setText(spokenText);
            // Do something with spokenText
        }
    }
}
