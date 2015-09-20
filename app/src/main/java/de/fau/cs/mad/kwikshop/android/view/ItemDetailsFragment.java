package de.fau.cs.mad.kwikshop.android.view;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnTextChanged;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.util.DateFormatter;
import de.fau.cs.mad.kwikshop.android.view.binding.ButtonBinding;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.messages.AutoCompletionHistoryDeletedEvent;
import de.fau.cs.mad.kwikshop.android.model.mock.SpaceTokenizer;
import de.fau.cs.mad.kwikshop.android.view.interfaces.SaveDeleteActivity;

public abstract class ItemDetailsFragment<TList extends DomainListObject> extends Fragment implements ItemDetailsViewModel.Listener {

    private static final int GALLERY = 1;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    public boolean numberPickerUpdating = false;

    protected static final String ARG_LISTID = "list_id";
    protected static final String ARG_ITEMID = "item_id";

    private int listId;
    private int itemId;

    ItemDetailsViewModel<TList> viewModel;

    private boolean updatingName = false;
    private boolean updatingComment = false;
    private boolean updatingBrand = false;
    private boolean updatingIsHighlighted = false;
    private boolean updatingUnit = false;
    private boolean updatingAmount = false;

    private final Object unitDisplayListLock = new Object();
    private final Object amountDisplayListLock = new Object();

    private DisplayList<Unit> unitDisplayList;
    private DisplayList<Double> amountDisplayList;

    @InjectView(R.id.productname_text)
    MultiAutoCompleteTextView textView_Name;

    @InjectView(R.id.brand_text)
    AutoCompleteTextView textView_Brand;

    @InjectView(R.id.comment_text)
    EditText textView_Comment;

    @InjectView(R.id.group_spinner)
    Spinner group_spinner;

    @InjectView(R.id.highlight_checkBox)
    CheckBox highlight_checkbox;

    @InjectView(R.id.lastbought_location)
    TextView lastBought_location;

    @InjectView(R.id.last_bought_relativelayout)
    RelativeLayout lastBought_relativeLayout;

    @InjectView(R.id.micButton)
    ImageButton micButton;

    @InjectView(R.id.itemImageView)
    ImageView itemImageView;

    @InjectView(R.id.uploadText)
    TextView uploadText;

    @InjectView(R.id.button_remove)
    ImageView button_remove;

    @InjectView(R.id.np_amount)
    NumberPicker amountPicker;

    @InjectView(R.id.np_unit)
    NumberPicker unitPicker;

    @Inject
    DateFormatter dateFormatter;

    @Inject
    DisplayHelper displayHelper;

    @Inject
    AutoCompletionHelper autoCompletionHelper;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // calling onCreateOptionsMenu
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            listId = getArguments().getInt(ARG_LISTID);
            itemId = getArguments().getInt(ARG_ITEMID);
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        textView_Name.requestFocus();

        if (viewModel.isNewItem()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(textView_Name, InputMethodManager.SHOW_IMPLICIT);
        } else {
            Window window = getActivity().getWindow();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_item_details, container, false);
        ButterKnife.inject(this, rootView);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        viewModel = createViewModel(objectGraph);
        viewModel.initialize(listId, itemId);

        objectGraph.inject(this);

        setupUI();

        // set actionbar with save and cancel buttons
        if (getActivity() instanceof SaveDeleteActivity) {

            SaveDeleteActivity parent = (SaveDeleteActivity) getActivity();
            new ButtonBinding(parent.getSaveButton(), viewModel.getSaveItemCommand());
            new ButtonBinding(parent.getDeleteButton(), viewModel.getDeleteItemCommand());
        }

        viewModel.setListener(this);

        // set actionbar title
        if (viewModel.isNewItem()) {
            getActivity().setTitle(R.string.title_fragment_item_details);
        } else {
            getActivity().setTitle(viewModel.getName());
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        viewModel.onDestroyView();

        // hide keyboard
        Window window = getActivity().getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @SuppressWarnings("unused")
    public void onEvent(AutoCompletionHistoryDeletedEvent event){
        if (autoCompletionHelper != null) {
            textView_Name.setAdapter(autoCompletionHelper.getNameAdapter(getActivity()));
            textView_Brand.setAdapter(autoCompletionHelper.getBrandAdapter(getActivity()));
        }
    }

    protected void setupUI() {

        amountPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        unitPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        // workaround for bug in numberPicker widget
        // see http://stackoverflow.com/questions/17708325/android-numberpicker-with-formatter-does-not-format-on-first-rendering
        try {
            Field f = NumberPicker.class.getDeclaredField("mInputText");
            f.setAccessible(true);
            EditText inputText = (EditText)f.get(amountPicker);
            inputText.setFilters(new InputFilter[0]);

        } catch (NoSuchFieldException | IllegalAccessException ignored) {

        }
        setDividerColor(unitPicker);
        setDividerColor(amountPicker);

        textView_Name.setAdapter(autoCompletionHelper.getNameAdapter(getActivity()));
        textView_Name.setTokenizer(new SpaceTokenizer());
        textView_Brand.setAdapter(autoCompletionHelper.getBrandAdapter(getActivity()));

        onNameChanged();
        onBrandChanged();
        onCommentChanged();
        onIsHighlightedChanged();
        onLocationChanged();
        onAvailableGroupsChanged();
        onSelectedGroupChanged();

        onAvailableUnitsChanged();
        onSelectedUnitChanged();

        onAvailableAmountsChanged();
        onSelectedAmountChanged(viewModel.getSelectedAmount(), viewModel.getSelectedAmount());


        amountPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                amountPicker_ValueChanged();
            }
        });

        unitPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                unitPicker_ValueChanged();
            }
        });
    }

    protected abstract ItemDetailsViewModel<TList> createViewModel(ObjectGraph objectGraph);

    private void setDividerColor(NumberPicker picker) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields)
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, getResources().getDrawable(R.drawable.np_numberpicker_selection_divider_green));
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
    }

    private ArrayList<String> getAvailableGroupNames(List<Group> groups) {

        ArrayList<String> names = new ArrayList<>();

        for(Group g: groups) {
            names.add(displayHelper.getDisplayName(g));
        }

        final Collator collator = Collator.getInstance(Locale.getDefault());
        Collections.sort(names, collator);

        return names;
    }

    private DisplayList<Unit> getUnitsForDisplay(List<Unit> units, double amount) {

        units = new ArrayList<>(units);

        final Collator collator = Collator.getInstance(Locale.getDefault());
        Collections.sort(units, new Comparator<Unit>() {
            @Override
            public int compare(Unit unit1, Unit unit2) {
                return collator.compare(displayHelper.getDisplayName(unit1), displayHelper.getDisplayName(unit2));
            }
        });

        DisplayList<Unit> result = new DisplayList<>();
        result.objectToIndexMap = new HashMap<>();
        result.indexToObjectMap = new HashMap<>();
        result.displayNames  = new String[units.size()];

        for(int i = 0; i < units.size(); i++) {
            result.objectToIndexMap.put(units.get(i), i);
            result.indexToObjectMap.put(i, units.get(i));
            result.displayNames[i] = displayHelper.getDisplayName(units.get(i), amount);
        }

        return result;
    }

    private DisplayList<Double> getAmountsForDisplay(List<Double> values) {

        DisplayList<Double> result = new DisplayList<>();
        result.objectToIndexMap = new HashMap<>();
        result.indexToObjectMap = new HashMap<>();
        result.displayNames = new String[values.size()];

        for(int i = 0; i < values.size(); i++) {
            result.objectToIndexMap.put(values.get(i), i);
            result.indexToObjectMap.put(i, values.get(i));
            result.displayNames[i] = displayHelper.getDisplayName(values.get(i));
        }

        return result;
    }


    //region Event Handlers

    @OnTextChanged(R.id.productname_text)
    @SuppressWarnings("unused")
    public void textView_Name_TextChanged(CharSequence s) {

        if(viewModel != null) {
            //noinspection SynchronizeOnNonFinalField
            synchronized (viewModel) {
                updatingName = true;
                viewModel.setName(s.toString());
                updatingName = false;
            }
        }
    }

    @OnTextChanged(R.id.comment_text)
    @SuppressWarnings("unused")
    public void textView_Comment_TextChanged(CharSequence s) {
        if(viewModel != null) {
            //noinspection SynchronizeOnNonFinalField
            synchronized (viewModel) {
                updatingComment = true;
                viewModel.setComment(s.toString());
                updatingComment = false;
            }
        }
    }

    @OnTextChanged(R.id.brand_text)
    @SuppressWarnings("unused")
    public void textView_Brand_TextChanged(CharSequence s) {

        updatingBrand = true;
        viewModel.setBrand(s.toString());
        updatingBrand = false;
    }

    @OnCheckedChanged(R.id.highlight_checkBox)
    @SuppressWarnings("unused")
    public void highlight_checkBox_CheckedChanged() {

        updatingIsHighlighted = true;

        viewModel.setIsHighlighted(highlight_checkbox.isChecked());

        updatingIsHighlighted = false;
    }

    public void unitPicker_ValueChanged() {

        updatingUnit = true;

        synchronized (unitDisplayListLock) {

            if(unitDisplayList == null) {
                return;
            }

            int selectedIndex = unitPicker.getValue();
            if(unitDisplayList.indexToObjectMap.containsKey(selectedIndex)) {
                viewModel.setSelectedUnit(unitDisplayList.indexToObjectMap.get(selectedIndex));
            }
        }

        updatingUnit  = false;
    }

    public void amountPicker_ValueChanged() {

        updatingAmount = true;

        synchronized (amountDisplayListLock) {

            if(amountDisplayList == null) {
                return;
            }

            int selectedIndex = amountPicker.getValue();
            if(amountDisplayList.indexToObjectMap.containsKey(selectedIndex)) {
                viewModel.setSelectedAmount(amountDisplayList.indexToObjectMap.get(selectedIndex));
            }
        }

        updatingAmount = true;

    }
    //endregion

    //region Listener Implementation

    @Override
    public void onNameChanged() {
        if(!updatingName) {
            textView_Name.setText(viewModel.getName());
        }
    }

    @Override
    public void onCommentChanged() {
        if(!updatingComment) {
            textView_Comment.setText(viewModel.getComment());
        }
    }

    @Override
    public void onBrandChanged() {
        if(!updatingBrand) {
            textView_Brand.setText(viewModel.getBrand());
        }
    }

    @Override
    public void onIsHighlightedChanged() {
        if(!updatingIsHighlighted) {
            highlight_checkbox.setChecked(viewModel.getIsHighlighted());
        }

    }

    @Override
    public void onLocationChanged() {

        // display the supermarket where this item was bought
        LastLocation location = viewModel.getLocation();

        if(location != null && location.getName() != null){
            String duration = dateFormatter.formatDate(viewModel.getLastBoughtDate());
            lastBought_location.setText(location.getName() + " (" + duration + ") ");
        } else {
            // hide information about last bought item
            if (lastBought_relativeLayout != null) {
                ((ViewManager) lastBought_relativeLayout.getParent()).removeView(lastBought_relativeLayout);
            }
        }
    }

    @Override
    public void onAvailableGroupsChanged() {

        ArrayAdapter<String> groupSpinnerArrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                getAvailableGroupNames(viewModel.getAvailableGroups()));

        groupSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        group_spinner.setAdapter(groupSpinnerArrayAdapter);
        group_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<Group> groups = viewModel.getAvailableGroups();
                if(position > 0 && position < groups.size()) {
                    viewModel.setSelectedGroup(groups.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setSelectedGroup(null);
            }
        });
    }

    @Override
    public void onSelectedGroupChanged() {

        int position = viewModel.getAvailableGroups().indexOf(viewModel.getSelectedGroup());

        if(position > 0) {
            group_spinner.setSelection(position);
        }
    }

    @Override
    public void onAvailableAmountsChanged() {

        List<Double> amounts = viewModel.getAvailableAmounts();
        synchronized (amountDisplayListLock) {

            amountDisplayList = getAmountsForDisplay(amounts);

            amountPicker.setMinValue(0);
            amountPicker.setMaxValue(amountDisplayList.displayNames.length - 1);

            amountPicker.setFormatter(new NumberPicker.Formatter() {
                @Override
                public String format(int i) {
                    return amountDisplayList.displayNames[i];
                }
            });

            onSelectedAmountChanged(0, 0);
        }
    }

    @Override
    public void onSelectedAmountChanged(double oldValue, double newValue) {

        if(!updatingAmount) {

            synchronized (amountDisplayListLock) {

                int selectedIndex =  amountDisplayList.objectToIndexMap.containsKey(viewModel.getSelectedAmount())
                        ? amountDisplayList.objectToIndexMap.get(viewModel.getSelectedAmount())
                        : 0;

                amountPicker.setMinValue(0);
                amountPicker.setMaxValue(amountDisplayList.displayNames.length -1);
                amountPicker.setValue(selectedIndex);
                amountPicker.invalidate();
            }
        }

        // change displayed unit names between singular and plural
        if((oldValue == 1 && newValue != 1) || (oldValue != 1 && newValue == 1)) {
            onAvailableUnitsChanged();
        }

    }


    @Override
    public void onAvailableUnitsChanged() {

        List<Unit> units = viewModel.getAvailableUnits();
        double amount = viewModel.getSelectedAmount();

        synchronized (unitDisplayListLock) {

            this.unitDisplayList = getUnitsForDisplay(units, amount);

            unitPicker.setMinValue(0);
            unitPicker.setMaxValue(unitDisplayList.displayNames.length - 1);
            unitPicker.setDisplayedValues(unitDisplayList.displayNames);

            onSelectedUnitChanged();
        }

    }

    @Override
    public void onSelectedUnitChanged() {

        if(updatingUnit) {
            return;
        }

        synchronized (unitDisplayListLock) {
            int selectedIndex =  unitDisplayList.objectToIndexMap.containsKey(viewModel.getSelectedUnit())
                    ? unitDisplayList.objectToIndexMap.get(viewModel.getSelectedUnit())
                    : 0;
            unitPicker.setValue(selectedIndex);
        }
    }

    @Override
    public void onImageIdChanged() {

    }

    @Override
    public void onLastBoughtDateChanged() {
        onLocationChanged();
    }

    @Override
    public void onFinish() {
        getActivity().finish();
    }

    //endregion



    private static class DisplayList<T> {

        Map<T, Integer> objectToIndexMap;
        Map<Integer, T> indexToObjectMap;
        String[] displayNames;

    }
}
