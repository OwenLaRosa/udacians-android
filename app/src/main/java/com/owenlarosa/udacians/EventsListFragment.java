package com.owenlarosa.udacians;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.owenlarosa.udacians.adapter.BlogFeedAdapter;
import com.owenlarosa.udacians.adapter.EventsListAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class EventsListFragment extends Fragment {

    @BindView(R.id.events_list_view)
    ListView listView;

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        listView.setAdapter(new EventsListAdapter(getActivity()));


        return rootView;
    }
}
