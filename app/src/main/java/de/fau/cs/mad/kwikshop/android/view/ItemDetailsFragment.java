package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.SpeechRecognitionHelper;
import de.fau.cs.mad.kwikshop.android.util.DateFormatter;
import de.fau.cs.mad.kwikshop.android.view.binding.ButtonBinding;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.messages.AutoCompletionHistoryDeletedEvent;
import de.fau.cs.mad.kwikshop.android.model.mock.SpaceTokenizer;
import de.fau.cs.mad.kwikshop.common.util.StringHelper;
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

    @InjectView(R.id.productname_text)
    MultiAutoCompleteTextView productName_text;

    @InjectView(R.id.brand_text)
    AutoCompleteTextView brand_text;

    @InjectView(R.id.comment_text)
    EditText comment_text;

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

        productName_text.requestFocus();

        if (viewModel.isNewItem()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(productName_text, InputMethodManager.SHOW_IMPLICIT);
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
        setCustomActionBar();

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
            productName_text.setAdapter(autoCompletionHelper.getNameAdapter(getActivity()));
            brand_text.setAdapter(autoCompletionHelper.getBrandAdapter(getActivity()));
        }
    }

    protected void setupUI() {

        productName_text.setAdapter(autoCompletionHelper.getNameAdapter(getActivity()));
        productName_text.setTokenizer(new SpaceTokenizer());
        brand_text.setAdapter(autoCompletionHelper.getBrandAdapter(getActivity()));

        // name text field
        onNameChanged();
        onBrandChanged();
        onCommentChanged();
        onIsHighlightedChanged();

        onLocationChanged();
    }

    @OnTextChanged(R.id.productname_text)
    @SuppressWarnings("unused")
    public void productname_text_TextChanged(CharSequence s) {

        if(viewModel != null) {
            synchronized (viewModel) {
                updatingName = true;
                viewModel.setName(s.toString());
                updatingName = false;
            }
        }
    }

    @Override
    public void onNameChanged() {
        if(!updatingName) {
            productName_text.setText(viewModel.getName());
        }
    }

    @OnTextChanged(R.id.comment_text)
    @SuppressWarnings("unused")
    public void comment_text_TextChanged(CharSequence s) {
        if(viewModel != null) {
            synchronized (viewModel) {
                updatingComment = true;
                viewModel.setComment(s.toString());
                updatingComment = false;
            }
        }
    }

    @Override
    public void onCommentChanged() {
        if(!updatingComment) {
            comment_text.setText(viewModel.getComment());
        }
    }

    @OnTextChanged(R.id.brand_text)
    @SuppressWarnings("unused")
    public void brand_text_TextChanged(CharSequence s) {

        updatingBrand = true;
        viewModel.setBrand(s.toString());
        updatingBrand = false;
    }

    @Override
    public void onBrandChanged() {
        if(!updatingBrand) {
            brand_text.setText(viewModel.getBrand());
        }
    }

    @OnCheckedChanged(R.id.highlight_checkBox)
    @SuppressWarnings("unused")
    public void highlight_checkBox_CheckedChanged() {

        updatingIsHighlighted = true;

        viewModel.setIsHighlighted(highlight_checkbox.isChecked());

        updatingIsHighlighted = false;
    }

    public void onIsHighlightedChanged() {
        if(!updatingIsHighlighted) {
            highlight_checkbox.setChecked(viewModel.getIsHighlighted());
        }

    }

    protected void setCustomActionBar() {

        if (getActivity() instanceof SaveDeleteActivity) {

            SaveDeleteActivity parent = (SaveDeleteActivity) getActivity();
            new ButtonBinding(parent.getSaveButton(), viewModel.getSaveItemCommand());
            new ButtonBinding(parent.getDeleteButton(), viewModel.getDeleteItemCommand());
        }
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

        ArrayAdapter<String> groupSpinnerArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getAvailableGroupNames());
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


    private ArrayList<String> getAvailableGroupNames() {

        ArrayList<String> names = new ArrayList<>();

        for(Group g: viewModel.getAvailableGroups()) {
            names.add(displayHelper.getDisplayName(g));
        }

        return names;
    }


    @Override
    public void onAvailableAmountsChanged() {

    }

    @Override
    public void onSelectedAmountChanged() {

    }

    @Override
    public void onAvailableUnitsChanged() {

    }

    @Override
    public void onSelectedUnitChanged() {

    }

    @Override
    public void onImageIdChanged() {

    }

    @Override
    public void onLastBoughtDateChanged() {

    }

    @Override
    public void onFinish() {
        getActivity().finish();
    }
}
