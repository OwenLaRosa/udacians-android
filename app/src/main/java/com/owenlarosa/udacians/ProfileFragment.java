package com.owenlarosa.udacians;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.owenlarosa.udacians.views.WritePostView;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/7/16.
 */

public class ProfileFragment extends Fragment {

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @OnClick(R.id.connect_button)
    public void connectTapped(View view) {

    }
}
