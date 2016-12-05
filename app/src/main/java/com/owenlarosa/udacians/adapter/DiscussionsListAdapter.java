package com.owenlarosa.udacians.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import com.owenlarosa.udacians.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.data;
import static android.R.attr.name;
import static android.R.attr.top;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class DiscussionsListAdapter extends BaseAdapter {

    private Context mContext;
    private String mUserId;

    private ArrayList<String> discussions = new ArrayList<String>();

    FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference enrollmentsReference;
    DatabaseReference topicsReference;

    public DiscussionsListAdapter(Context context, String userId) {
        mUserId = userId;
        mContext = context;
        DatabaseReference userReference = mFirebaseDatabase.getReference().child("users").child(mUserId);
        enrollmentsReference = userReference.child("enrollments");
        enrollmentsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String course = dataSnapshot.getKey();
                discussions.add(course);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String course = dataSnapshot.getKey();
                discussions.remove(course);
                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        topicsReference = userReference.child("topics");
        topicsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String topic = dataSnapshot.getKey();
                discussions.add(topic);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String topic = dataSnapshot.getKey();
                discussions.remove(topic);
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
        // number of courses + discussions user participates in
        return discussions.size();
    }

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
            cell = inflater.inflate(R.layout.chats_list_item, viewGroup, false);
            holder = new ViewHolder(cell);
            cell.setTag(holder);
        } else {
            holder = (ViewHolder) cell.getTag();
        }
        String topic = discussions.get(i);
        populateViewHolder(holder, topic);

        return cell;
    }

    static class ViewHolder {
        @BindView(R.id.chat_name_text_view)
        TextView nameTextView;
        @BindView(R.id.chat_description_text_view)
        TextView descriptionTextView;
        @BindView(R.id.chat_photo_image_view)
        ImageView imageView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    /**
     * Populate the view with data about the discussion
     * @param viewHolder View to be populated
     * @param topic Name of topic corresponding with this view
     */
    private void populateViewHolder(final ViewHolder viewHolder, String topic) {
        if (topic.startsWith("nd")) {
            // for chats corresponding with a specific Nanodegree
            final boolean beta;
            if (topic.endsWith("beta")) {
                // some students are enrolled in beta programs with different course IDs
                topic = topic.replace("beta", "");
                beta = true;
            } else {
                // non beta, will use regular course name
                beta = false;
            }
            DatabaseReference nanodegreeReference = mFirebaseDatabase.getReference().child("nano_degrees").child(topic);
            DatabaseReference nameReference = nanodegreeReference.child("name");
            nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name;
                    // indicate whether or not this is a beta version of the program
                    if (beta) {
                        name = dataSnapshot.getValue(String.class) + " " + mContext.getString(R.string.nd_beta_suffix);
                    } else {
                        name = dataSnapshot.getValue(String.class);
                    }
                    viewHolder.nameTextView.setText(name);
                    viewHolder.descriptionTextView.setText(mContext.getString(R.string.nd_chat_default, name));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            DatabaseReference imageReference = nanodegreeReference.child("image");
            imageReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String image = dataSnapshot.getValue(String.class);
                    Glide.with(mContext)
                            .load(image)
                            .into(viewHolder.imageView);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}

