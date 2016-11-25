package com.owenlarosa.udacians.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.owenlarosa.udacians.R;
import com.owenlarosa.udacians.data.Message;

import org.w3c.dom.Text;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

/**
 * Created by Owen LaRosa on 11/25/16.
 */

public class ProfilePostsAdapter extends BaseAdapter {

    Context mContext;
    // user id of profile to show posts for
    String mUid;

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

    /**
     * Add a post and update the data set
     * @param post Post to be added
     */
    public void addPost(Message post) {
        posts.add(post);
        notifyDataSetChanged();
    }

    /**
     * Delete a post and update the data set
     * @param index Index of the post to be deleted
     */
    public void deletePost(int index) {
        posts.remove(index);
        notifyDataSetChanged();
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
        PostViewHolder holder = null;
        if (cell == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            cell = inflater.inflate(R.layout.display_post_view, viewGroup, false);
            holder = new PostViewHolder(cell);
            cell.setTag(holder);
        } else {
            holder = (PostViewHolder) cell.getTag();
        }
        return cell;
    }

    /**
     * View holder to display the contents of individual posts
     */
    static class PostViewHolder {
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

}
