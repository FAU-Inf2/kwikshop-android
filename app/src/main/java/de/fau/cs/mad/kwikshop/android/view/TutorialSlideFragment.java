package de.fau.cs.mad.kwikshop.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;
import pl.droidsonroids.gif.GifDrawable;


public class TutorialSlideFragment extends Fragment {

    private static final float STOP = 0.00000001f;
    private static final float PLAY = 1f;

    @InjectView(R.id.iv_tutorial_gif)
    ImageView gif;

    @InjectView(R.id.tv_tutorial_title)
    TextView title;

    @InjectView(R.id.tv_tutorial_desc)
    TextView desc;

    @InjectView(R.id.iv_tutorial_play)
    ImageView playView;

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
            final GifDrawable gifFromResource = new GifDrawable(getResources(), imageResourceId);
            gif.setImageDrawable(gifFromResource);

            gifFromResource.setSpeed(STOP);

            playView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    playView.setVisibility(View.GONE);
                    gif.setVisibility(View.VISIBLE);

                    gifFromResource.setSpeed(PLAY);


                }
            });

            gif.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    playView.setVisibility(View.VISIBLE);
                    gif.setVisibility(View.GONE);

                    gifFromResource.setSpeed(STOP);

                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootView;
    }


}
