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

    private int[] Images = new int[] { R.drawable.ic_home, R.drawable.ic_home,
            R.drawable.ic_home, R.drawable.ic_home

    };

    private int mCount = Images.length;

    public TutorialSlidesFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putInt(IMAGE_RESOURCE_ID, Images[position]);
        Fragment fragment = new TutorialSlideFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public int getIconResId(int index) {
        return 0;
    }

}
