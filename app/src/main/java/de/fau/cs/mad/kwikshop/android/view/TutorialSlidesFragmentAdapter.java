package de.fau.cs.mad.kwikshop.android.view;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.viewpagerindicator.IconPagerAdapter;

import de.fau.cs.mad.kwikshop.android.R;

public class TutorialSlidesFragmentAdapter extends FragmentPagerAdapter implements
        IconPagerAdapter {

    public static final String IMAGE_RESOURCE_ID = "image_resource";
    public static final String TITLE_RESOURCE_ID = "title_resource";
    public static final String DESC_RESOURCE_ID = "desc_resource";

    private int[] gif = new int[] { R.drawable.tutorial_nav_drawer, R.drawable.tutorial_nav_drawer, R.drawable.tutorial_nav_drawer};

    private int[] titles = new int[] {R.string.tutorial_navigation_title, R.string.tutorial_navigation_title, R.string.tutorial_navigation_title };

    private int[] descriptions = new int[] {R.string.tutorial_navigation_description, R.string.tutorial_navigation_description, R.string.tutorial_navigation_description};

    public TutorialSlidesFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putInt(IMAGE_RESOURCE_ID, gif[position]);
        args.putInt(TITLE_RESOURCE_ID, titles[position]);
        args.putInt(DESC_RESOURCE_ID, descriptions[position]);
        Fragment fragment = new TutorialSlideFragment();
        fragment.setArguments(args);
        return fragment;
    }

    int mCount = gif.length;

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public int getIconResId(int index) {
        return 0;
    }

}
