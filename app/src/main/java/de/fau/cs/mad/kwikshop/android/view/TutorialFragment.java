package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import dagger.ObjectGraph;
import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.android.di.KwikShopModule;
import de.fau.cs.mad.kwikshop.android.viewmodel.TutorialViewModel;


public class TutorialFragment extends Fragment {

    TutorialViewModel viewModel;
    TutorialSlidesFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    public static TutorialFragment newInstance() {
        return new TutorialFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tutorial_pager, container, false);

        ObjectGraph objectGraph = ObjectGraph.create(new KwikShopModule(getActivity()));

        viewModel = objectGraph.get(TutorialViewModel.class);
        viewModel.setContext(getActivity().getApplicationContext());

        mAdapter = new TutorialSlidesFragmentAdapter(getActivity().getSupportFragmentManager(), viewModel.getChapters());

        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        ((CirclePageIndicator) mIndicator).setSnap(true);

        return rootView;
    }

}