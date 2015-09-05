package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.model.SpeechRecognitionHelper;
import de.fau.cs.mad.kwikshop.android.view.interfaces.SaveDeleteActivity;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipesDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;

public class RecipeDetailFragment extends FragmentWithViewModel implements RecipesDetailsViewModel.Listener {


    public static final String EXTRA_RECIPEID = "extra_RecipeId";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    @InjectView(R.id.textView_RecipeName)
    TextView textView_RecipeName;

    @InjectView(R.id.recipe_detail_numberPicker)
    NumberPicker numberPicker;

    @InjectView(R.id.recipe_detail_spinner)
    Spinner recipe_detail_spinner;

    @InjectView(R.id.micButton)
    ImageButton micButton;

    private RecipesDetailsViewModel viewModel;
    private boolean updatingViewModel = false;


    public static RecipeDetailFragment newInstance() {
        return new RecipeDetailFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));
        viewModel = objectGraph.get(RecipesDetailsViewModel.class);
        initializeViewModel();

        View rootView = inflater.inflate(R.layout.activity_recipe_detail, container, false);
        ButterKnife.inject(this, rootView);


        // focus test box
        textView_RecipeName.setText(viewModel.getName());


        //show keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //set up binding between view and view model

        Activity activity = getActivity();
        if (activity instanceof SaveDeleteActivity) {
            SaveDeleteActivity saveCancelActivity = (SaveDeleteActivity) activity;

            bindButton(saveCancelActivity.getSaveButton(), viewModel.getSaveCommand());
            bindButton(saveCancelActivity.getDeleteButton(), viewModel.getDeleteCommand());
        }



        //initialize number Picker
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(50);
        numberPicker.setValue(viewModel.getScaleFactor());

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                viewModel.setScaleFactor(newVal);
            }
        });

        ArrayList<String> spinner_entries = new ArrayList<String>(2);
        spinner_entries.add(0, getString(R.string.recipe_scaleName_person));
        spinner_entries.add(1, getString(R.string.recipe_scaleName_piece));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinner_entries);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recipe_detail_spinner.setAdapter(spinnerAdapter);
        if(viewModel.getScaleName().equals(getActivity().getString(R.string.recipe_scaleName_piece))){
            recipe_detail_spinner.setSelection(1);
        }
        recipe_detail_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        viewModel.setScaleName(getString(R.string.recipe_scaleName_person));
                        break;
                    case 1:
                        viewModel.setScaleName(getString(R.string.recipe_scaleName_piece));
                        break;
                    default:
                        viewModel.setScaleName(getString(R.string.recipe_scaleName_person));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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


        viewModel.addListener(this);

        return rootView;
    }

    @Override
    public void onResume() {

        super.onResume();

        Window window = getActivity().getWindow();

        if (viewModel.getIsNewList()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            textView_RecipeName.requestFocus();
        } else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.onDestroyView();
    }


    @Override
    public void onNameChanged(String value) {
        //update name changed in the view model in the view
        if (!updatingViewModel) {
            textView_RecipeName.setText(viewModel.getName());
        }
    }

    @Override
    public void onFinish() {
        textView_RecipeName.setText("");
        getActivity().finish();
    }


    @OnTextChanged(R.id.textView_RecipeName)
    public void textView_Recipe_OnTextChanged(CharSequence s) {

        //send updated value for shopping list name to the view model
        updatingViewModel = true;
        viewModel.setName(s != null ? s.toString() : "");
        updatingViewModel = false;

    }


    private void initializeViewModel() {

        Intent intent = getActivity().getIntent();

        if (intent.hasExtra(EXTRA_RECIPEID)) {
            int recipeId = ((Long) intent.getExtras().get(EXTRA_RECIPEID)).intValue();
            viewModel.initialize(recipeId);
        } else {
            viewModel.initialize();
        }
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
            textView_RecipeName.setText(spokenText);
            // Do something with spokenText
        }
    }


}
