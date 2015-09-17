package de.fau.cs.mad.kwikshop.android.view;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import pl.droidsonroids.gif.GifDrawable;


public class TutorialSlideFragment extends Fragment {

    @InjectView(R.id.iv_tutorial_gif)
    ImageView gif;

    @InjectView(R.id.tv_tutorial_title)
    TextView title;

    @InjectView(R.id.tv_tutorial_desc)
    TextView desc;

    int imageResourceId;
    int titleResourceId;
    int descResourceId;

    public TutorialSlideFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        imageResourceId = bundle.getInt(TutorialSlidesFragmentAdapter.IMAGE_RESOURCE_ID);
        titleResourceId = bundle.getInt(TutorialSlidesFragmentAdapter.TITLE_RESOURCE_ID);
        descResourceId = bundle.getInt(TutorialSlidesFragmentAdapter.DESC_RESOURCE_ID);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tutorial_slide, container, false);
        ButterKnife.inject(this, rootView);

        title.setText(titleResourceId);
        desc.setText(descResourceId);

        try {
            GifDrawable gifFromResource = new GifDrawable( getResources(), imageResourceId);
            gif.setImageDrawable(gifFromResource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootView;
    }


}
