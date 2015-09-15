package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.SpeechRecognitionHelper;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemViewModel;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.messages.AutoCompletionHistoryDeletedEvent;
import de.fau.cs.mad.kwikshop.android.model.mock.SpaceTokenizer;
import de.fau.cs.mad.kwikshop.android.view.binding.ButtonBinding;
import de.fau.cs.mad.kwikshop.android.view.binding.ListViewItemCommandBinding;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipeViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.common.util.StringHelper;
import de.greenrobot.event.EventBus;

public class RecipeFragment  extends Fragment implements RecipeViewModel.Listener, ObservableArrayList.Listener<ItemViewModel> {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;


    private static final String ARG_RECIPEID = "recipe_id";


    private int recipeID = -1;

    private static AutoCompletionHelper autoCompletion;

    private RecipeViewModel viewModel;
    private boolean updatingViewModel;

    @InjectView(R.id.list_recipe)
    DynamicListView recipeListView;

    @InjectView(R.id.textView_quickAdd)
    MultiAutoCompleteTextView textView_QuickAdd;

    @InjectView(R.id.fab)
    FloatingActionButton floatingActionButton;

    @InjectView(R.id.button_quickAdd)
    View button_QuickAdd;

    @InjectView(R.id.micButton)
    ImageButton micButton;

    @InjectView(R.id.swipe_container_recipe_list)
    SwipeRefreshLayout swipeLayout;


    public static RecipeFragment newInstance(int recipeID) {
        RecipeFragment fragment = new RecipeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_RECIPEID, recipeID);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            recipeID = getArguments().getInt(ARG_RECIPEID);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_add_to_a_shopping_list:
                viewModel.showAddRecipeDialog(recipeID);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        EventBus.getDefault().register(this);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_recipe, container, false);
        ButterKnife.inject(this, rootView);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));

        DisplayHelper displayHelper = objectGraph.get(DisplayHelper.class);

        viewModel = objectGraph.get(RecipeViewModel.class);
        viewModel.initialize(this.recipeID);

        getActivity().setTitle(viewModel.getName());

        viewModel.addListener(this);
        viewModel.getItems().addListener(this);

        View footer = inflater.inflate(R.layout.listview_footerspace, recipeListView, false);
        recipeListView.addFooterView(footer);

        RecipeAdapter recipeAdapter = new RecipeAdapter(getActivity(), viewModel,
                viewModel.getItems(), displayHelper);
        recipeListView.setAdapter(recipeAdapter);
        recipeListView.setSelector(R.drawable.list_selector);

        new ListViewItemCommandBinding(ListViewItemCommandBinding.ListViewItemCommandType.Click,
                recipeListView,
                viewModel.getSelectItemCommand());

        recipeListView.enableDragAndDrop();

        recipeListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                                   final int position, final long id) {
                        swipeLayout.setEnabled(false);
                        viewModel.getItems().disableEvents();
                        recipeListView.startDragging(position);
                        //sets value of the Spinner to the first entry, in this case Manual
                        return true;
                    }
                }
        );

        recipeListView.setOnItemMovedListener(new OnItemMovedListener() {
            @Override
            public void onItemMoved(int i, int i1) {
                swipeLayout.setEnabled(true);
                viewModel.itemsSwapped(i, i1);
            }
        });

        justifyListViewHeightBasedOnChildren(recipeListView);


        new ButtonBinding(floatingActionButton, viewModel.getAddItemCommand(), false);
        new ButtonBinding(button_QuickAdd, viewModel.getQuickAddCommand());

        button_QuickAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.getQuickAddCommand().execute(null);
                recipeListView.setSelection(viewModel.getItems().size());
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
                    return false;
                }
            }
        });
        textView_QuickAdd.setTokenizer(new SpaceTokenizer());

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechRecognitionHelper.run(getActivity());
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, RecognizerIntent.EXTRA_MAX_RESULTS);
                startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);

            }

        });


        //wire up auto-complete for product name
        if (autoCompletion == null) {
            autoCompletion = AutoCompletionHelper.getAutoCompletionHelper(getActivity().getBaseContext());
        }
        refreshQuickAddAutoCompletion();

        disableFloatingButtonWhileSoftKeyboardIsShown();

        // swipe refresh view
        swipeLayout.setColorSchemeResources(R.color.secondary_Color, R.color.primary_Color);
        swipeLayout.setDistanceToTriggerSync(250);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                SyncingActivity.requestSync();

                // wait
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                }, getResources().getInteger(R.integer.sync_delay));
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


    public void onEvent(AutoCompletionHistoryDeletedEvent event) {
        if (autoCompletion != null) {
            refreshQuickAddAutoCompletion();
        }
    }


    @OnTextChanged(R.id.textView_quickAdd)
    @SuppressWarnings("unused")
    public void textView_ShoppingListName_OnTextChanged(CharSequence s) {

        synchronized (viewModel) {
            //send updated value for recipe name to the view model
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
        //Nothing to do
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

        justifyListViewHeightBasedOnChildren(recipeListView);
    }

    @Override
    public void onItemRemoved(ItemViewModel removedItem) {
        justifyListViewHeightBasedOnChildren(recipeListView);
    }

    @Override
    public void onItemModified(ItemViewModel modifiedItem) {
        justifyListViewHeightBasedOnChildren(recipeListView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            boolean numberMatchFound = false;
            int positionMatchFound = 0;
            for(int i = 0; i < results.size(); i++){
                try{
                    Integer.parseInt(StringHelper.getFirstWord(results.get(i)));
                    numberMatchFound = true;
                    positionMatchFound = i;
                    break;
                }catch (NumberFormatException e){
                    numberMatchFound = false;
                }
            }
            String spokenText;
            if(numberMatchFound) spokenText = results.get(positionMatchFound);
            else spokenText = results.get(0);

            textView_QuickAdd.setText(spokenText);
            // Do something with spokenText

            if (textView_QuickAdd.requestFocus()) {
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(textView_QuickAdd, InputMethodManager.SHOW_IMPLICIT);
            }

        }
    }
}
