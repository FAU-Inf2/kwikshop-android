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

    private int[] Images = new int[] { R.drawable.ic_launcher, R.drawable.ic_about,
            R.drawable.ic_action, R.drawable.ic_add_to_cart

    };

    protected static final int[] ICONS = new int[] { R.drawable.ic_cart_out,
            R.drawable.ic_cart_out, R.drawable.ic_cart_out, R.drawable.ic_cart_out };

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
        return ICONS[index % ICONS.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}
