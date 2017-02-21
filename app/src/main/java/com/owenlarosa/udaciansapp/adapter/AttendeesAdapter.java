package com.owenlarosa.udaciansapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udaciansapp.Keys;
import com.owenlarosa.udaciansapp.ProfileActivity;
import com.owenlarosa.udaciansapp.ProfileFragment;
import com.owenlarosa.udaciansapp.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 11/30/16.
 */

public class AttendeesAdapter extends RecyclerView.Adapter {

    private Context mContext;
    // id of the event (organizer's user ID)
    private String mUserId;

    // user IDs of event attendees
    private ArrayList<String> members = new ArrayList<String>();
    private ArrayList<String> mailingList = new ArrayList<String>();
    /**
     * Get email addresses of all event members
     * @return Addresses as an arraylist
     */
    public ArrayList<String> getMailingList() {
        return mailingList;
    }

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference attendeesReference;

    public AttendeesAdapter(Context context, String userId) {
        mContext = context;
        mUserId = userId;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        attendeesReference = mFirebaseDatabase.getReference().child(Keys.EVENTS).child(mUserId).child(Keys.MEMBERS);
        attendeesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String user = dataSnapshot.getKey();
                members.add(user);
                notifyDataSetChanged();
                updateMailingList(user, true);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String user = dataSnapshot.getKey();
                members.remove(user);
                notifyDataSetChanged();
                updateMailingList(user, false);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // event organizer's ID has just been added
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attendee_recycler_view_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;
        // load image based on user ID
        final String userId = members.get(position);
        viewHolder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(ProfileFragment.EXTRA_USERID, userId);
                mContext.startActivity(intent);
                ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        DatabaseReference photoReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(userId).child(Keys.BASIC).child(Keys.PHOTO);
        photoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String photoUrl = dataSnapshot.getValue(String.class);
                Glide.with(mContext)
                        .load(photoUrl)
                        .into(viewHolder.imageButton);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference nameReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(userId).child(Keys.BASIC).child(Keys.NAME);
        nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                viewHolder.imageButton.setContentDescription(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.attendee_image_button)
        ImageButton imageButton;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * Add or remove the user to the mailing list
     * @param user ID or the user to be added/removed
     * @param addUser true if adding a user, false if removing one
     */
    private void updateMailingList(String user, final boolean addUser) {
        DatabaseReference emailReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(user).child(Keys.EMAIL);
        emailReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String email = dataSnapshot.getValue(String.class);
                if (addUser) {
                    mailingList.add(email);
                } else {
                    mailingList.remove(email);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
