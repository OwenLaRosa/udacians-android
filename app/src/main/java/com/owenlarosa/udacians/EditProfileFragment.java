package com.owenlarosa.udacians;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.data.BasicProfile;
import com.owenlarosa.udacians.data.ProfileInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/16/16.
 */

public class EditProfileFragment extends Fragment {

    @BindView(R.id.edit_profile_image_button)
    ImageButton profileImageButton;
    @BindView(R.id.edit_title_text_field)
    EditText titleEditText;
    @BindView(R.id.edit_about_text_field)
    EditText aboutEditText;
    @BindView(R.id.edit_site_text_field)
    EditText siteEditText;
    @BindView(R.id.edit_blog_text_field)
    EditText blogEditText;
    @BindView(R.id.edit_linkedin_text_field)
    EditText linkedinEditText;
    @BindView(R.id.edit_twitter_text_field)
    EditText twitterEditText;

    Unbinder mUnbinder;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mBasicReference;
    DatabaseReference mProfileReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userReference = mFirebaseDatabase.getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mBasicReference = userReference.child("basic");
        mProfileReference = userReference.child("profile");

        if (savedInstanceState == null) {
            // prefill the data for the first launch
            loadData();
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isFinishing()) {
            // data should only be saved when activity is permantly closed
            // it should not be saved for other lifecycle events such as rotation
            saveChanges();
        }
    }

    @OnClick(R.id.edit_profile_image_button)
    public void chooseImage() {

    }

    @OnClick(R.id.edit_reset_button)
    public void resetChanges() {
        loadData();
    }

    /**
     * Populate text fields with current profile data
     */
    private void loadData() {
        mBasicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BasicProfile basicProfile = dataSnapshot.getValue(BasicProfile.class);
                if (basicProfile != null) {
                    getActivity().setTitle(basicProfile.getName());
                    titleEditText.setText(basicProfile.getTitle() != null ? basicProfile.getTitle() : "");
                    aboutEditText.setText(basicProfile.getAbout() != null ? basicProfile.getAbout() : "");
                }
                titleEditText.setEnabled(true);
                aboutEditText.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProfileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    siteEditText.setText(profileInfo.getSite() != null ? profileInfo.getSite() : "");
                    blogEditText.setText(profileInfo.getBlog() != null ? profileInfo.getBlog() : "");
                    linkedinEditText.setText(profileInfo.getLinkedin() != null ? profileInfo.getLinkedin() : "");
                    twitterEditText.setText(profileInfo.getTwitter() != null ? profileInfo.getTwitter() : "");
                }
                siteEditText.setEnabled(true);
                blogEditText.setEnabled(true);
                linkedinEditText.setEnabled(true);
                twitterEditText.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Upload new profile data to the server
     */
    private void saveChanges() {
        mBasicReference.child("title").setValue(titleEditText.getText().toString());
        mBasicReference.child("about").setValue(aboutEditText.getText().toString());
        mProfileReference.child("site").setValue(siteEditText.getText().toString());
        mProfileReference.child("blog").setValue(blogEditText.getText().toString());
        mProfileReference.child("linkedin").setValue(linkedinEditText.getText().toString());
        mProfileReference.child("twitter").setValue(twitterEditText.getText().toString());
    }

}
