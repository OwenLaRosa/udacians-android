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
import com.owenlarosa.udaciansapp.ProfileActivity;
import com.owenlarosa.udaciansapp.ProfileFragment;
import com.owenlarosa.udaciansapp.R;
import com.owenlarosa.udaciansapp.data.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 1/1/17.
 */

public class PostFeedAdapter extends BaseAdapter {

    private Context mContext;
    private String mUserId;

    private ArrayList<String> connections = new ArrayList<>();
    private ArrayList<PostLink> postLinks = new ArrayList<>();

    FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    private class PostLink {

        String identifier;
        long timestamp;

        public PostLink(String identifier, long timestamp) {
            this.identifier = identifier;
            this.timestamp = timestamp;
        }
    }

    public PostFeedAdapter(Context context, String userId) {
        mContext = context;
        mUserId = userId;

        DatabaseReference connectionsReference = mFirebaseDatabase.getReference().child("users").child(mUserId).child("connections");
        connectionsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot connection: dataSnapshot.getChildren()) {
                    connections.add(connection.getKey());
                }
                getConnectionsPostLinks();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getCount() {
        return postLinks.size();
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
        int viewType = getItemViewType(i);
        PostViewHolder holder = null;
        if (cell == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            cell = inflater.inflate(R.layout.display_post_view, viewGroup, false);
            holder = new PostViewHolder(cell);
            cell.setTag(holder);
        } else {
            holder = (PostViewHolder) cell.getTag();
        }
        PostLink postLink = postLinks.get(i);
        populateViewHolder(holder, postLink);

        return cell;
    }

    class PostViewHolder {
        @BindView(R.id.display_post_profile_image_button)
        ImageButton profileImageButton;
        @BindView(R.id.display_post_delete_button)
        ImageButton deleteButton;
        @BindView(R.id.display_post_name_text_view)
        TextView nameTextView;
        @BindView(R.id.display_post_content_text_view)
        TextView contentTextView;
        @BindView(R.id.display_post_content_image_view)
        ImageView contentImageView;
        @BindView(R.id.display_post_time_text_view)
        TextView timeTextView;

        PostViewHolder(View view) {
            ButterKnife.bind(this, view);
            deleteButton.setVisibility(View.GONE);
        }

    }


    /**
     * Display a user's post into the view
     * @param viewHolder view to be populated
     * @param postLink reference to the post to display
     */
    private void populateViewHolder(final PostViewHolder viewHolder, PostLink postLink) {
        Date date = new Date(postLink.timestamp);
        String formattedTime = new SimpleDateFormat("H:mm").format(date);
        viewHolder.timeTextView.setText(formattedTime);
        DatabaseReference postReference = mFirebaseDatabase.getReference().child("posts").child(postLink.identifier);
        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Message post = dataSnapshot.getValue(Message.class);
                if (post.getContent() == null || post.getContent().equals("")) {
                    viewHolder.contentTextView.setVisibility(View.GONE);
                } else {
                    viewHolder.contentTextView.setText(post.getContent());
                    viewHolder.contentTextView.setVisibility(View.VISIBLE);
                }
                if (post.getImageUrl() == null || post.getImageUrl().equals("")) {
                    viewHolder.contentImageView.setVisibility(View.GONE);
                } else {
                    Glide.with(mContext)
                            .load(post.getImageUrl())
                            .into(viewHolder.contentImageView);
                    viewHolder.contentImageView.setVisibility(View.VISIBLE);
                }
                if (!(post.getSender() == null) || !(post.getSender().equals(""))) {
                    viewHolder.profileImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(ProfileFragment.EXTRA_USERID, post.getSender());
                            mContext.startActivity(intent);
                        }
                    });
                }
                DatabaseReference nameReference = mFirebaseDatabase.getReference().child("users").child(post.getSender()).child("basic").child("name");
                nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.nameTextView.setText(dataSnapshot.getValue(String.class));
                        viewHolder.profileImageButton.setContentDescription(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                DatabaseReference profileImageReference = mFirebaseDatabase.getReference().child("users").child(post.getSender()).child("basic").child("photo");
                profileImageReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String photoUrl = dataSnapshot.getValue(String.class);
                        Glide.with(mContext)
                                .load(photoUrl)
                                .into(viewHolder.profileImageButton);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Get all posts from a user's connections
     * connections arraylist should be populated before calling
     */
    private void getConnectionsPostLinks() {
        for (String user: connections) {
            DatabaseReference userPostLinksReference = mFirebaseDatabase.getReference().child("users").child(user).child("posts");
            userPostLinksReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    PostLink postLink = new PostLink(dataSnapshot.getKey(), dataSnapshot.getValue(Long.class));
                    postLinks.add(postLink);
                    Collections.sort(postLinks, new Comparator<PostLink>() {
                        @Override
                        public int compare(PostLink t1, PostLink t2) {
                            return (int) (t2.timestamp - t1.timestamp);
                        }
                    });
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
    }

}
