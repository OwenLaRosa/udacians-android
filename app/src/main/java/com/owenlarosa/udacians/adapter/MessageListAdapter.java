package com.owenlarosa.udacians.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.owenlarosa.udacians.R;
import com.owenlarosa.udacians.data.Message;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 12/5/16.
 */

public class MessageListAdapter extends BaseAdapter {

    private Context mContext;

    private ArrayList<Message> messages = new ArrayList<Message>();

    DatabaseReference messagesListReference;

    public MessageListAdapter(Context context, DatabaseReference messagesListReference) {
        mContext = context;
        this.messagesListReference = messagesListReference;
        messagesListReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messages.add(message);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View cell = view;
        ViewHolder holder = null;
        if (cell == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            cell = inflater.inflate(R.layout.message_view, viewGroup, false);
            holder = new ViewHolder(cell);
            cell.setTag(holder);
        } else {
            holder = (ViewHolder) cell.getTag();
        }
        return cell;
    }

    class ViewHolder {
        @BindView(R.id.message_name_text_view)
        TextView nameTextView;
        @BindView(R.id.message_profile_image_view)
        ImageView profileImageView;
        @BindView(R.id.message_time_text_view)
        TextView timeTextView;
        @BindView(R.id.message_content_text_view)
        TextView contentTextView;
        @BindView(R.id.message_content_image_view)
        ImageView contentImageView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
