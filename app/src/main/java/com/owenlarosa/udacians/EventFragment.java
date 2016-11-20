package com.owenlarosa.udacians;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.data.Event;
import com.owenlarosa.udacians.views.WritePostView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/12/16.
 */

public class EventFragment extends Fragment {

    public static final String EXTRA_USERID = "userId";

    @BindView(R.id.event_name_text_view)
    TextView nameTextView;
    @BindView(R.id.event_location_text_view)
    TextView locationTextView;
    @BindView(R.id.event_about_text_view)
    TextView aboutTextView;
    @BindView(R.id.event_write_post_view)
    WritePostView writePostView;
    @BindView(R.id.event_posts_recycler_view)
    RecyclerView recyclerView;

    Unbinder mUnbinder;

    private String mUserId;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mEventReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            // attached to profile activity
            mUserId = intent.getStringExtra(EXTRA_USERID);
        } else {
            // attached to main activity
            mUserId = getArguments().getString(EXTRA_USERID);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mEventReference = mFirebaseDatabase.getReference().child("events").child(mUserId).child("info");
        mEventReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                nameTextView.setText(event.getName());
                locationTextView.setText(event.getPlace());
                aboutTextView.setText(event.getAbout());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return rootView;
    }

    @OnClick(R.id.attend_button)
    public void attendButtonTapped(View view) {

    }

}
