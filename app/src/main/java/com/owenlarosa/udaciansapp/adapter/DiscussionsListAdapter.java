package com.owenlarosa.udaciansapp.adapter;

import android.app.Activity;
import android.content.Context;
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
import com.owenlarosa.udaciansapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class DiscussionsListAdapter extends BaseAdapter {

    private class DirectDiscussion implements Comparable<DirectDiscussion> {

        String userId;
        long lastUpdated;

        public DirectDiscussion(String userId, long lastUpdated) {
            this.userId = userId;
            this.lastUpdated = lastUpdated;
        }

        @Override
        public int compareTo(@Nonnull DirectDiscussion directDiscussion) {
            return lastUpdated > directDiscussion.lastUpdated ? 1 : 0;
        }
    }

    private Context mContext;
    private boolean mIsDirect;

    private ArrayList<String> discussions = new ArrayList<String>();
    private ArrayList<DirectDiscussion> directDiscussions = new ArrayList<>();

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference topicsReference;
    private DatabaseReference directMessagesReference;

    public DiscussionsListAdapter(Context context, String userId, boolean direct) {
        mIsDirect = direct;
        mContext = context;
        DatabaseReference userReference = mFirebaseDatabase.getReference().child("users").child(userId);
        if (mIsDirect) {
            directMessagesReference = userReference.child("direct_messages");
            directMessagesReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String userId = dataSnapshot.getKey();
                    Long timestamp = dataSnapshot.getValue(Long.class);
                    directDiscussions.add(new DirectDiscussion(userId, timestamp));
                    // direct chats should be listed with most recent shown first
                    Collections.sort(directDiscussions);
                    notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    // even though these are different instances, this will remove the object
                    // ArrayList.remove() uses Object.equals() rather than looking for a specific instance
                    String userId = dataSnapshot.getKey();
                    Long timestamp = dataSnapshot.getValue(Long.class);
                    directDiscussions.remove(new DirectDiscussion(userId, timestamp));
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
        DatabaseReference enrollmentsReference = userReference.child("enrollments");
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
        if (mIsDirect) {
            // number of active direct discussions
            return directDiscussions.size();
        } else {
            // number of courses + discussions user participates in
            return discussions.size();
        }
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public String getItem(int i) {
        if (mIsDirect) {
            return directDiscussions.get(i).userId;
        } else {
            return discussions.get(i);
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View cell = view;
        ViewHolder holder = null;
        DirectViewHolder directHolder = null;
        if (cell == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            if (mIsDirect) {
                cell = inflater.inflate(R.layout.connections_list_item, viewGroup, false);
                directHolder = new DirectViewHolder(cell);
                cell.setTag(directHolder);
            } else {
                cell = inflater.inflate(R.layout.chats_list_item, viewGroup, false);
                holder = new ViewHolder(cell);
                cell.setTag(holder);
            }
        } else {
            if (mIsDirect) {
                directHolder = (DirectViewHolder) cell.getTag();
            } else {
                holder = (ViewHolder) cell.getTag();
            }
        }
        String topic;
        if (mIsDirect) {
            topic = directDiscussions.get(i).userId;
            populateDirectViewHolder(directHolder, topic);
        } else {
            topic = discussions.get(i);
            populateViewHolder(holder, topic);
        }

        return cell;
    }

    class ViewHolder {
        @BindView(R.id.chat_name_text_view)
        TextView nameTextView;
        @BindView(R.id.chat_description_text_view)
        TextView descriptionTextView;
        @BindView(R.id.chat_photo_image_view)
        ImageView imageView;
        @BindView(R.id.leave_chat_button)
        ImageButton leaveChatButton;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        public String id;

        @OnClick(R.id.leave_chat_button)
        public void leaveChat() {
            // remove this topic from the user's list
            topicsReference.child(id).removeValue();
        }

    }

    class DirectViewHolder extends ConnectionsListAdapter.ViewHolder {

        public DirectViewHolder(View view) {
            super(view);
            // normally this button contains a message icon, but tapping the list opens the chat
            // instead this button should be supplied as a leave button
            messageButton.setBackgroundResource(R.drawable.delete);
        }

        public String id;

        @OnClick(R.id.message_button)
        public void removeChat() {
            directMessagesReference.child(id).removeValue();
        }

    }

    /**
     * Populate the view with data about the discussion
     * @param viewHolder View to be populated
     * @param topic Name of topic corresponding with this view
     */
    private void populateViewHolder(final ViewHolder viewHolder, String topic) {
        // reference to topic ID to handle leaving chats
        viewHolder.id = topic;
        if (topic.startsWith("nd")) {
            // for chats corresponding with a specific Nanodegree
            // users are disallowed from leaving course specific chats
            viewHolder.leaveChatButton.setVisibility(View.GONE);
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
        } else {
            // custom topics created by users
            // users can leave these chats
            viewHolder.leaveChatButton.setVisibility(View.VISIBLE);
            DatabaseReference nameReference = mFirebaseDatabase.getReference().child("topics").child(topic).child("info").child("name");
            nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    viewHolder.nameTextView.setText(dataSnapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            DatabaseReference creatorReference = mFirebaseDatabase.getReference().child("users").child(topic).child("basic").child("name");
            creatorReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    viewHolder.descriptionTextView.setText(mContext.getString(R.string.discussion_posted_by, dataSnapshot.getValue()));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            DatabaseReference imageReference = mFirebaseDatabase.getReference().child("users").child(topic).child("basic").child("photo");
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

    /**
     * Populate view with data pertaining to a direct chat
     * @param viewHolder view to be populated
     * @param userId user the conversation is with
     */
    private void populateDirectViewHolder(final DirectViewHolder viewHolder, String userId) {
        viewHolder.id = userId;
        DatabaseReference userBasicReference = mFirebaseDatabase.getReference().child("users").child(userId).child("basic");
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
