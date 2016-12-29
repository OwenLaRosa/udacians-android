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
import com.owenlarosa.udaciansapp.adapter.DiscussionsListAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/12/16.
 */

public class ChatsListFragment extends Fragment {

    @BindView(R.id.chats_list_view)
    ListView listView;
    private DiscussionsListAdapter adapter;

    Unbinder mUnbinder;
    private Context mContext;
    private String mUser;
    // true if list is showing direct chats, false for group discussions
    private boolean mDirect = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chats_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mContext = getActivity();

        mUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new DiscussionsListAdapter(mContext, mUser, false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                String chatId = adapter.getItem(i);
                intent.putExtra(ChatFragment.EXTRA_CHAT, chatId);
                // chats on this screen are public topics, not DMs
                intent.putExtra(ChatFragment.EXTRA_DIRECT, mDirect);
                startActivity(intent);
            }
        });
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.discussions_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_sort_groups:
                // general course discussions and topic where this user is a participant
                mDirect = false;
                adapter = new DiscussionsListAdapter(mContext, mUser, false);
                listView.setAdapter(adapter);
                return true;
            case R.id.menu_sort_direct:
                // users this user has sent or received direct messages
                // sorted with recent ones displayed first
                mDirect = true;
                adapter = new DiscussionsListAdapter(mContext, mUser, true);
                listView.setAdapter(adapter);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
