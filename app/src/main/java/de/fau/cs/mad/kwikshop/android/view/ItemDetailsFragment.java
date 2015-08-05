package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
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
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.GroupStorage;
import de.fau.cs.mad.kwikshop.android.model.SpeechRecognitionHelper;
import de.fau.cs.mad.kwikshop.android.model.UnitStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.ActivityResultEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.DeleteItemEvent;
import de.fau.cs.mad.kwikshop.android.viewmodel.ItemDetailsViewModel;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.interfaces.DomainListObject;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.model.AutoCompletionHelper;
import de.fau.cs.mad.kwikshop.android.model.interfaces.ListManager;
import de.fau.cs.mad.kwikshop.android.model.interfaces.SimpleStorage;
import de.fau.cs.mad.kwikshop.android.model.messages.AutoCompletionHistoryDeletedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ItemChangedEvent;
import de.fau.cs.mad.kwikshop.android.model.messages.ListType;
import de.fau.cs.mad.kwikshop.android.model.mock.SpaceTokenizer;
import de.fau.cs.mad.kwikshop.android.util.ItemMerger;
import de.fau.cs.mad.kwikshop.android.util.StringHelper;
import de.fau.cs.mad.kwikshop.android.view.interfaces.SaveDeleteActivity;
import de.greenrobot.event.EventBus;

public abstract class ItemDetailsFragment<TList extends DomainListObject> extends Fragment {

    private static final int GALLERY = 1;
    private static final int GALLERY_INTENT_CALLED = 1;
    private static final int GALLERY_KITKAT_INTENT_CALLED = 0;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;


    protected boolean isNewItem;


    protected static final String ARG_LISTID = "list_id";
    protected static final String ARG_ITEMID = "item_id";

    private int listId;
    private int itemId;

    private int selectedUnitIndex = -1;
    private int selectedGroupIndex = -1;


    private String[] numbersForAmountPicker;
    private int numberPickerCalledWith;


    protected Item item;


    @InjectView(R.id.productname_text)
    MultiAutoCompleteTextView productname_text;

    @InjectView(R.id.numberPicker)
    NumberPicker numberPicker;

    @InjectView(R.id.unit_spinner)
    Spinner unit_spinner;

    @InjectView(R.id.brand_text)
    AutoCompleteTextView brand_text;

    @InjectView(R.id.comment_text)
    EditText comment_text;

    @InjectView(R.id.group_spinner)
    Spinner group_spinner;

    @InjectView(R.id.highlight_checkBox)
    CheckBox highlight_checkbox;

    @InjectView(R.id.lastbought_location)
    TextView lastbought_location;

    @InjectView(R.id.last_bought_relativelayout)
    RelativeLayout last_bought_relativelayout;

    @InjectView(R.id.micButton)
    ImageButton micButton;

    @InjectView(R.id.itemImageView)
    ImageView itemImageView;

    @InjectView(R.id.uploadText)
    TextView uploadText;

    @InjectView(R.id.button_remove)
    ImageView button_remove;

    @Inject
    AutoCompletionHelper autoCompletionHelper;

    @Inject
    SimpleStorage<Unit> unitStorage;

    @Inject
    SimpleStorage<Group> groupStorage;

    private ItemDetailsViewModel viewModel;


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

        productname_text.requestFocus();

        if (viewModel.isNewItem()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(productname_text, InputMethodManager.SHOW_IMPLICIT);
        } else {
            Window window = getActivity().getWindow();
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        EventBus.getDefault().register(this);

        View rootView = inflater.inflate(R.layout.fragment_item_details, container, false);
        ButterKnife.inject(this, rootView);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        viewModel = objectGraph.get(ItemDetailsViewModel.class);
        viewModel.initialize(listId, itemId);
        objectGraph.inject(this);

        isNewItem = viewModel.isNewItem();

        setupUI();

        // set actionbar with save and cancel buttons
        setCustomActionBar();

        // set actionbar title
        if (viewModel.isNewItem()) {
            getActivity().setTitle(R.string.title_fragment_item_details);
        } else {
            getActivity().setTitle(viewModel.getItemName());
        }


        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);

        Window window = getActivity().getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }


    @SuppressWarnings("unused")
    public void onEvent(AutoCompletionHistoryDeletedEvent event){
        if (autoCompletionHelper != null) {
            productname_text.setAdapter(autoCompletionHelper.getNameAdapter(getActivity()));
            brand_text.setAdapter(autoCompletionHelper.getBrandAdapter(getActivity()));
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ItemChangedEvent event) {
        if (event.getListType() == getListType() && event.getListId() == listId && event.getItemId() == item.getId()) {
            setupUI();
        }
    }

    public void onEvent(DeleteItemEvent event){
        //TODO: this is only a temporal solution untill ListManager works in ItemDetailsViewModel
        if(event.getListId() == listId && event.getItemId() == itemId){
            deleteItem();
            getActivity().finish();
        }
    }


    protected void saveItem() {

        if (viewModel.isNewItem()) {
            item = new Item();
        }

        item.setName(productname_text.getText().toString());
        if(numberPickerCalledWith != numberPicker.getValue()){
            //only set amount if it got changed, so values written by parser which are not listed
            //in the numberPicker don't get overwritten
            Double pickerAmountDouble;
            String numberPickerString = numbersForAmountPicker[numberPicker.getValue()];
                if (numberPickerString.contains("/")) {
                    String[] rat = numberPickerString.split("/");
                    pickerAmountDouble = Double.parseDouble(rat[0]) / Double.parseDouble(rat[1]);
                } else {
                    pickerAmountDouble= Double.parseDouble(numberPickerString);
                }
            item.setAmount(pickerAmountDouble);
        }

        item.setBrand(brand_text.getText().toString());
        item.setComment(comment_text.getText().toString());
        item.setHighlight(highlight_checkbox.isChecked());
        viewModel.setImageItem();

        if (selectedUnitIndex >= 0) {
            Unit u = viewModel.getSelectedUnit(selectedUnitIndex);
            item.setUnit(u);
        } else {
            item.setUnit(unitStorage.getDefaultValue());
        }

        if (selectedGroupIndex >= 0) {
            Group g = viewModel.getSelectedGroup(selectedGroupIndex);
            item.setGroup(g);
        } else {
            item.setGroup(groupStorage.getDefaultValue());
        }

        setAdditionalItemProperties();

        autoCompletionHelper.offerNameAndGroup(item.getName(), item.getGroup());
        autoCompletionHelper.offerBrand(item.getBrand());

        ItemMerger<TList> itemMerger = new ItemMerger<>(getListManager());
        if(viewModel.isNewItem()) {
            if(!itemMerger.mergeItem(listId, item)) {
                getListManager().addListItem(listId, item);
            }
        } else {
            if(itemMerger.mergeItem(listId, item)){
                getListManager().deleteItem(listId, item.getId());
            }else {
                getListManager().saveListItem(listId, item);
            }
        }


        hideKeyboard();

    }

    protected void deleteItem() {

        if(!viewModel.isNewItem()){
            getListManager().deleteItem(listId, itemId);
        }

        hideKeyboard();
    }

    protected void setupUI() {


        // display the supermarket where this item was bought

        LastLocation location = viewModel.isNewItem() ? null : getListManager().getListItem(listId, itemId).getLocation();

        if(location != null){
            if(location.getName() != null){
                long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - location.getTimestamp());
                String duration;
                if(days < 1){
                    duration =  getString(R.string.today);
                } else {
                    if(days == 1)
                        duration = days + " " + getString(R.string.day);
                    else
                        duration = days + " " + getString(R.string.days);
                }
                lastbought_location.setText(location.getName() + " (" + duration + ") ");
            } else {
                // hide information about last bought item
                ((ViewManager) last_bought_relativelayout.getParent()).removeView(last_bought_relativelayout);
            }
        } else {
            // hide information about last bought item
            if (last_bought_relativelayout != null)
                ((ViewManager) last_bought_relativelayout.getParent()).removeView(last_bought_relativelayout);
        }

        //populate number picker
        numbersForAmountPicker = new String[1003];
        String [] numsOnce = new String[]{
                "1/4","1/2","3/4","1","2","3","4","5","6","7","8","9","10","11", "12","15", "20","25","30", "40", "50", "60",
                "70", "75", "80", "90", "100", "125", "150", "175", "200", "250", "300", "350", "400",
                "450", "500", "600", "700", "750", "800", "900", "1000"
        };
        Double [] intNumsOnce = new Double[numsOnce.length];
        for(int i = 0; i < intNumsOnce.length; i++){
            if (numsOnce[i].contains("/")) {
                String[] rat = numsOnce[i].split("/");
                intNumsOnce[i] = (Double.parseDouble(rat[0]) / Double.parseDouble(rat[1]));
            } else {
                intNumsOnce[i] = Double.parseDouble(numsOnce[i]);
            }
        }


        //setDisplayedValues length must be as long as range
        for(int i = 0; i < numbersForAmountPicker.length; i++){
            numbersForAmountPicker[i] = numsOnce[i%numsOnce.length];
        }

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(1000);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setDisplayedValues(numbersForAmountPicker);

        //wire up auto-complete for product name and brand
        productname_text.setAdapter(autoCompletionHelper.getNameAdapter(getActivity()));
        productname_text.setTokenizer(new SpaceTokenizer());
        brand_text.setAdapter(autoCompletionHelper.getBrandAdapter(getActivity()));


        if (viewModel.isNewItem()) {

            productname_text.setText("");
            numberPicker.setValue(3);
            brand_text.setText("");
            comment_text.setText("");

        } else {
            item = getListManager().getListItem(listId, itemId);
            viewModel.setItem(item);

            viewModel.setImageId();
            //numberPicker.setValue(index) sets the picker to the index + numberPicker.minValue()
            double itemAmount = item.getAmount();

            int index = 1;
            for(int i = 0; i < intNumsOnce.length; i++){
                if(intNumsOnce[i].equals(itemAmount)){
                    index = i;
                    break;
                }
            }

            // Fill UI elements with data from Item
            productname_text.setText(item.getName());
            numberPicker.setValue(index);
            brand_text.setText(item.getBrand());
            comment_text.setText(item.getComment());
            //load image

            if (viewModel.getImageId() != null && !viewModel.getImageId().equals("")) {
                itemImageView.setImageURI(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, viewModel.getImageId()));
                if (itemImageView.getDrawable() == null) {
                    uploadText.setText(R.string.uploadPicture);
                    button_remove.setClickable(false);
                    button_remove.setEnabled(false);
                }
                else{
                    button_remove.setClickable(true);
                    button_remove.setEnabled(true);
                }
            }
            else{
                uploadText.setText(R.string.uploadPicture);
            }

        }
        numberPickerCalledWith = numberPicker.getValue();


        button_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemImageView.setImageDrawable(null);
                uploadText.setText(R.string.uploadPicture);
            }

        });

        //sort units by name
        viewModel.sortUnitsByName();

        //TODO implement adapter for Unit instead of String


        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, viewModel.getUnitNames());
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


        if (viewModel.getSelectedUnit() != null) {
            unit_spinner.setSelection(viewModel.getUnits().indexOf(viewModel.getSelectedUnit()));
        }


        ArrayAdapter<String> groupSpinnerArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, viewModel.getGroupNames());
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


        if (viewModel.getSelectedGroup() != null) {
            group_spinner.setSelection(viewModel.getGroups().indexOf(viewModel.getSelectedGroup()));
        }


        //check highlight_checkbox, if item is already highlighted
        if (!viewModel.isNewItem() && item.isHighlight()) {
            highlight_checkbox.setChecked(true);
        } else {
            highlight_checkbox.setChecked(false);
        }

        //set on click listener to item's image
        itemImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    itemImageView.setImageBitmap(null);
                    if (viewModel.getImageItem() != null)
                        viewModel.getImageItem().recycle();
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/jpeg");
                startActivityForResult(intent, GALLERY);
            }



        });

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.openVoiceRecognition();
            }

        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == GALLERY && resultCode != 0) {
            viewModel.setmImageUri(data.getData());
            try {

                viewModel.setImageItem(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), viewModel.getmImageUri()));
                String pathsegment[] = viewModel.getmImageUri().getLastPathSegment().split(":");
                viewModel.setImageId(pathsegment[1]);
                final String[] imageColumns = { MediaStore.Images.Media.DATA };
                final String imageOrderBy = null;

                Uri uri = getUri();
                Cursor imageCursor = getActivity().getContentResolver().query(uri, imageColumns,
                        MediaStore.Images.Media._ID + "=" + viewModel.getImageId(), null, null);

                if (imageCursor.moveToFirst()) {
                    viewModel.setPathImage(imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                }
                if (getOrientation(getActivity().getApplicationContext(), viewModel.getmImageUri()) != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(getOrientation(getActivity().getApplicationContext(), viewModel.getmImageUri()));
                    if (viewModel.getRotateImage() != null)
                        viewModel.getRotateImage().recycle();
                    viewModel.setRotateImage(Bitmap.createBitmap(viewModel.getImageItem(), 0, 0, viewModel.getImageItem().getWidth(), viewModel.getImageItem().getHeight(), matrix,true));


                    //itemImageView.setImageBitmap(rotateImage);

                    itemImageView.setImageURI(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, viewModel.getImageId()));
                } else
                    uploadText.setText("");
                    //itemImageView.setImageBitmap(ImageItem);
                itemImageView.setImageURI(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, viewModel.getImageId()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            productname_text.setText(spokenText);
            // Do something with spokenText
        }
    }
    // By using this method get the Uri of Internal/External Storage for Media
    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION },null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    @OnTextChanged(R.id.productname_text)
    @SuppressWarnings("unused")
    void onProductNameChanged(CharSequence text) {

        if(viewModel.getGroups() == null) {
            return;
        }

        String name = StringHelper.removeSpacesAtEndOfWord(text.toString());
        Group group = autoCompletionHelper.getGroup(name);

        if (group != null) {
            group_spinner.setSelection(viewModel.getGroups().indexOf(group));
        }
    }


    protected abstract ListType getListType();

    //work-around: right list manager cannot be injected because Dagger does not know final type
    // probably because of generics in java are broken
    protected abstract ListManager<TList> getListManager();

    /**
     * Will be called by saveItem() after properties have been set but before item is actually saved
     * This enables sub-classes to save additional data in the item
     */
    protected void setAdditionalItemProperties() {

    }


    protected void setCustomActionBar() {

        if (getActivity() instanceof SaveDeleteActivity) {

            SaveDeleteActivity parent = (SaveDeleteActivity) getActivity();

            View saveButton = parent.getSaveButton();
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (productname_text.getText().length() > 0) {
                        saveItem();
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_empty_productname), Toast.LENGTH_LONG).show();
                    }
                }
            });

            View delete = parent.getDeleteButton();

            if (viewModel.isNewItem()) {
                delete.setVisibility(View.GONE);
            } else {
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.showDeleteItemDialog(getString(R.string.title_delete_item), getString(R.string.message_delete_item),
                                getString(R.string.yes), getString(R.string.no), getString(R.string.dont_show_this_message_again));

                    }
                });
            }

        }


    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            return outputStream.toByteArray();

    }


}
