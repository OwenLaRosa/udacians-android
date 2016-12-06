package com.owenlarosa.udacians.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.owenlarosa.udacians.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/11/16.
 */

public class ChatInputView extends LinearLayout {

    @BindView(R.id.chat_input_text_field)
    public EditText messageTextField;
    @BindView(R.id.chat_input_add_image_button)
    public ImageButton pickImageButton;
    @BindView(R.id.chat_input_send_button)
    public Button sendButton;

    private Unbinder unbinder;

    public ChatInputView(Context context) {
        super(context);
        setupViews(context);
    }

    public ChatInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupViews(context);
    }

    public ChatInputView(Context context, AttributeSet attributeSet, int defaultStyle) {
        super(context, attributeSet, defaultStyle);
        setupViews(context);
    }

    private void setupViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.chat_input_view, this);

        unbinder = ButterKnife.bind(this, rootView);
    }

}
