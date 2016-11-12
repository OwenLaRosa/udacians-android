package com.owenlarosa.udacians.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.owenlarosa.udacians.R;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/11/16.
 */

public class MessageView extends RelativeLayout {

    @BindView(R.id.message_profile_image_view)
    ImageView profileImageView;
    @BindView(R.id.message_name_text_view)
    TextView nameTextView;
    @BindView(R.id.message_time_text_view)
    TextView timeTextView;
    @BindView(R.id.message_content_text_view)
    TextView contentTextView;
    @BindView(R.id.message_content_image_view)
    ImageView contentImageView;

    Unbinder unbinder;

    public MessageView(Context context) {
        super(context);
        setupViews(context);
    }

    public MessageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupViews(context);
    }

    public MessageView(Context context, AttributeSet attributeSet, int defaultStyle) {
        super(context, attributeSet, defaultStyle);
        setupViews(context);
    }

    private void setupViews(Context context) {
        // initialize views from the layout with Butterknife
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.message_view, this);

        unbinder = ButterKnife.bind(this, rootView);
    }

}
