package com.owenlarosa.udaciansapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.owenlarosa.udaciansapp.adapter.JobsListAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class JobsListFragment extends Fragment {

    @BindView(R.id.jobs_list_view)
    ListView listView;

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jobs_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        listView.setAdapter(new JobsListAdapter(getActivity()));

        return rootView;
    }

}
