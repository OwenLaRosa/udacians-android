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

    Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chats_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DiscussionsListAdapter adapter = new DiscussionsListAdapter(getActivity(), user);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                String chatId = adapter.getItem(i);
                intent.putExtra(ChatFragment.EXTRA_CHAT, chatId);
                // chats on this screen are public topics, not DMs
                intent.putExtra(ChatFragment.EXTRA_DIRECT, false);
                startActivity(intent);
            }
        });

        return rootView;
    }

}
