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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.ProfileFragment;
import com.owenlarosa.udacians.R;
import com.owenlarosa.udacians.data.Message;
import com.owenlarosa.udacians.data.ProfileInfo;
import com.owenlarosa.udacians.interfaces.MessageDelegate;
import com.owenlarosa.udacians.views.WritePostView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.owenlarosa.udacians.R.string.post;

/**
 * Created by Owen LaRosa on 11/25/16.
 */

public class ProfilePostsAdapter extends BaseAdapter {

    // identifiers for view types used by the adapter
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_POST = 1;

    private Context mContext;
    // user id of profile to show posts for
    private String mUid;

    /**
     * "About" text of the user's profile
     */
    private String about = "";

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
        // should refresh the header view with new "about" text
        notifyDataSetChanged();
    }

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
        // all posts plus a header view
        return posts.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    // 2 methods used so the adapter works with multiple different view types

    @Override
    public int getViewTypeCount() {
        // header view, view for displaying posts
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // first view is header, all others show posts
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_POST;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View cell = view;
        int viewType = getItemViewType(i);
        if (viewType == 0) {
            HeaderViewHolder holder = null;
            if (cell == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                cell = inflater.inflate(R.layout.profile_posts_header, viewGroup, false);
                holder = new HeaderViewHolder(cell);
                cell.setTag(holder);
            } else {
                holder = (HeaderViewHolder) cell.getTag();
            }
            populateHeaderViewHolder(holder);
        } else {
            PostViewHolder holder = null;
            if (cell == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                cell = inflater.inflate(R.layout.display_post_view, viewGroup, false);
                holder = new PostViewHolder(cell);
                cell.setTag(holder);
            } else {
                holder = (PostViewHolder) cell.getTag();
            }
            // i = 1 is the first post, since the view at index 0 is the header
            Message post = posts.get(i - 1);
            populatePostViewHolder(holder, post);
        }

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

    /**
     * Shows basic profile info
     * About section and links to personal sites
     */
    class HeaderViewHolder implements MessageDelegate {
        @BindView(R.id.profile_about_text_view)
        TextView aboutTextView;
        @BindView(R.id.profile_links_linear_layout)
        LinearLayout linksLinearLayout;
        @BindView(R.id.profile_write_post_view)
        WritePostView writePostView;

        HeaderViewHolder(View view) {
            ButterKnife.bind(this, view);
            // view has a callback to let this class know about posting messages
            writePostView.delegate = this;
        }

        // alerted by writePostView, upload the post
        @Override
        public void sendMessage(Message message) {
            // mapped form will correctly allow the server to generate the timestamp
            postsReference.push().setValue(message.toMap());
        }
    }

    /**
     * Fill contents of the header view
     * @param viewHolder view that contains the contents
     */
    private void populateHeaderViewHolder(final HeaderViewHolder viewHolder) {
        viewHolder.aboutTextView.setText(about);
        DatabaseReference profileReference = mFirebaseDatabase.getReference().child("users").child(mUid).child("basic");
        profileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                // check if links of each type exist, and if so add them to the layout
                if (profileInfo.getSite() != null && !profileInfo.getSite().equals("")) {
                    ImageButton button = addLinkButton(LinkType.Personal, profileInfo.getSite());
                    viewHolder.linksLinearLayout.addView(button);
                }
                if (profileInfo.getBlog() != null && !profileInfo.getBlog().equals("")) {
                    ImageButton button = addLinkButton(LinkType.Blog, profileInfo.getBlog());
                    viewHolder.linksLinearLayout.addView(button);
                }
                if (profileInfo.getLinkedin() != null && !profileInfo.getLinkedin().equals("")) {
                    ImageButton button = addLinkButton(LinkType.Linkedin, profileInfo.getLinkedin());
                    viewHolder.linksLinearLayout.addView(button);
                }
                if (profileInfo.getTwitter() != null && !profileInfo.getTwitter().equals("")) {
                    ImageButton button = addLinkButton(LinkType.Twitter, profileInfo.getTwitter());
                    viewHolder.linksLinearLayout.addView(button);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // types of eligible links for image buttons
    enum LinkType {
        Personal,
        Blog,
        Linkedin,
        Twitter
    }

    /**
     * Create and add a button that opens a link
     * @param type Type of link, used to determine the image
     * @param link Url to open in browser when tapped
     * @return ImageButton with the specified properties
     */
    private ImageButton createLinkButton(LinkType type, final String link) {
        // configure the button's size and margins
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int size = (int) mContext.getResources().getDimension(R.dimen.profile_links_height);
        int horizontalSpace = (int) mContext.getResources().getDimension(R.dimen.profile_links_horizontal_space);
        layoutParams.setMargins(0, 0, horizontalSpace, 0);
        layoutParams.setMarginEnd(horizontalSpace);
        layoutParams.width = size;
        layoutParams.height = size;
        // create a button with these layout parameters
        ImageButton button = new ImageButton(mContext);
        button.setLayoutParams(layoutParams);
        // determine the correct icon to display
        switch (type) {
            case Personal:
                button.setBackgroundResource(R.drawable.personal_site);
                break;
            case Blog:
                button.setBackgroundResource(R.drawable.blog);
                break;
            case Linkedin:
                button.setBackgroundResource(R.drawable.linkedin);
                break;
            case Twitter:
                button.setBackgroundResource(R.drawable.twitter);
                break;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // convert to Uri for use with intent
                Uri linkUri = Uri.parse(link);
                // open the link in the browser
                Intent intent = new Intent(Intent.ACTION_VIEW, linkUri);
                mContext.startActivity(intent);
            }
        });
        return button;
    }

}
