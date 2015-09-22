package de.fau.cs.mad.kwikshop.android.view;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.viewpagerindicator.IconPagerAdapter;

import java.util.ArrayList;

import de.fau.cs.mad.kwikshop.common.TutorialChapter;

public class TutorialSlidesFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

    public static final String IMAGE_RESOURCE_ID = "image_resource";
    public static final String TITLE_RESOURCE_ID = "title_resource";
    public static final String DESC_RESOURCE_ID = "desc_resource";
    public ArrayList<TutorialChapter> chapters;

    public TutorialSlidesFragmentAdapter(FragmentManager fm, ArrayList<TutorialChapter> chapters) {
        super(fm);
        this.chapters = chapters;
    }

    @Override
    public Fragment getItem(int position) {

        Bundle args = new Bundle();
        args.putInt(IMAGE_RESOURCE_ID, chapters.get(position).getGifID());
        args.putInt(TITLE_RESOURCE_ID, chapters.get(position).getTitleID());
        args.putInt(DESC_RESOURCE_ID, chapters.get(position).getDescriptionID());

        Fragment fragment = new TutorialSlideFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public int getCount() {
        return chapters == null ? 0 : chapters.size();
    }

    @Override
    public int getIconResId(int index) {
        return 0;
    }

}
