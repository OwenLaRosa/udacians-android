package com.owenlarosa.udacians;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.adapter.ProfilePostsAdapter;
import com.owenlarosa.udacians.data.BasicProfile;
import com.owenlarosa.udacians.data.Message;
import com.owenlarosa.udacians.data.ProfileInfo;
import com.owenlarosa.udacians.interfaces.MessageDelegate;
import com.owenlarosa.udacians.views.ProfileView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/7/16.
 */

public class ProfileFragment extends Fragment implements MessageDelegate {

    public static final String EXTRA_USERID = "userId";

    @BindView(R.id.profile_image_view)
    ImageView profilePictureImageView;
    @BindView(R.id.profile_name_text_view)
    TextView nameTextView;
    @BindView(R.id.profile_title_text_view)
    TextView titleTextView;
    @BindView(R.id.connect_button)
    FloatingActionButton connectButton;
    @BindView(R.id.posts_list_view)
    ListView postsListView;
    ProfileView headerView;

    private Unbinder mUnbinder;

    private String mUserId;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBasicReference;
    private DatabaseReference mProfileReference;
    private DatabaseReference mPostsReference;
    private DatabaseReference mIsConnectionReference;

    // whether or not this user is a connection
    private boolean mIsConnection = false;

    // ensures resources can be accessed even if not attached to activity
    private Resources mResources;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            // attached to profile activity
            mUserId = intent.getStringExtra(EXTRA_USERID);
        } else {
            // attached to main activity
            mUserId = getArguments().getString(EXTRA_USERID);
        }
        mResources = getResources();

        final ProfilePostsAdapter postsAdapter = new ProfilePostsAdapter(getActivity(), mUserId);
        postsListView.setAdapter(postsAdapter);
        headerView = new ProfileView(getActivity());
        postsListView.addHeaderView(headerView);
        headerView.writePostView.delegate = this;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userReference = mFirebaseDatabase.getReference().child("users").child(mUserId);
        mBasicReference = userReference.child("basic");
        mBasicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BasicProfile profile = dataSnapshot.getValue(BasicProfile.class);
                nameTextView.setText(profile.getName());
                titleTextView.setText(profile.getTitle());
                headerView.aboutTextView.setText(profile.getAbout());
                Glide.with(getActivity())
                        .load(profile.getPhoto())
                        .into(profilePictureImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProfileReference = userReference.child("profile");
        mProfileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                // check if links of each type exist, and if so add them to the layout
                if (profileInfo.getSite() != null && !profileInfo.getSite().equals("")) {
                    addLinkButton(LinkType.Personal, profileInfo.getSite());
                }
                if (profileInfo.getBlog() != null && !profileInfo.getBlog().equals("")) {
                    addLinkButton(LinkType.Blog, profileInfo.getBlog());
                }
                if (profileInfo.getLinkedin() != null && !profileInfo.getLinkedin().equals("")) {
                    addLinkButton(LinkType.Linkedin, profileInfo.getLinkedin());
                }
                if (profileInfo.getTwitter() != null && !profileInfo.getTwitter().equals("")) {
                    addLinkButton(LinkType.Twitter, profileInfo.getTwitter());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // used to write new posts, reading posts is handled by the adapter
        mPostsReference = userReference.child("posts");
        // connections stored under user currently signed into the app
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mIsConnectionReference = mFirebaseDatabase.getReference().child("users").child(user).child("connections").child(mUserId);
        mIsConnectionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    // connection is added, show option to remove
                    mIsConnection = true;
                    connectButton.setImageResource(R.drawable.remove_connection);
                    connectButton.setBackgroundTintList(ColorStateList.valueOf(mResources.getColor(R.color.colorRemove)));
                } else {
                    // not a connection yet, show option to add
                    mIsConnection = false;
                    connectButton.setImageResource(R.drawable.add_connection);
                    connectButton.setBackgroundTintList(ColorStateList.valueOf(mResources.getColor(R.color.colorAccent)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return rootView;
    }

    @OnClick(R.id.connect_button)
    public void connectTapped(View view) {
        if (mIsConnection) {
            // remove the connection
            mIsConnectionReference.removeValue();
        } else {
            // add the connection
            mIsConnectionReference.setValue(true);
        }
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
     */
    private void addLinkButton(LinkType type, final String link) {
        // configure the button's size and margins
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int size = (int) mResources.getDimension(R.dimen.profile_links_height);
        int horizontalSpace = (int) mResources.getDimension(R.dimen.profile_links_horizontal_space);
        layoutParams.setMargins(0, 0, horizontalSpace, 0);
        layoutParams.setMarginEnd(horizontalSpace);
        layoutParams.width = size;
        layoutParams.height = size;
        // create a button with these layout parameters
        ImageButton button = new ImageButton(getActivity());
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
                startActivity(intent);
            }
        });
        // display the button onscreen
        headerView.linksLinearLayout.addView(button);
    }

    @Override
    public void sendMessage(Message message) {
        // mapped form will correctly allow the server to generate the timestamp
        mPostsReference.push().setValue(message.toMap());
    }
}
