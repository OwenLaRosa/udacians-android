package com.owenlarosa.udacians.views;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.owenlarosa.udacians.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/8/16.
 */

public class DisplayPostView extends RelativeLayout {

    @BindView(R.id.display_post_profile_image_view)
    ImageView profileImageView;
    @BindView(R.id.profile_name_text_view)
    TextView nameTextView;
    @BindView(R.id.display_post_time_text_view)
    TextView timeTextView;
    @BindView(R.id.display_post_content_text_view)
    TextView contentTextView;
    @BindView(R.id.display_post_content_image_view)
    ImageView contentImageView;

    Unbinder unbinder;

    public DisplayPostView(Context context) {
        super(context);
        setupViews(context);
    }

    public DisplayPostView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupViews(context);
    }

    public DisplayPostView(Context context, AttributeSet attributeSet, int defaultStyle) {
        super(context, attributeSet, defaultStyle);
        setupViews(context);
    }

    private void setupViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.display_post_view, this);

        unbinder = ButterKnife.bind(this, rootView);
    }

    @OnClick(R.id.display_post_delete_button)
    public void deletePost(View view) {
        // delete the post, only on user's wall
    }

}
