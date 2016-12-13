package com.owenlarosa.udacians.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.owenlarosa.udacians.R;

import java.util.HashMap;

/**
 * Created by Owen LaRosa on 12/11/16.
 */

public class MultipleInputView extends Dialog {

    private static class Keys {
        private static final String NAME = "name";
        private static final String TITLE = "title";
        private static final String URL = "url";
        private static final String PLACE = "place";
        private static final String ABOUT = "about";
    }

    private Context mContext;
    private Type mType;
    // page/prompt screen the user is currently on, starts counting at 0
    private int pageIndex = 0;
    //
    private HashMap<String, String> contents = new HashMap<String, String>();

    // general title, signifies posting a topic, article, or an event
    private TextView titleTextView;
    // prompt for specific info about the item
    private TextView subtitleTextView;
    // input area, includes hints for possible input
    private EditText inputEditText;
    // goes to previous screen or cancels input
    private Button backButton;
    // goes to next screen or finishes input
    private Button nextButton;

    // reusable click listeners for the back and next buttons

    // navigate to the next page
    private View.OnClickListener nextPageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateContents();
            pageIndex++;
            loadInputPage();
        }
    };

    // navigate to the previous page
    private View.OnClickListener previousPageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateContents();
            pageIndex--;
            loadInputPage();
        }
    };

    // cancel input
    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    };

    // submit input
    private View.OnClickListener submitClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    public enum Type {
        Topic, Article, Event;
    }

    public MultipleInputView(Context context, Type type) {
        super(context);
        mContext = context;
        mType = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadInputPage();
        // dialog should be fullscreen, has translucent margins
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
    }

    private void loadInputPage() {
        // refresh the content view and assign subviews
        setContentView(R.layout.add_new_view);
        titleTextView = (TextView) findViewById(R.id.input_title_text_view);
        subtitleTextView = (TextView) findViewById(R.id.input_subtitle_text_view);
        inputEditText = (EditText) findViewById(R.id.input_edit_text);
        backButton = (Button) findViewById(R.id.input_back_button);
        nextButton = (Button) findViewById(R.id.input_next_button);
        // unless otherwise specified, all text input is single line
        inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        switch (mType) {
            case Topic:
                titleTextView.setText(R.string.input_topic_title);
                if (pageIndex == 0) {
                    // discussion prompt
                    subtitleTextView.setText(R.string.input_topic_p1_subtitle);
                    inputEditText.setHint(R.string.input_topic_p1_hint);
                    backButton.setText(mContext.getString(R.string.input_cancel));
                    backButton.setOnClickListener(cancelClickListener);
                    nextButton.setText(mContext.getString(R.string.input_submit));
                    nextButton.setOnClickListener(submitClickListener);
                }
                break;
            case Article:
                titleTextView.setText(R.string.input_article_title);
                if (pageIndex == 0) {
                    // article title
                    subtitleTextView.setText(R.string.input_article_p1_subtitle);
                    inputEditText.setHint(R.string.input_article_p1_hint);
                    backButton.setText(mContext.getString(R.string.input_cancel));
                    backButton.setOnClickListener(cancelClickListener);
                    nextButton.setText(mContext.getString(R.string.input_next));
                    nextButton.setOnClickListener(nextPageClickListener);
                } else if (pageIndex == 1) {
                    // article URL
                    subtitleTextView.setText(R.string.input_article_p2_subtitle);
                    inputEditText.setHint(R.string.input_article_p2_hint);
                    backButton.setText(mContext.getString(R.string.input_back));
                    backButton.setOnClickListener(previousPageClickListener);
                    nextButton.setText(mContext.getString(R.string.input_submit));
                    nextButton.setOnClickListener(submitClickListener);
                }
                break;
            case Event:
                titleTextView.setText(R.string.input_event_title);
                if (pageIndex == 0) {
                    // event name
                    subtitleTextView.setText(R.string.input_event_p1_subtitle);
                    inputEditText.setHint(R.string.input_event_p1_hint);
                    backButton.setText(mContext.getString(R.string.input_cancel));
                    backButton.setOnClickListener(cancelClickListener);
                    nextButton.setText(mContext.getString(R.string.input_next));
                    nextButton.setOnClickListener(nextPageClickListener);
                } else if (pageIndex == 1) {
                    // event location
                    subtitleTextView.setText(R.string.input_event_p2_subtitle);
                    inputEditText.setHint(R.string.input_event_p2_hint);
                    backButton.setText(mContext.getString(R.string.input_back));
                    backButton.setOnClickListener(previousPageClickListener);
                    nextButton.setText(mContext.getString(R.string.input_next));
                    nextButton.setOnClickListener(nextPageClickListener);
                } else if (pageIndex == 2) {
                    // description of the event
                    subtitleTextView.setText(R.string.input_event_p3_subtitle);
                    inputEditText.setHint(R.string.input_event_p3_hint);
                    // event details should allow multi-line input
                    inputEditText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    backButton.setText(mContext.getString(R.string.input_back));
                    backButton.setOnClickListener(previousPageClickListener);
                    nextButton.setText(mContext.getString(R.string.input_submit));
                    nextButton.setOnClickListener(submitClickListener);
                }
                break;
        }
    }

    private void updateContents() {
        String input = inputEditText.getText().toString();
        switch (mType) {
            case Topic:
                contents.put(Keys.NAME, input);
                break;
            case Article:
                if (pageIndex == 0) {
                    contents.put(Keys.TITLE, input);
                } else if (pageIndex == 1) {
                    contents.put(Keys.URL, input);
                }
                break;
            case Event:
                if (pageIndex == 0) {
                    contents.put(Keys.NAME, input);
                } else if (pageIndex == 1) {
                    contents.put(Keys.PLACE, input);
                } else if (pageIndex == 2) {
                    contents.put(Keys.ABOUT, input);
                }
                break;
        }
    }

}
