package de.fau.cs.mad.kwikshop.android.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;
import de.fau.cs.mad.kwikshop.android.R;
import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.model.ListStorageFragment;
import de.fau.cs.mad.kwikshop.android.view.interfaces.SaveDeleteActivity;
import de.fau.cs.mad.kwikshop.android.viewmodel.ShoppingListDetailsViewModel;
import de.fau.cs.mad.kwikshop.android.viewmodel.di.KwikShopViewModelModule;

public class ShoppingListDetailFragment extends FragmentWithViewModel implements ShoppingListDetailsViewModel.Listener {


    public static final String EXTRA_SHOPPINGLISTID = "extra_ShoppingListId";


    @InjectView(R.id.textView_ShoppingListName)
    TextView textView_ShoppingListName;

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

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopViewModelModule(getActivity()));
        viewModel = objectGraph.get(ShoppingListDetailsViewModel.class);
        initializeViewModel();

        View rootView = inflater.inflate(R.layout.activity_shopping_list_detail, container, false);
        ButterKnife.inject(this, rootView);




        // focus test box
        textView_ShoppingListName.setText(viewModel.getName());






        //show keyboard
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //set up binding between view and view model
        bindButton(button_CreateCalendarEvent, viewModel.getCreateCalendarEventCommand());
        bindButton(button_EditCalendarEvent, viewModel.getEditCalendarEventCommand());

        Activity activity = getActivity();
        if (activity instanceof SaveDeleteActivity) {
            SaveDeleteActivity saveCancelActivity = (SaveDeleteActivity) activity;

            bindButton(saveCancelActivity.getSaveButton(), viewModel.getSaveCommand());
            bindButton(saveCancelActivity.getDeleteButton(), viewModel.getDeleteCommand());
        }

        textView_ShoppingListName.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    viewModel.getSaveCommand().execute(null);
                    return true;
                }
                return false;
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
            textView_ShoppingListName.requestFocus();
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
