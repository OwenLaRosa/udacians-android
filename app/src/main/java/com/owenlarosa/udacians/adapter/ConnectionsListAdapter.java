package com.owenlarosa.udacians.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.api.client.util.Data;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.ChatActivity;
import com.owenlarosa.udacians.R;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class ConnectionsListAdapter extends BaseAdapter {

    private Context mContext;
    private String mUser;

    private ArrayList<String> connections = new ArrayList<String>();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mConnectionsReference;

    public ConnectionsListAdapter(Context context, String user) {
        mUser = user;
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mConnectionsReference = mFirebaseDatabase.getReference().child("users").child(mUser).child("connections");
        mConnectionsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // keys are the user IDs
                connections.add(dataSnapshot.getKey());
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                connections.remove(dataSnapshot.getKey());
                notifyDataSetChanged();
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
        // 10 views as placeholders
        return connections.size();
    }

    // these 2 methods are required by base adapter, not used here
    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View cell = view;
        ViewHolder holder = null;
        if (cell == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            cell = inflater.inflate(R.layout.connections_list_item, viewGroup, false);
            holder = new ViewHolder(cell);
            cell.setTag(holder);
        } else {
            holder = (ViewHolder) cell.getTag();
        }
        String userId = connections.get(i);
        populateViewHolder(holder, userId);
        holder.messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                mContext.startActivity(intent);
            }
        });
        return cell;
    }

    static class ViewHolder {
        @BindView(R.id.connection_name_text_view)
        TextView nameTextView;
        @BindView(R.id.connection_title_text_view)
        TextView titleTextView;
        @BindView(R.id.connection_location_text_view)
        TextView locationTextView;
        @BindView(R.id.connection_photo_image_view)
        ImageView profileImageView;
        @BindView(R.id.message_button)
        ImageButton messageButton;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    /**
     * Fills in the view holder with data related to the user
     * @param viewHolder View to populate
     * @param userId User to fetch data for
     */
    public void populateViewHolder(final ViewHolder viewHolder, String userId) {
        DatabaseReference userBasicReference = mFirebaseDatabase.getReference().child("users").child(userId);
        DatabaseReference nameReference = userBasicReference.child("name");
        nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                viewHolder.nameTextView.setText(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference titleReference = userBasicReference.child("title");
        titleReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = dataSnapshot.getValue(String.class);
                viewHolder.titleTextView.setText(title);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference photoReference = userBasicReference.child("photo");
        photoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String photoUrl = dataSnapshot.getValue(String.class);
                Glide.with(mContext)
                        .load(photoUrl)
                        .into(viewHolder.profileImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference locationReference = mFirebaseDatabase.getReference().child("locations").child(userId).child("location");
        locationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String location = dataSnapshot.getValue(String.class);
                viewHolder.locationTextView.setText(location);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}

