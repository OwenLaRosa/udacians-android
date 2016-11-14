package com.owenlarosa.udacians;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.owenlarosa.udacians.adapter.BlogFeedAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class BlogsFragment extends Fragment {

    @BindView(R.id.blogs_list_view)
    ListView listView;

    Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blogs_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        listView.setAdapter(new BlogFeedAdapter(getActivity()));

        return rootView;
    }
}
