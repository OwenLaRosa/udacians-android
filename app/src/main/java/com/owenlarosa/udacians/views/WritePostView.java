package com.owenlarosa.udacians.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.owenlarosa.udacians.R;
import com.owenlarosa.udacians.data.Message;
import com.owenlarosa.udacians.interfaces.MessageDelegate;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/8/16.
 */

public class WritePostView extends RelativeLayout {

    @BindView(R.id.write_post_image_button)
    public ImageButton addImageButton;
    @BindView(R.id.write_post_edit_text)
    public EditText postEditText;
    @BindView(R.id.write_post_image_view)
    public ImageView previewImageView;
    public MessageDelegate delegate;

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

    @OnClick(R.id.create_post_button)
    public void createPost() {
        // create a message/post object
        Message message = new Message();
        message.setSender(FirebaseAuth.getInstance().getCurrentUser().getUid());
        message.setContent(postEditText.getText().toString());

        // alert fragment to send a message
        delegate.sendMessage(message);

        // clear text and image when sending a message
        postEditText.setText("");
        // can't reset an image view, just set its visibility to gone
        previewImageView.setVisibility(View.GONE);
    }
}
