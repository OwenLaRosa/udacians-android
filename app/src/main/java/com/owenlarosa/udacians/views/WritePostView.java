package com.owenlarosa.udacians.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.owenlarosa.udacians.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/8/16.
 */

public class WritePostView extends RelativeLayout {

    @BindView(R.id.write_post_image_button)
    ImageButton addImageButton;
    @BindView(R.id.write_post_edit_text)
    EditText postEditText;
    @BindView(R.id.write_post_image_view)
    ImageView previewImageView;

    Unbinder unbinder;

    public WritePostView(Context context) {
        super(context);
        setupViews(context);
    }

    public WritePostView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupViews(context);
    }

    public WritePostView(Context context, AttributeSet attributeSet, int defaultStyle) {
        super(context, attributeSet, defaultStyle);
        setupViews(context);
    }

    private void setupViews(Context context) {
        // initialize views from the layout with Butterknife
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.write_post_view, this);

        unbinder = ButterKnife.bind(this, rootView);
    }

}
