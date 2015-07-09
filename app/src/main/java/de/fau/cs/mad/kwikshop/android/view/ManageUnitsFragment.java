package de.fau.cs.mad.kwikshop.android.view;


;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.mock.SpaceTokenizer;
import de.fau.cs.mad.kwikshop.android.view.binding.ButtonBinding;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.RegularlyRepeatHelper;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.view.interfaces.SaveDeleteActivity;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.common.ObservableArrayList;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.greenrobot.event.EventBus;

/**
 * Created by Nicolas on 01/07/2015.
 */
public class ManageUnitsFragment
        extends Fragment
        implements ShoppingListViewModel.Listener, ObservableArrayList.Listener<Unit> {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private ShoppingListViewModel viewModel;
    @InjectView(R.id.quickAddUnit)
    MultiAutoCompleteTextView quickAddUnit;
    @InjectView(R.id.quickAddGroup)
    MultiAutoCompleteTextView quickAddGroup;
    @InjectView(R.id.button_qaunit)
    ImageButton button_qaunit;
    @InjectView(R.id.button_qagroup)
    ImageButton button_qagroup;
    @InjectView(R.id.unit_spinner)
    Spinner unit_spinner;
    @InjectView(R.id.group_spinner)
    Spinner group_spinner;

    private List<Unit> units;
    private int selectedUnitIndex = -1;
    private List<Group> groups;
    private int selectedGroupIndex = -1;
    private boolean updatingViewModel;

    ArrayAdapter<String> spinnerArrayAdapter;
    private int listID = -1;

    private AutoCompletionHelper autoCompletion;
    private View rootView;

    public static ManageUnitsFragment newInstance() {

        ManageUnitsFragment fragment = new ManageUnitsFragment();
        return fragment;

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_unit_settings, container, false);
        ButterKnife.inject(this, rootView);
        setCustomActionBar();

        EventBus.getDefault().register(this);
        getActivity().setTitle(R.string.settings_option_3_manageUnits);
        setupUI();
        return rootView;
    }
    @Override
    public void onResume() {

        super.onResume();

        quickAddUnit.requestFocus();
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }
    private void setCustomActionBar() {

        if (getActivity() instanceof SaveDeleteActivity) {

            SaveDeleteActivity parent = (SaveDeleteActivity) getActivity();

            View saveButton = parent.getSaveButton();
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (quickAddUnit.getText().length() > 0) {
                        //save unit
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_empty_productname), Toast.LENGTH_LONG).show();
                    }
                }
            });


        }


    }



    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);

        Window window = getActivity().getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }


    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent event) {

            setupUI();

    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    private void setupUI() {


        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        //populate unit picker with units from database
        DisplayHelper displayHelper = new DisplayHelper(getActivity());

        viewModel = objectGraph.get(ShoppingListViewModel.class);
        autoCompletion = objectGraph.get(AutoCompletionHelper.class);



        //get units from the database and sort them by name
        units = ListStorageFragment.getUnitStorage().getItems();
        Collections.sort(units, new Comparator<Unit>() {
            @Override
            public int compare(Unit lhs, Unit rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        //TODO implement adapter for Unit instead of String

        ArrayList<String> unitNames = new ArrayList<>();
        for (Unit u : units) {
            unitNames.add(displayHelper.getDisplayName(u));
        }

        spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, unitNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unit_spinner.setAdapter(spinnerArrayAdapter);
        unit_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUnitIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedUnitIndex = -1;
            }


        });





        //get groups from the database and populate group spinner
        groups = ListStorageFragment.getGroupStorage().getItems();
        ArrayList<String> groupNames = new ArrayList<>();
        for (Group g : groups) {
            groupNames.add(displayHelper.getDisplayName(g));
        }

        final ArrayAdapter<String> groupSpinnerArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, groupNames);
        groupSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        group_spinner.setAdapter(groupSpinnerArrayAdapter);
        group_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroupIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGroupIndex = -1;
            }
        });

        new ButtonBinding(button_qaunit, viewModel.getQuickAddUnitCommand(spinnerArrayAdapter, quickAddUnit));



        //TODO: quick add long click

        quickAddUnit.setFocusableInTouchMode(true);
        quickAddUnit.requestFocus();

        quickAddUnit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                synchronized (viewModel) {
                    // If the event is a key-down event on the "enter" button
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        viewModel.getQuickAddUnitCommand(spinnerArrayAdapter, quickAddUnit).execute(null);
                       // updateSpinnerList();
                        return true;
                    }
                    return false;
                }
            }
        });
        quickAddUnit.setTokenizer(new SpaceTokenizer());

        new ButtonBinding(button_qagroup, viewModel.getQuickAddGroupCommand(groupSpinnerArrayAdapter, quickAddGroup));

        //TODO: quick add long click

        quickAddGroup.setFocusableInTouchMode(true);
        quickAddGroup.requestFocus();

        quickAddGroup.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                synchronized (viewModel) {
                    // If the event is a key-down event on the "enter" button
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        viewModel.getQuickAddGroupCommand(groupSpinnerArrayAdapter, quickAddGroup).execute(null);
                        // updateSpinnerList();
                        return true;
                    }
                    return false;
                }
            }
        });
        quickAddGroup.setTokenizer(new SpaceTokenizer());
        RegularlyRepeatHelper.getRegularlyRepeatHelper(getActivity()); // to make sure it is initialized when needed in ShoppingListViewModel

        refreshQuickAddAutoCompletion();


    }
    /**
     * call this method to initialize or refresh the data used by QuickAdd's auto completion
     */
    private void refreshQuickAddAutoCompletion() {
        quickAddUnit.setAdapter(autoCompletion.getNameAdapter(getActivity()));
    }

    @Override
    public void onItemSortTypeChanged() {

    }
    @OnTextChanged(R.id.quickAddUnit)
    @SuppressWarnings("unused")
    public void textView_ManageUnits_OnTextChanged(CharSequence s) {

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
            this.quickAddUnit.setText(viewModel.getQuickAddText());
        }
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
    public void onItemModified(Unit modifiedItem) {

    }
    @Override
    public void onItemAdded(Unit newItem) {

        //TODO: It might make sense to move autocompletion handling to the view model
        //IMPORTANT
        if(autoCompletion != null) {
            refreshQuickAddAutoCompletion();
        }


    }
    @OnTextChanged(R.id.quickAddGroup)
    @SuppressWarnings("unused")
    public void textView_ManageGroups_OnTextChanged(CharSequence s) {

        synchronized (viewModel) {
            //send updated value for shopping list name to the view model
            updatingViewModel = true;
            viewModel.setQuickAddText(s != null ? s.toString() : "");
            updatingViewModel = false;
        }
    }
    @Override
    public void onItemRemoved(Unit removedItem) {

    }

}
