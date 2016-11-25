package com.owenlarosa.udacians.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.owenlarosa.udacians.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/25/16.
 */

public class ProfileView extends LinearLayout {

    @BindView(R.id.profile_about_text_view)
    public TextView aboutTextView;
    @BindView(R.id.profile_links_linear_layout)
    public LinearLayout linksLinearLayout;
    @BindView(R.id.profile_write_post_view)
    public WritePostView writePostView;

    Unbinder unbinder;

    public ProfileView(Context context) {
        super(context);
        setupViews(context);
    }

    public ProfileView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupViews(context);
    }

    public ProfileView(Context context, AttributeSet attributeSet, int defaultStyle) {
        super(context, attributeSet, defaultStyle);
        setupViews(context);
    }

    private void setupViews(Context context) {
        // initialize views from the layout with Butterknife
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.profile_posts_header, this);

        unbinder = ButterKnife.bind(this, rootView);
    }

}
