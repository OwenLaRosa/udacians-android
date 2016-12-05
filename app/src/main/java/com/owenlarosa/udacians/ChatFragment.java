package com.owenlarosa.udacians;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.FirebaseDatabase;
import com.owenlarosa.udacians.adapter.MessageListAdapter;
import com.owenlarosa.udacians.views.ChatInputView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/11/16.
 */

public class ChatFragment extends Fragment {

    @BindView(R.id.chat_list_view)
    ListView messagesListView;
    @BindView(R.id.chat_entry)
    ChatInputView chatEntry;

    Unbinder mUnbinder;

    private FirebaseDatabase mFirebaseDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        MessageListAdapter adapter = new MessageListAdapter(getActivity(), mFirebaseDatabase.getReference());
        messagesListView.setAdapter(adapter);

        return rootView;
    }
}


