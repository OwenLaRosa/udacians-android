package com.owenlarosa.udacians;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

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
    @BindView(R.id.edit_first_name_text_field)
    EditText firstNameEditText;
    @BindView(R.id.edit_last_name_text_field)
    EditText lastNameEditText;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @OnClick(R.id.edit_profile_image_button)
    public void chooseImage() {

    }

    @OnClick(R.id.edit_reset_button)
    public void resetChanges() {

    }

}
