package com.owenlarosa.udacians;

import android.app.Activity;
import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.adapter.AttendeesAdapter;
import com.owenlarosa.udacians.adapter.PostsListAdapter;
import com.owenlarosa.udacians.data.Event;
import com.owenlarosa.udacians.data.Message;
import com.owenlarosa.udacians.interfaces.MessageDelegate;
import com.owenlarosa.udacians.views.EventView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static android.view.View.VISIBLE;

/**
 * Created by Owen LaRosa on 11/12/16.
 */

public class EventFragment extends Fragment implements MessageDelegate {

    public static final String EXTRA_USERID = "userId";

    // user selected an image from the gallery
    private static final int RESULT_PICK_IMAGE = 1;
    // user took an image with the camera
    private static final int RESULT_TAKE_IMAGE = 2;

    @BindView(R.id.event_name_text_view)
    TextView nameTextView;
    @BindView(R.id.event_location_text_view)
    TextView locationTextView;
    @BindView(R.id.event_organizer_text_view)
    TextView organizerTextView;
    @BindView(R.id.attend_button)
    FloatingActionButton attendButton;
    @BindView(R.id.event_posts_list_view)
    ListView postsListView;
    EventView headerView;

    Unbinder mUnbinder;

    private String mUserId;

    private Context mContext;
    private Resources mResources;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mEventReference;
    private DatabaseReference mPostsReference;
    // event is added to the user's data
    private DatabaseReference isAttendingReference;
    // user is added to the event's data
    private DatabaseReference isMemberReference;

    private boolean mIsAttending = false;

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

        mContext = getActivity();
        mResources = getActivity().getResources();

        PostsListAdapter postsAdapter = new PostsListAdapter(getActivity(), mUserId, PostsListAdapter.PostsType.Event);
        postsListView.setAdapter(postsAdapter);
        headerView = new EventView(getActivity());
        postsListView.addHeaderView(headerView);

        // sending the actual messages is handled by this fragment
        headerView.writePostView.delegate = this;
        // set up the button to add an image to post
        headerView.writePostView.addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make sure read permissions are enabled before loading image
                verifyStoragePermissions((Activity) mContext);
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_PICK_IMAGE);
            }
        });

        // show horizontal list of attendees for this event
        // referenced: http://www.androidhive.info/2016/01/android-working-with-recycler-view/
        AttendeesAdapter attendeesAdapter = new AttendeesAdapter(getActivity(), mUserId);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        headerView.recyclerView.setLayoutManager(layoutManager);
        headerView.recyclerView.setItemAnimator(new DefaultItemAnimator());
        headerView.recyclerView.setAdapter(attendeesAdapter);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mEventReference = mFirebaseDatabase.getReference().child("events").child(mUserId).child("info");
        mEventReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                nameTextView.setText(event.getName());
                locationTextView.setText(getString(R.string.evemt_location, event.getPlace()));
                headerView.aboutTextView.setText(event.getAbout());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // show name of organizer
        final DatabaseReference organizerReference = mFirebaseDatabase.getReference().child("users").child(mUserId).child("basic").child("name");
        organizerReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                organizerTextView.setText(getString(R.string.event_organizer, name));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mPostsReference = mFirebaseDatabase.getReference().child("events").child(mUserId).child("posts");
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        isAttendingReference = mFirebaseDatabase.getReference().child("users").child(user).child("events").child(mUserId);
        isAttendingReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    // user is attending event, show option to remove
                    mIsAttending = true;
                    attendButton.setImageResource(R.drawable.remove_event);
                    attendButton.setBackgroundTintList(ColorStateList.valueOf(mResources.getColor(R.color.colorRemove)));
                } else {
                    // not attending, show option to attend event
                    mIsAttending = false;
                    attendButton.setImageResource(R.drawable.add_event);
                    attendButton.setBackgroundTintList(ColorStateList.valueOf(mResources.getColor(R.color.colorAccent)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        isMemberReference = mFirebaseDatabase.getReference().child("events").child(mUserId).child("members").child(user);

        return rootView;
    }

    // get image returned from gallery or camera
    // referenced: http://viralpatel.net/blogs/pick-image-from-galary-android-app/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RESULT_PICK_IMAGE && resultCode == RESULT_OK && null != intent) {
            Uri imagePath = intent.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getActivity().getContentResolver().query(imagePath,
                    filePathColumn, null, null, null);
            if (null != cursor && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                // show selected bitmap in the preview pane
                headerView.writePostView.previewImageView.setVisibility(VISIBLE);
                headerView.writePostView.previewImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            }
        }
    }

    @OnClick(R.id.attend_button)
    public void attendButtonTapped(View view) {
        if (mIsAttending) {
            // remove event from user data
            isAttendingReference.removeValue();
            // unlist them as attendee
            isMemberReference.removeValue();
        } else {
            // add event to user data
            isAttendingReference.setValue(true);
            // include user in attendee list
            isMemberReference.setValue(true);
        }
    }

    @Override
    public void sendMessage(Message message) {
        // use map so server generates timestamp
        mPostsReference.push().setValue(message.toMap());
    }

    // on Android Marshmallow and later, permissions for reading the image must be requested at runtime
    // see: http://stackoverflow.com/questions/8854359/exception-open-failed-eacces-permission-denied-on-android

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
