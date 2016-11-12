package com.owenlarosa.udacians;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.owenlarosa.udacians.views.WritePostView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/12/16.
 */

public class EventFragment extends Fragment {

    @BindView(R.id.event_name_text_view)
    TextView nameTextView;
    @BindView(R.id.event_location_text_view)
    TextView locationTextView;
    @BindView(R.id.event_about_text_view)
    TextView aboutTextView;
    @BindView(R.id.event_write_post_view)
    WritePostView writePostView;
    @BindView(R.id.event_posts_recycler_view)
    RecyclerView recyclerView;

    Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @OnClick(R.id.attend_button)
    public void attendButtonTapped(View view) {

    }

}
