package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.view.interfaces.SaveDeleteActivity;
import de.fau.cs.mad.kwikshop.android.viewmodel.RecipesDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.di.KwikShopViewModelModule;

public class RecipeDetailFragment extends FragmentWithViewModel implements RecipesDetailsViewModel.Listener {


    public static final String EXTRA_RECIPEID = "extra_RecipeId";


    @InjectView(R.id.textView_RecipeName)
    TextView textView_RecipeName;

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

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopViewModelModule(getActivity()));
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

        viewModel.addListener(this);

        return rootView;
    }

    @Override
    public void onResume() {

        super.onResume();

        Window window = getActivity().getWindow();

        if (viewModel.getIsNewRecipe()) {
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


}
