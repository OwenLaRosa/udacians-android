package com.owenlarosa.udaciansapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class HelpFragment extends Fragment {

    private static final String HELP_FILE_URL = "file:///android_asset/udacians_help.html";

    @BindView(R.id.help_web_view)
    WebView webView;

    private Unbinder mUnbinder;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference bugsReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        webView.loadUrl(HELP_FILE_URL);

        return rootView;
    }

}
