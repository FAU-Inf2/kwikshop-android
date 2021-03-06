package de.fau.cs.mad.kwikshop.android.view;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.fau.cs.mad.kwikshop.android.R;

public class AboutFragment extends Fragment{


    @InjectView(R.id.gitHubLink)
    TextView textView_GitHubLink;

    @InjectView(R.id.textView_GitCommit)
    TextView textView_GitCommit;

    @InjectView(R.id.container)
    ViewGroup container;

    @InjectView(R.id.acknowledgements_Container)
    ViewGroup acknowledgements_Container;

    public static AboutFragment newInstance(){
        return new AboutFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup fragmentContainer, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_about, fragmentContainer, false);
        ButterKnife.inject(this, rootView);


        // link to the app's GitHub page
        String linkText = String.format("<a href=\"%s\">%s</a> ",
                getResources().getString(R.string.githubLink),
                getResources().getString(R.string.viewOnGitHub));
        textView_GitHubLink.setText(Html.fromHtml(linkText));
        textView_GitHubLink.setMovementMethod(LinkMovementMethod.getInstance());

        //git commit


        String text = String.format(
                getResources().getString(R.string.about_Commit_Format),
                getResources().getString(R.string.BuildInfo_Git_Commit),
                getResources().getString(R.string.BuildInfo_CommonRepository_Git_Commit));

        textView_GitCommit.setText(text);

        //display acknowledgements

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        String[] acknowledgements = getResources().getStringArray(R.array.acknowledgements_Items);
        for (String item : acknowledgements) {

            View view = layoutInflater.inflate(R.layout.activity_about_acknowledgements_item, null);
            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setText(Html.fromHtml(item));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            acknowledgements_Container.addView(view);
        }


        // long Google Play Services attribution text
        // if we use Google Play Service it has to be included
        View view = layoutInflater.inflate(R.layout.activity_about_acknowledgements_item, null);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity()));
        container.addView(view);


        return rootView;
    }

}
