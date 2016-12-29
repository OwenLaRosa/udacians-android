package com.owenlarosa.udaciansapp;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.owenlarosa.udaciansapp.adapter.ConnectionsListAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/12/16.
 */

public class  ConnectionsListFragment extends Fragment {

    @BindView(R.id.connections_list_view)
    ListView listView;
    private ConnectionsListAdapter adapter;

    Unbinder mUnbinder;
    Context mContext;
    String mUser;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connections_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mContext = getActivity();

        mUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // should show connections this user has added by default
        adapter = new ConnectionsListAdapter(mContext, mUser, false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                String userId = (String) adapter.getItem(i);
                intent.putExtra(ProfileFragment.EXTRA_USERID, userId);
                startActivity(intent);
            }
        });
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.connections_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_sort_connections) {
            // connections added by this user
            adapter = new ConnectionsListAdapter(mContext, mUser, false);
            listView.setAdapter(adapter);
            return true;
        } else if (id == R.id.menu_sort_followers) {
            // users who added this one as a connection
            adapter = new ConnectionsListAdapter(mContext, mUser, true);
            listView.setAdapter(adapter);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
