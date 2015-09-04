package de.fau.cs.mad.kwikshop.android.view;



import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.melnykov.fab.FloatingActionButton;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;


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
import de.fau.cs.mad.kwikshop.android.viewmodel.BarcodeScannerViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.LocationViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.*;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.common.Item;
import de.greenrobot.event.EventBus;
import se.walkercrou.places.Place;


public class ShoppingListFragment
        extends Fragment
        implements ShoppingListViewModel.Listener, ObservableArrayList.Listener<ItemViewModel> {


    public static final String ARG_LISTID = "list_id";
    private static final String ARG_SHARINGCODE = "list_sharingcode";
    public static final String DO_NOT_ASK_FOR_SUPERMARKET = "ask_for_supermarket";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;


    private int listID = -1;

    private AutoCompletionHelper autoCompletion;

    private ShoppingListViewModel viewModel;

    private BarcodeScannerViewModel barcodeViewModel;

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

    @InjectView(R.id.button_barcode_scan)
    ImageButton btBarcodeScan;




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
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            listID = getArguments().getInt(ARG_LISTID);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.refresh_current_supermarket:
                Command<Void> command = viewModel.getFindNearbySupermarketCommand();
                if(command.getCanExecute()) {
                    command.execute(null);
                }
                return true;
            case R.id.action_add_recipe:
                viewModel.showAddRecipeDialog(listID);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        //TODO
//        locationViewModel.dismissProgressDialog();
//        locationViewModel.dismissDialog();
    }

    @Override
    public void onDestroy() {

        //TODO
        super.onDestroy();
        if(viewModel.getSwipedItemOrder().size() != 0 && viewModel.getSendBoughtItemsToServerCommand().getCanExecute()) {
            //viewModel.setPlacesChoiceIndex(locationViewModel.getPlaceChoiceIndex());
            //viewModel.getSendBoughtItemsToServerCommand().execute(null);
        }
//        locationViewModel.dismissProgressDialog();
//        locationViewModel.dismissDialog();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        EventBus.getDefault().register(this);

        ListStorageFragment.SetupLocalListStorageFragment(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);
        ButterKnife.inject(this, rootView);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        DisplayHelper displayHelper = objectGraph.get(DisplayHelper.class);
        viewModel = objectGraph.get(ShoppingListViewModel.class);
        autoCompletion = objectGraph.get(AutoCompletionHelper.class);
        barcodeViewModel = objectGraph.get(BarcodeScannerViewModel.class);
        objectGraph.inject(this);
        viewModel.initialize(this.listID);

        getActivity().setTitle(viewModel.getName());

        viewModel.addListener(this);
        viewModel.getItems().addListener(this);

        View footer = inflater.inflate(R.layout.listview_footerspace, shoppingListView, false);
        shoppingListView.addFooterView(footer);

        final ShoppingListAdapter shoppingListAdapter = new ShoppingListAdapter(getActivity(), viewModel,
                viewModel.getItems(), displayHelper);
        shoppingListView.setAdapter(shoppingListAdapter);

        new ListViewItemCommandBinding(ListViewItemCommandBinding.ListViewItemCommandType.Click,
                shoppingListView,
                viewModel.getSelectItemCommand());

        shoppingListView.enableSwipeToDismiss(
                new OnDismissCallback() {
                    @Override
                    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                        Command<Integer> command = viewModel.getToggleIsBoughtCommand();
                        for (int position : reverseSortedPositions) {
                            if (command.getCanExecute()) {
                                try {
                                    Item item = shoppingListAdapter.getItem(position).getItem();
                                    command.execute(item.getId());
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
                        if (shoppingListAdapter.getItem(position).getItem().isBought())
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


        new ButtonBinding(floatingActionButton, viewModel.getAddItemCommand(), false);


        button_QuickAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.getQuickAddCommand().execute(null);
                shoppingListView.setSelection(viewModel.getItems().size() - viewModel.getBoughtItemsCount());
            }
        });

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
                    shoppingListView.setSelection(viewModel.getItems().size() - viewModel.getBoughtItemsCount());
                    return false;
                }
            }
        });
        textView_QuickAdd.setTokenizer(new SpaceTokenizer());

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

        // find supermarket places
        if(!getActivity().getIntent().getExtras().getBoolean(DO_NOT_ASK_FOR_SUPERMARKET)){
            Command<Void> findSuperMarketsCommand = viewModel.getFindNearbySupermarketCommand();
            if(findSuperMarketsCommand.getCanExecute()) {
                findSuperMarketsCommand.execute(null);
            }
        }


        // shopping mode
        if(getActivity().getIntent().getExtras().getBoolean(ShoppingListActivity.SHOPPING_MODE)){
            // remove quick add view
            ((ViewManager) quickAddLayout.getParent()).removeView(quickAddLayout);
            ((ViewManager) floatingActionButton.getParent()).removeView(floatingActionButton);
        }



        // barcode scanner
        btBarcodeScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeViewModel.setContext(getActivity());
                if(barcodeViewModel.checkInternetConnection()){
                    android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(BaseActivity.frameLayout.getId(), BarcodeScannerFragment.newInstance(listID), "BARCODE_SCANNER_FRAGMENT").commit();
                } else {
                    barcodeViewModel.notificationOfNoConnection();
                }
            }
        });



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
    public void onItemAdded(ItemViewModel newItem) {
        //TODO: It might make sense to move autocompletion handling to the view model
        //IMPORTANT
        if(autoCompletion != null) {
            refreshQuickAddAutoCompletion();
        }
    }

    @Override
    public void onItemRemoved(ItemViewModel removedItem) {

    }

    @Override
    public void onItemModified(ItemViewModel modifiedItem) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            textView_QuickAdd.setText(spokenText);
            // Do something with spokenText
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
