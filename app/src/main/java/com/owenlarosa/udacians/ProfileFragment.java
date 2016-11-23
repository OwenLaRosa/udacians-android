package com.owenlarosa.udacians;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.data.BasicProfile;
import com.owenlarosa.udacians.data.Message;
import com.owenlarosa.udacians.data.ProfileInfo;
import com.owenlarosa.udacians.interfaces.MessageDelegate;
import com.owenlarosa.udacians.views.WritePostView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.R.attr.button;

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
    @BindView(R.id.profile_about_text_view)
    TextView aboutTextView;
    @BindView(R.id.profile_links_linear_layout)
    LinearLayout linksLinearLayout;
    @BindView(R.id.profile_write_post_view)
    WritePostView writePostView;
    @BindView(R.id.profile_posts_linear_layout)
    LinearLayout postsLinearLayout;

    private Unbinder mUnbinder;

    private String mUserId;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBasicReference;
    private DatabaseReference mProfileReference;
    private DatabaseReference mPostsReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        // listen for messages to be sent
        writePostView.delegate = this;

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            // attached to profile activity
            mUserId = intent.getStringExtra(EXTRA_USERID);
        } else {
            // attached to main activity
            mUserId = getArguments().getString(EXTRA_USERID);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userReference = mFirebaseDatabase.getReference().child("users").child(mUserId);
        mBasicReference = userReference.child("basic");
        mBasicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BasicProfile profile = dataSnapshot.getValue(BasicProfile.class);
                nameTextView.setText(profile.getName());
                titleTextView.setText(profile.getTitle());
                aboutTextView.setText(profile.getAbout());
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
        mPostsReference = userReference.child("posts");

        return rootView;
    }

    @OnClick(R.id.connect_button)
    public void connectTapped(View view) {

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
        int size = (int) getResources().getDimension(R.dimen.profile_links_height);
        int horizontalSpace = (int) getResources().getDimension(R.dimen.profile_links_horizontal_space);
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
        linksLinearLayout.addView(button);
    }

    @Override
    public void sendMessage(Message message) {
        mPostsReference.push().setValue(message);
    }
}
