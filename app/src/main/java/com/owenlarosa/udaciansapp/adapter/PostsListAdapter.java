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
import com.owenlarosa.udaciansapp.Keys;
import com.owenlarosa.udaciansapp.ProfileActivity;
import com.owenlarosa.udaciansapp.ProfileFragment;
import com.owenlarosa.udaciansapp.R;
import com.owenlarosa.udaciansapp.Utils;
import com.owenlarosa.udaciansapp.data.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Owen LaRosa on 11/25/16.
 */

public class PostsListAdapter extends BaseAdapter {

    public enum PostsType {
        Person,
        Event,
    }

    private PostsType mType;

    private Context mContext;
    // user id of profile to show posts for
    private String mUid;

    // posts to be displayed in list view
    private ArrayList<Message> posts = new ArrayList<Message>();
    // maps post keys to message objects for later retrieval
    // this is mainly for deleting posts from the array list
    private HashMap<String, Message> iDtoMessage = new HashMap<String, Message>();

    // References to Firebase Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference postLinksReference;
    private DatabaseReference postsReference;

    public PostsListAdapter(Context context, String userId, final PostsType type) {
        mContext = context;
        mUid = userId;

        mType = type;

        // adapter can be used for posts on user profiles or events
        // because these are at different paths in the DB, the root node is different
        String root = "";
        switch (type) {
            case Person:
                root = Keys.USERS;
                break;
            case Event:
                root = Keys.EVENTS;
                break;
        }

        // set up the firebase references
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        postsReference = mFirebaseDatabase.getReference().child(Keys.POSTS);
        postLinksReference = mFirebaseDatabase.getReference().child(root).child(mUid).child(Keys.POSTS);
        postLinksReference.limitToLast(10).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (type == PostsType.Event) {
                    // message objects for posts in events are stored directly in a child of the event
                    Message post = dataSnapshot.getValue(Message.class);
                    String id = dataSnapshot.getKey();
                    iDtoMessage.put(id, post);
                    posts.add(post);
                    post.setId(id);
                    notifyDataSetChanged();
                } else {
                    // message objects for posts on user profiles are all stored in the "posts" child
                    // they need to be referenced separately by the message ID
                    String messageId = dataSnapshot.getKey();
                    final Long timestamp = dataSnapshot.getValue(Long.class);
                    postsReference.child(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Message post = dataSnapshot.getValue(Message.class);
                            // profile posts from this reference do not always contain a date
                            // the timestamp is the value of a child of postLinksReference
                            post.setDate(timestamp);
                            String id = dataSnapshot.getKey();
                            iDtoMessage.put(id, post);
                            posts.add(post);
                            post.setId(id);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String id = dataSnapshot.getKey();
                Message post = iDtoMessage.get(id);
                iDtoMessage.remove(id);
                posts.remove(post);
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
        return posts.size();
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
        // posts should be reversed with newest on top
        Message post = posts.get(posts.size() - 1 - i);
        populatePostViewHolder(holder, post);

        return cell;
    }

    /**
     * View holder to display the contents of individual posts
     */
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

        // Firebase key for the post data
        public String id;

        PostViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.display_post_delete_button)
        public void deletePost() {
            postLinksReference.child(id).removeValue();
        }

    }

    /**
     * Fill in the contents of the view holder
     * @param viewHolder view to display the post
     * @param post post data to display in the view
     */
    private void populatePostViewHolder(final PostViewHolder viewHolder, final Message post) {
        // id used to reference post for deletion
        viewHolder.id = post.getId();
        // content is directly in the message object
        viewHolder.contentTextView.setText(post.getContent());
        // posts should only link to profiles on events
        // if on a user's profile, don't link as this would be redundant
        if (mType == PostsType.Event){
            viewHolder.profileImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.putExtra(ProfileFragment.EXTRA_USERID, post.getSender());
                    mContext.startActivity(intent);
                    ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
        }
        // other data is associate with the user
        // download it separately if it hasn't been already
        DatabaseReference basicReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(post.getSender()).child(Keys.BASIC);
        DatabaseReference nameReference = basicReference.child(Keys.NAME);
        nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                viewHolder.nameTextView.setText(name);
                viewHolder.profileImageButton.setContentDescription(name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference photoReference = basicReference.child(Keys.PHOTO);
        photoReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
        // show image if one is contained in the post
        if (post.getImageUrl() != null) {
            if (post.getContent() == null || post.getContent().equals("")) {
                // post does not have any text to show
                viewHolder.contentTextView.setVisibility(View.GONE);
            } else {
                viewHolder.contentTextView.setVisibility(View.VISIBLE);
            }
            viewHolder.contentImageView.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(post.getImageUrl())
                    .into(viewHolder.contentImageView);
        } else {
            // make sure the right content is visible
            // without this, sometimes an image view is shown and a text view is not
            viewHolder.contentImageView.setVisibility(View.GONE);
            viewHolder.contentTextView.setVisibility(View.VISIBLE);
        }
        // users can delete all posts on their profile
        // they can also delete posts they authored on a different profile
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (user.equals(post.getSender()) || user.equals(mUid)) {
            viewHolder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            viewHolder.deleteButton.setVisibility(View.GONE);
        }
        Date date = new Date(post.getDate());
        String formattedTime = Utils.formatTime(date);
        viewHolder.timeTextView.setText(formattedTime);
    }
}
