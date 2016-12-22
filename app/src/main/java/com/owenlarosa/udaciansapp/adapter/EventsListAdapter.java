package com.owenlarosa.udaciansapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udaciansapp.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class EventsListAdapter extends BaseAdapter {

    private Context mContext;
    private String mUser;

    // list of events (user IDs of organizers)
    private ArrayList<String> events = new ArrayList<String>();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mEventsReference;

    public EventsListAdapter(Context context, String user) {
        mUser = user;
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mEventsReference = mFirebaseDatabase.getReference().child("users").child(mUser).child("events");
        mEventsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // keys are the user IDs
                events.add(dataSnapshot.getKey());
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                events.remove(dataSnapshot.getKey());
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
        return events.size();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return events.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View cell = view;
        ViewHolder holder = null;
        if (cell == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            cell = inflater.inflate(R.layout.events_list_item, viewGroup, false);
            holder = new ViewHolder(cell);
            cell.setTag(holder);
        } else {
            holder = (ViewHolder) cell.getTag();
        }
        String userId = events.get(i);
        populateViewHolder(holder, userId);
        return cell;
    }

    static class ViewHolder {
        @BindView(R.id.event_name_text_view)
        TextView nameTextView;
        @BindView(R.id.event_about_text_view)
        TextView aboutTextView;
        @BindView(R.id.event_location_text_view)
        TextView locationTextView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    /**
     * Fill in the view with info about the event
     * @param viewHolder View to be populated
     * @param userId ID of the event / organizer's user ID
     */
    public void populateViewHolder(final ViewHolder viewHolder, String userId) {
        DatabaseReference eventReference = mFirebaseDatabase.getReference().child("events").child(userId).child("info");
        DatabaseReference nameReference = eventReference.child("name");
        nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.nameTextView.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference aboutReference = eventReference.child("about");
        aboutReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.aboutTextView.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference placeReference = eventReference.child("place");
        placeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.locationTextView.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

