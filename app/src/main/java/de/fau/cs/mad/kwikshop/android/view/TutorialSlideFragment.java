package de.fau.cs.mad.kwikshop.android.view;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;


public class TutorialSlideFragment extends Fragment {

    @InjectView(R.id.image_view_tutorial)
    ImageView imageView;

    int imageResourceId;

    public TutorialSlideFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        imageResourceId = bundle.getInt(TutorialSlidesFragmentAdapter.IMAGE_RESOURCE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tutorial_slide, container, false);
        ButterKnife.inject(this, rootView);

        imageView.setImageResource(imageResourceId);

        return rootView;
    }


}
