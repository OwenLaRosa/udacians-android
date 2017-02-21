package com.owenlarosa.udaciansapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udaciansapp.ProfileActivity;
import com.owenlarosa.udaciansapp.ProfileFragment;
import com.owenlarosa.udaciansapp.R;
import com.owenlarosa.udaciansapp.Utils;
import com.owenlarosa.udaciansapp.data.Message;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 12/5/16.
 */

public class MessageListAdapter extends BaseAdapter {

    private Context mContext;

    private ArrayList<Message> messages = new ArrayList<Message>();

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference messagesListReference;

    public MessageListAdapter(Context context, DatabaseReference messagesListReference) {
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        this.messagesListReference = messagesListReference;
        messagesListReference.limitToLast(30).addChildEventListener(new ChildEventListener() {
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
        Message message = messages.get(i);
        populateViewHolder(holder, message);
        return cell;
    }

    class ViewHolder {
        @BindView(R.id.message_name_text_view)
        TextView nameTextView;
        @BindView(R.id.message_profile_image_button)
        ImageButton profileImageButton;
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

    /**
     * Display a message in the view
     * @param viewHolder View to be populated with message data
     * @param message Message to be displayed
     */
    private void populateViewHolder(final ViewHolder viewHolder, final Message message) {
        // user should see their own name in different color to easily recognize their messages
        if (message.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            viewHolder.nameTextView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        } else {
            viewHolder.nameTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
        }
        viewHolder.profileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // clicking on the profile image should launch user's profile
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(ProfileFragment.EXTRA_USERID, message.getSender());
                mContext.startActivity(intent);
                ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        DatabaseReference userBasicReference = mFirebaseDatabase.getReference().child("users").child(message.getSender()).child("basic");
        // user data stored in separate profile reference
        final DatabaseReference nameReference = userBasicReference.child("name");
        nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.nameTextView.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference photoReference = userBasicReference.child("photo");
        photoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Glide.with(mContext)
                        .load(dataSnapshot.getValue(String.class))
                        .into(viewHolder.profileImageButton);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (message.getContent() != null && !message.getContent().equals("")) {
            // message contents stored directly in object
            viewHolder.contentTextView.setVisibility(View.VISIBLE);
            viewHolder.contentTextView.setText(message.getContent());
        } else {
            viewHolder.contentTextView.setVisibility(View.GONE);
        }
        // message may also contain URL for attached image
        if (message.getImageUrl() != null && !message.getImageUrl().equals("")) {
            viewHolder.contentImageView.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(message.getImageUrl())
                    .into(viewHolder.contentImageView);
        } else {
            viewHolder.contentImageView.setVisibility(View.GONE);
        }

        // time should be formatted as hours:minutes
        Date date = new Date(message.getDate());
        viewHolder.timeTextView.setText(Utils.formatTime(date));
    }

    @Override
    public boolean isEnabled(int position) {
        // Nothing should happen when tapping message items
        return false;
    }
}
