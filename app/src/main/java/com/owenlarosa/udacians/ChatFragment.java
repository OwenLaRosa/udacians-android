package com.owenlarosa.udacians;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DatabaseReference;
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

    // ID of the chat to be displayed
    public static final String EXTRA_CHAT = "chat";
    // whether or not this chat is a direct message
    public static final String EXTRA_DIRECT = "direct";

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

        String chatId = "";
        boolean direct;

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            chatId = intent.getStringExtra(EXTRA_CHAT);
            direct = intent.getBooleanExtra(EXTRA_DIRECT, false);
        } else {
            chatId = getArguments().getString(EXTRA_CHAT);
            direct = getArguments().getBoolean(EXTRA_DIRECT, false);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference chatReference;
        if (direct) {
            // direct messages
            chatReference = mFirebaseDatabase.getReference().child("users").child(chatId).child("messages");
        } else {
            chatReference = mFirebaseDatabase.getReference().child("topics").child(chatId).child("messages");
        }

        MessageListAdapter adapter = new MessageListAdapter(getActivity(), chatReference);
        messagesListView.setAdapter(adapter);

        return rootView;
    }
}


