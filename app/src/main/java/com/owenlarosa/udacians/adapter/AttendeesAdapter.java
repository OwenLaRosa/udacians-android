package com.owenlarosa.udacians.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.R;

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
     * @return Addresses as an array
     */
    public String[] getMailingList() {
        return (String[]) mailingList.toArray();
    }

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference attendeesReference;

    public AttendeesAdapter(Context context, String userId) {
        mContext = context;
        mUserId = userId;
        // event organizer is an attendee
        members.add(mUserId);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        attendeesReference = mFirebaseDatabase.getReference().child("events").child(mUserId).child("members");
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
        String userId = members.get(position);
        DatabaseReference photoReference = mFirebaseDatabase.getReference().child("users").child(userId).child("basic").child("photo");
        photoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String photoUrl = dataSnapshot.getValue(String.class);
                Glide.with(mContext)
                        .load(photoUrl)
                        .into(viewHolder.imageView);
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
        @BindView(R.id.attendee_image_view)
        ImageView imageView;

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
        DatabaseReference emailReference = mFirebaseDatabase.getReference().child("users").child(user).child("email");
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
