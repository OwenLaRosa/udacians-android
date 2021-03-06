package com.owenlarosa.udaciansapp.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.owenlarosa.udaciansapp.Keys;
import com.owenlarosa.udaciansapp.R;
import com.owenlarosa.udaciansapp.Utils;

import java.util.HashMap;

/**
 * Created by Owen LaRosa on 12/11/16.
 */

public class MultipleInputView extends Dialog {

    private static class InfoKeys {
        private static final String NAME = "name";
        private static final String TITLE = "title";
        private static final String URL = "url";
        private static final String PLACE = "place";
        private static final String ABOUT = "about";
        private static final String LONGITUDE = "longitude";
        private static final String LATITUDE = "latitude";
        private static final String TIMESTAMP = "timestamp";
    }

    private Context mContext;
    private Type mType;
    // page/prompt screen the user is currently on, starts counting at 0
    private int pageIndex = 0;
    // contents of the data to be pushed to Firebase
    private HashMap<String, Object> contents = new HashMap<String, Object>();
    // latitude and longitude of the touch location used to present this dialog
    private LatLng mCoordinates;
    // ID of the currently logged in user
    private String mUserId;

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
            // should not advance to next page without entering some text
            if (inputEditText.getText().toString().equals("")) {
                return;
            }
            updateContents();
            pageIndex++;
            loadInputPage();
        }
    };

    FirebaseDatabase mFirebaseDatabase;

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
            if (inputEditText.getText().toString().equals("")) {
                return;
            }
            // save input from the last page
            updateContents();
            switch (mType) {
                case Topic:
                    // data is pushed to location reference, so include the coordinates
                    contents.put(InfoKeys.LONGITUDE, mCoordinates.longitude);
                    contents.put(InfoKeys.LATITUDE, mCoordinates.latitude);
                    contents.put(InfoKeys.TIMESTAMP, ServerValue.TIMESTAMP);
                    // name is stored outside of the topic_location reference
                    String name = (String) contents.get(InfoKeys.NAME);
                    contents.remove(InfoKeys.NAME);
                    // push the coordinates as a topic location
                    DatabaseReference topicLocationReference = mFirebaseDatabase.getReference().child(Keys.TOPIC_LOCATIONS).child(mUserId);
                    topicLocationReference.removeValue();
                    topicLocationReference.setValue(contents);
                    // Rename the topic and clear old messages
                    DatabaseReference topicNameReference = mFirebaseDatabase.getReference().child(Keys.TOPICS).child(mUserId).child(Keys.INFO).child(Keys.NAME);
                    topicNameReference.setValue(name);
                    DatabaseReference topicMessageReference = mFirebaseDatabase.getReference().child(Keys.TOPICS).child(mUserId).child(Keys.MESSAGES);
                    topicMessageReference.removeValue();
                    // add the topic to this user's list
                    DatabaseReference userTopicReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(mUserId).child(Keys.TOPICS).child(mUserId);
                    userTopicReference.setValue(true);
                    break;
                case Article:
                    // should not be allowed to submit invalid URLs for articles
                    String textToValidate = inputEditText.getText().toString();
                    String validUrl = Utils.getValidUrl(textToValidate);
                    if (validUrl != null) {
                        contents.put(InfoKeys.URL, validUrl);
                    } else {
                        return;
                    }
                    // data is pushed to location reference, so include the coordinates
                    contents.put(InfoKeys.LONGITUDE, mCoordinates.longitude);
                    contents.put(InfoKeys.LATITUDE, mCoordinates.latitude);
                    contents.put(InfoKeys.TIMESTAMP, ServerValue.TIMESTAMP);
                    DatabaseReference articleReference = mFirebaseDatabase.getReference().child(Keys.ARTICLES).child(mUserId);
                    articleReference.removeValue();
                    articleReference.setValue(contents);
                    break;
                case Event:
                    // coordinates are store separately from actual event data
                    HashMap<String, Object> coordinateMap = new HashMap<>();
                    coordinateMap.put(InfoKeys.LONGITUDE, mCoordinates.longitude);
                    coordinateMap.put(InfoKeys.LATITUDE, mCoordinates.latitude);
                    coordinateMap.put(InfoKeys.TIMESTAMP, ServerValue.TIMESTAMP);
                    DatabaseReference eventLocationReference = mFirebaseDatabase.getReference().child(Keys.EVENT_LOCATIONS).child(mUserId);
                    // remove reference first so UI can update properly
                    eventLocationReference.removeValue();
                    eventLocationReference.setValue(coordinateMap);
                    // event data is stored separately, in the "info" child
                    DatabaseReference eventReference = mFirebaseDatabase.getReference().child(Keys.EVENTS).child(mUserId);
                    DatabaseReference eventInfoReference = eventReference.child(Keys.INFO);
                    eventInfoReference.setValue(contents);
                    // clear posts and members list
                    DatabaseReference eventPostsReference = eventReference.child(Keys.POSTS);
                    eventPostsReference.removeValue();
                    final DatabaseReference eventMembersReference = eventReference.child(Keys.MEMBERS);
                    eventMembersReference.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            // because users did not voluntarily join the new event, it should be removed from their events list
                            String userId = dataSnapshot.getKey();
                            // remove the event unless it was created by the logged in user
                            if (!userId.equals(mUserId)) {
                                // reference that lists this event on a user's profile
                                DatabaseReference userEventReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(userId).child(Keys.EVENTS).child(mUserId);
                                userEventReference.removeValue();
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    // remove all members from the events list
                    eventMembersReference.removeValue();
                    // make sure the user who posted the event is added as a member
                    DatabaseReference thisUserEventReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(mUserId).child(Keys.EVENTS).child(mUserId);
                    thisUserEventReference.setValue(true);
                    DatabaseReference memberReference = mFirebaseDatabase.getReference().child(Keys.EVENTS).child(mUserId).child(Keys.MEMBERS).child(mUserId);
                    memberReference.setValue(true);
                    break;
            }
            dismiss();
        }
    };

    public enum Type {
        Topic, Article, Event;
    }

    public MultipleInputView(Context context, Type type, LatLng coordinates) {
        super(context);
        mContext = context;
        mType = type;
        mCoordinates = coordinates;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                    inputEditText.setText((String) contents.get(InfoKeys.NAME));
                    inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
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
                    inputEditText.setText((String) contents.get(InfoKeys.TITLE));
                    inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    backButton.setText(mContext.getString(R.string.input_cancel));
                    backButton.setOnClickListener(cancelClickListener);
                    nextButton.setText(mContext.getString(R.string.input_next));
                    nextButton.setOnClickListener(nextPageClickListener);
                } else if (pageIndex == 1) {
                    // article URL
                    subtitleTextView.setText(R.string.input_article_p2_subtitle);
                    inputEditText.setHint(R.string.input_article_p2_hint);
                    inputEditText.setText((String) contents.get(InfoKeys.URL));
                    inputEditText.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
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
                    inputEditText.setText((String) contents.get(InfoKeys.NAME));
                    inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    backButton.setText(mContext.getString(R.string.input_cancel));
                    backButton.setOnClickListener(cancelClickListener);
                    nextButton.setText(mContext.getString(R.string.input_next));
                    nextButton.setOnClickListener(nextPageClickListener);
                } else if (pageIndex == 1) {
                    // event location
                    subtitleTextView.setText(R.string.input_event_p2_subtitle);
                    inputEditText.setHint(R.string.input_event_p2_hint);
                    inputEditText.setText((String) contents.get(InfoKeys.PLACE));
                    inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    backButton.setText(mContext.getString(R.string.input_back));
                    backButton.setOnClickListener(previousPageClickListener);
                    nextButton.setText(mContext.getString(R.string.input_next));
                    nextButton.setOnClickListener(nextPageClickListener);
                } else if (pageIndex == 2) {
                    // description of the event
                    subtitleTextView.setText(R.string.input_event_p3_subtitle);
                    inputEditText.setHint(R.string.input_event_p3_hint);
                    inputEditText.setText((String) contents.get(InfoKeys.ABOUT));
                    inputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
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
                contents.put(InfoKeys.NAME, input);
                break;
            case Article:
                if (pageIndex == 0) {
                    contents.put(InfoKeys.TITLE, input);
                } else if (pageIndex == 1) {
                    contents.put(InfoKeys.URL, input);
                }
                break;
            case Event:
                if (pageIndex == 0) {
                    contents.put(InfoKeys.NAME, input);
                } else if (pageIndex == 1) {
                    contents.put(InfoKeys.PLACE, input);
                } else if (pageIndex == 2) {
                    contents.put(InfoKeys.ABOUT, input);
                }
                break;
        }
    }

}
