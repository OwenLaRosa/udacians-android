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
import android.widget.ListView;
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
import com.owenlarosa.udacians.adapter.ProfilePostsAdapter;
import com.owenlarosa.udacians.data.BasicProfile;
import com.owenlarosa.udacians.data.Message;
import com.owenlarosa.udacians.data.ProfileInfo;
import com.owenlarosa.udacians.interfaces.MessageDelegate;
import com.owenlarosa.udacians.views.DisplayPostView;
import com.owenlarosa.udacians.views.WritePostView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.R.attr.button;

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
    @BindView(R.id.posts_list_view)
    ListView postsListView;

    private Unbinder mUnbinder;

    private String mUserId;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBasicReference;

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

        final ProfilePostsAdapter postsAdapter = new ProfilePostsAdapter(getActivity(), mUserId);
        postsListView.setAdapter(postsAdapter);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userReference = mFirebaseDatabase.getReference().child("users").child(mUserId);
        mBasicReference = userReference.child("basic");
        mBasicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BasicProfile profile = dataSnapshot.getValue(BasicProfile.class);
                nameTextView.setText(profile.getName());
                titleTextView.setText(profile.getTitle());
                postsAdapter.setAbout(profile.getAbout());
                Glide.with(getActivity())
                        .load(profile.getPhoto())
                        .into(profilePictureImageView);
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
