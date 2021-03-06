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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udaciansapp.ChatActivity;
import com.owenlarosa.udaciansapp.ChatFragment;
import com.owenlarosa.udaciansapp.Keys;
import com.owenlarosa.udaciansapp.R;

import java.util.ArrayList;

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

    public ConnectionsListAdapter(Context context, String user, boolean followers) {
        mUser = user;
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        if (followers) {
            // users who have added this user as a connection
            mConnectionsReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(mUser).child(Keys.FOLLOWERS);
        } else {
            // users that this user has added as a connection
            mConnectionsReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(mUser).child(Keys.CONNECTIONS);
        }
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
        return connections.get(i);
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
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
                String userId = connections.get(i);
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(ChatFragment.EXTRA_CHAT, userId);
                intent.putExtra(ChatFragment.EXTRA_DIRECT, true);
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
        DatabaseReference userBasicReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(userId).child(Keys.BASIC);
        DatabaseReference nameReference = userBasicReference.child(Keys.NAME);
        nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                viewHolder.nameTextView.setText(name);
                viewHolder.profileImageView.setContentDescription(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference titleReference = userBasicReference.child(Keys.TITLE);
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
        DatabaseReference photoReference = userBasicReference.child(Keys.PHOTO);
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
        DatabaseReference locationReference = mFirebaseDatabase.getReference().child(Keys.LOCATIONS).child(userId).child(Keys.LOCATION);
        locationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String location = dataSnapshot.getValue(String.class);
                    viewHolder.locationTextView.setText(location);
                } else {
                    // location not available in string form
                    viewHolder.locationTextView.setText(mContext.getString(R.string.unknown_location));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}

