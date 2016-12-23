package com.owenlarosa.udaciansapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.owenlarosa.udaciansapp.adapter.EventsListAdapter;

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

        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final EventsListAdapter adapter = new EventsListAdapter(getActivity(), user);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), EventActivity.class);
                intent.putExtra(EventFragment.EXTRA_USERID, (String) adapter.getItem(i));
                startActivity(intent);
            }
        });

        return rootView;
    }
}