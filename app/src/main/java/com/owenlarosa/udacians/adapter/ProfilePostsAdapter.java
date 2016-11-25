package com.owenlarosa.udacians.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.R;
import com.owenlarosa.udacians.data.Message;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 11/25/16.
 */

public class ProfilePostsAdapter extends BaseAdapter {

    private Context mContext;
    // user id of profile to show posts for
    private String mUid;

    // posts to be displayed in list view
    private ArrayList<Message> posts = new ArrayList<Message>();

    // References to Firebase Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference postsReference;

    public ProfilePostsAdapter(Context context, String userId) {
        mContext = context;
        mUid = userId;

        // set up the firebase references
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        postsReference = mFirebaseDatabase.getReference().child("users").child(mUid).child("posts");
        postsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message post = dataSnapshot.getValue(Message.class);
                posts.add(post);
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
        Message post = posts.get(i);
        populatePostViewHolder(holder, post);

        return cell;
    }

    /**
     * View holder to display the contents of individual posts
     */
    class PostViewHolder {
        @BindView(R.id.display_post_profile_image_view)
        ImageView imageView;
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
        }
    }

    /**
     * Fill in the contents of the view holder
     * @param viewHolder view to display the post
     * @param post post data to display in the view
     */
    private void populatePostViewHolder(final PostViewHolder viewHolder, Message post) {
        // content is directly in the message object
        viewHolder.contentTextView.setText(post.getContent());
        // other data is associate with the user
        // download it separately if it hasn't been already
        DatabaseReference basicReference = mFirebaseDatabase.getReference().child("users").child(post.getSender()).child("basic");
        DatabaseReference nameReference = basicReference.child("name");
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
        DatabaseReference photoReference = basicReference.child("photo");
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
        // users can delete all posts on their profile
        // they can also delete posts they authored on a different profile
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (user.equals(post.getSender()) || user.equals(mUid)) {
            viewHolder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            viewHolder.deleteButton.setVisibility(View.INVISIBLE);
        }
    }
}
