package de.cs.fau.mad.quickshop.android.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cs.fau.mad.quickshop_android.R;
import de.cs.fau.mad.quickshop.android.model.ListStorageFragment;
import de.cs.fau.mad.quickshop.android.view.interfaces.SaveCancelActivity;
import de.cs.fau.mad.quickshop.android.viewmodel.ShoppingListDetailsViewModel;

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

        View rootView = inflater.inflate(R.layout.activity_shopping_list_detail, container, false);
        ButterKnife.inject(this, rootView);

        new ListStorageFragment().SetupLocalListStorageFragment(getActivity());


        viewModel = initializeViewModel();


        // focus test box
        textView_ShoppingListName.setText(viewModel.getName());
        textView_ShoppingListName.setFocusable(true);
        textView_ShoppingListName.setFocusableInTouchMode(true);
        textView_ShoppingListName.requestFocus();
        textView_ShoppingListName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updatingViewModel = true;
                String text = textView_ShoppingListName.getText().toString();
                viewModel.setName(text);
                updatingViewModel = false;
            }
        });

        //show keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

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
        if (!updatingViewModel) {
            textView_ShoppingListName.setText(viewModel.getName());
        }
    }

    @Override
    public void onFinish() {
        textView_ShoppingListName.setText("");
        getActivity().finish();
    }




    private ShoppingListDetailsViewModel initializeViewModel() {

        Intent intent = getActivity().getIntent();
        int shoppingListId;
        if (intent.hasExtra(EXTRA_SHOPPINGLISTID)) {
            shoppingListId = ((Long) intent.getExtras().get(EXTRA_SHOPPINGLISTID)).intValue();
            return new ShoppingListDetailsViewModel(getActivity(), new DefaultViewLauncher(getActivity()),
                    ListStorageFragment.getLocalListStorage(), shoppingListId);

        } else {
            return new ShoppingListDetailsViewModel(getActivity(), new DefaultViewLauncher(getActivity()),
                    ListStorageFragment.getLocalListStorage());
        }
    }


}
