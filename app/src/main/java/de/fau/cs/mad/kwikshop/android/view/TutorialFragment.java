package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import de.fau.cs.mad.kwikshop.android.R;


public class TutorialFragment extends Fragment {


    TutorialSlidesFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    public static TutorialFragment newInstance() {
        return new TutorialFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tutorial_pager, container, false);

        mAdapter = new TutorialSlidesFragmentAdapter(getActivity()
                .getSupportFragmentManager());

        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        ((CirclePageIndicator) mIndicator).setSnap(true);

        mIndicator
                .setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        Toast.makeText(TutorialFragment.this.getActivity(), "Changed to page " + position, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPageScrolled(int position,
                                               float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });

        return rootView;
    }

}