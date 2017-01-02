package com.owenlarosa.udaciansapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.owenlarosa.udaciansapp.adapter.PostFeedAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 1/1/17.
 */

public class PostFeedFragment extends Fragment {

    @BindView(R.id.feed_list_view)
    ListView feedListView;

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        PostFeedAdapter adapter = new PostFeedAdapter(getActivity(), user);
        feedListView.setAdapter(adapter);

        return rootView;
    }

}
