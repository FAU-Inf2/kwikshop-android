package de.cs.fau.mad.quickshop.android.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import cs.fau.mad.quickshop_android.R;
import dagger.ObjectGraph;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.view.interfaces.SaveCancelActivity;
import de.cs.fau.mad.quickshop.android.viewmodel.ListOfShoppingListsViewModel;
import de.cs.fau.mad.quickshop.android.viewmodel.ShoppingListDetailsViewModel;
import de.cs.fau.mad.quickshop.android.viewmodel.di.QuickshopViewModelModule;

public class ShoppingListDetailFragment extends FragmentWithViewModel implements ShoppingListDetailsViewModel.Listener {


    public static final String EXTRA_SHOPPINGLISTID = "extra_ShoppingListId";


    @InjectView(R.id.textView_ShoppingListName)
    TextView textView_ShoppingListName;

    @InjectView(R.id.button_delete)
    View button_Delete;

    @InjectView(R.id.create_calendar_event)
    View button_CreateCalendarEvent;

    @InjectView(R.id.edit_calendar_event)
    View button_EditCalendarEvent;

    private ShoppingListDetailsViewModel viewModel;
    private boolean updatingViewModel = false;


    public static ShoppingListDetailFragment newInstance() {
        ShoppingListDetailFragment fragment = new ShoppingListDetailFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());

        ObjectGraph objectGraph = ObjectGraph.create(new QuickshopViewModelModule(getActivity()));
        viewModel = objectGraph.get(ShoppingListDetailsViewModel.class);
        initializeViewModel();

        View rootView = inflater.inflate(R.layout.activity_shopping_list_detail, container, false);
        ButterKnife.inject(this, rootView);




        // focus test box
        textView_ShoppingListName.setText(viewModel.getName());
        textView_ShoppingListName.setFocusable(true);
        textView_ShoppingListName.setFocusableInTouchMode(true);
        textView_ShoppingListName.requestFocus();

        //show keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //set up binding between view and view model
        bindButton(button_Delete, viewModel.getDeleteCommand());
        bindButton(button_CreateCalendarEvent, viewModel.getCreateCalendarEventCommand());
        bindButton(button_EditCalendarEvent, viewModel.getEditCalendarEventCommand());

        Activity activity = getActivity();
        if (activity instanceof SaveCancelActivity) {
            SaveCancelActivity saveCancelActivity = (SaveCancelActivity) activity;

            bindButton(saveCancelActivity.getSaveButton(), viewModel.getSaveCommand());
            bindButton(saveCancelActivity.getCancelButton(), viewModel.getCancelCommand());
        }

        viewModel.addListener(this);

        return rootView;
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
            textView_ShoppingListName.setText(viewModel.getName());
        }
    }

    @Override
    public void onFinish() {
        textView_ShoppingListName.setText("");
        getActivity().finish();
    }


    @OnTextChanged(R.id.textView_ShoppingListName)
    public void textView_ShoppingListName_OnTextChanged(CharSequence s) {

        //send updated value for shopping list name to the view model
        updatingViewModel = true;
        viewModel.setName(s != null ? s.toString() : "");
        updatingViewModel = false;

    }

    private void initializeViewModel() {

        Intent intent = getActivity().getIntent();

        if (intent.hasExtra(EXTRA_SHOPPINGLISTID)) {
            int shoppingListId = ((Long) intent.getExtras().get(EXTRA_SHOPPINGLISTID)).intValue();
            viewModel.initialize(shoppingListId);
        } else {
            viewModel.initialize();
        }
    }


}
