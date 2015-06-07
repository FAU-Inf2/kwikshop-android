package de.cs.fau.mad.kwikshop.android.view;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cs.fau.mad.kwikshop_android.R;


public class AboutActivity extends BaseActivity {

    @InjectView(R.id.gitHubLink)
    TextView textView_GitHubLink;

    @InjectView(R.id.about_textView_Version)
    TextView textView_Version;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.inject(this);

        // link to the app's GitHub page
        String linkText = String.format("<a href=\"%s\">%s</a> ",
                getResources().getString(R.string.githubLink),
                getResources().getString(R.string.viewOnGitHub));
        textView_GitHubLink.setText(Html.fromHtml(linkText));
        textView_GitHubLink.setMovementMethod(LinkMovementMethod.getInstance());

        // display version of package
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            textView_Version.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }


}
