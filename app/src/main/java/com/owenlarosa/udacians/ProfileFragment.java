package com.owenlarosa.udacians;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.data.BasicProfile;
import com.owenlarosa.udacians.views.WritePostView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.R.attr.name;

/**
 * Created by Owen LaRosa on 11/7/16.
 */

public class ProfileFragment extends Fragment {

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

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            // attached to profile activity
            mUserId = intent.getStringExtra(EXTRA_USERID);
        } else {
            // attached to main activity
            mUserId = getArguments().getString(EXTRA_USERID);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mBasicReference = mFirebaseDatabase.getReference().child("users").child(mUserId).child("basic");
        mBasicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BasicProfile profile = dataSnapshot.getValue(BasicProfile.class);
                nameTextView.setText(profile.getName());
                titleTextView.setText(profile.getTitle());
                aboutTextView.setText(profile.getAbout());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return rootView;
    }

    @OnClick(R.id.connect_button)
    public void connectTapped(View view) {

    }
}
