package com.owenlarosa.udaciansapp;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.owenlarosa.udaciansapp.adapter.PostsListAdapter;
import com.owenlarosa.udaciansapp.data.BasicProfile;
import com.owenlarosa.udaciansapp.data.Message;
import com.owenlarosa.udaciansapp.data.ProfileInfo;
import com.owenlarosa.udaciansapp.interfaces.MessageDelegate;
import com.owenlarosa.udaciansapp.views.ProfileView;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static android.view.View.VISIBLE;

/**
 * Created by Owen LaRosa on 11/7/16.
 */

public class ProfileFragment extends Fragment implements MessageDelegate {

    public static final String EXTRA_USERID = "userId";

    // user selected an image from the gallery
    private static final int RESULT_PICK_IMAGE = 1;
    // user took an image with the camera
    private static final int RESULT_TAKE_IMAGE = 2;

    @BindView(R.id.profile_image_view)
    ImageView profilePictureImageView;
    @BindView(R.id.profile_name_text_view)
    TextView nameTextView;
    @BindView(R.id.profile_title_text_view)
    TextView titleTextView;
    @BindView(R.id.connect_button)
    FloatingActionButton connectButton;
    @BindView(R.id.posts_list_view)
    ListView postsListView;
    ProfileView headerView;

    private Unbinder mUnbinder;

    private String mUserId;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBasicReference;
    private DatabaseReference mProfileReference;
    private DatabaseReference mPostsReference;
    private DatabaseReference mIsConnectionReference;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPublicImageStorage;

    // whether or not this user is a connection
    private boolean mIsConnection = false;

    private Context mContext;
    // ensures resources can be accessed even if not attached to activity
    private Resources mResources;

    // image currently displayed in post authoring view
    private Bitmap mImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
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
        mResources = getResources();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference userReference = mFirebaseDatabase.getReference().child("users").child(mUserId);
        mBasicReference = userReference.child("basic");
        mBasicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BasicProfile profile = dataSnapshot.getValue(BasicProfile.class);
                nameTextView.setText(profile.getName());
                if (profile.getTitle() != null && !profile.getTitle().equals("")) {
                    titleTextView.setText(profile.getTitle());
                } else {
                    titleTextView.setText(getString(R.string.title_default));
                }
                if (profile.getAbout() != null && !profile.getAbout().equals("")) {
                    headerView.aboutTextView.setText(profile.getAbout());
                } else {
                    headerView.aboutTextView.setText(getString(R.string.about_default));
                }
                Glide.with(getActivity())
                        .load(profile.getPhoto())
                        .into(profilePictureImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProfileReference = userReference.child("profile");
        mProfileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                // exit if valid profile info was not returned
                if (profileInfo == null) {
                    headerView.linksLinearLayout.setVisibility(View.GONE);
                    return;
                }
                // if the content does not contain actual links (e.g. all empty strings)
                boolean noLinks = true;
                // check if links of each type exist, and if so add them to the layout
                if (profileInfo.getSite() != null && !profileInfo.getSite().equals("")) {
                    addLinkButton(LinkType.Personal, profileInfo.getSite());
                    noLinks = false;
                }
                if (profileInfo.getBlog() != null && !profileInfo.getBlog().equals("")) {
                    addLinkButton(LinkType.Blog, profileInfo.getBlog());
                    noLinks = false;
                }
                if (profileInfo.getLinkedin() != null && !profileInfo.getLinkedin().equals("")) {
                    addLinkButton(LinkType.Linkedin, profileInfo.getLinkedin());
                    noLinks = false;
                }
                if (profileInfo.getTwitter() != null && !profileInfo.getTwitter().equals("")) {
                    addLinkButton(LinkType.Twitter, profileInfo.getTwitter());
                    noLinks = false;
                }
                // there may be valid data but no actual links to show; view should be gone in this case
                if (noLinks) {
                    headerView.linksLinearLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // used to write new posts, reading posts is handled by the adapter
        mPostsReference = userReference.child("posts");
        // connections stored under user currently signed into the app
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final PostsListAdapter postsAdapter = new PostsListAdapter(getActivity(), mUserId, PostsListAdapter.PostsType.Person);
        postsListView.setAdapter(postsAdapter);

        if (user.equals(mUserId)) {
            // users can't add themselves as a connection
            connectButton.setVisibility(View.GONE);
            // user can post on their own wall
            headerView = new ProfileView(getActivity());
            postsListView.addHeaderView(headerView);
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
        } else {
            // users can add others as connections
            mIsConnectionReference = mFirebaseDatabase.getReference().child("users").child(user).child("connections").child(mUserId);
            mIsConnectionReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        // connection is added, show option to remove
                        mIsConnection = true;
                        connectButton.setImageResource(R.drawable.remove_connection);
                        connectButton.setBackgroundTintList(ColorStateList.valueOf(mResources.getColor(R.color.colorRemove)));
                    } else {
                        // not a connection yet, show option to add
                        mIsConnection = false;
                        connectButton.setImageResource(R.drawable.add_connection);
                        connectButton.setBackgroundTintList(ColorStateList.valueOf(mResources.getColor(R.color.colorAccent)));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            headerView = new ProfileView(getActivity());
            postsListView.addHeaderView(headerView);
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
            // users can't post on others' profile
            headerView.writePostView.setVisibility(View.GONE);
        }

        // storage is used for uploading images
        mFirebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = mFirebaseStorage.getReferenceFromUrl("gs://udacians-df696.appspot.com");
        mPublicImageStorage = storageReference.child(user).child("public").child("images");

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
                mImage = BitmapFactory.decodeFile(picturePath);
                headerView.writePostView.previewImageView.setVisibility(VISIBLE);
                headerView.writePostView.previewImageView.setImageBitmap(mImage);
            }
        }
    }

    @OnClick(R.id.connect_button)
    public void connectTapped(View view) {
        if (mIsConnection) {
            // remove the connection
            mIsConnectionReference.removeValue();
        } else {
            // add the connection
            mIsConnectionReference.setValue(true);
        }
    }

    // types of eligible links for image buttons
    enum LinkType {
        Personal,
        Blog,
        Linkedin,
        Twitter
    }

    /**
     * Create and add a button that opens a link
     * @param type Type of link, used to determine the image
     * @param link Url to open in browser when tapped
     */
    private void addLinkButton(LinkType type, final String link) {
        // configure the button's size and margins
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int size = (int) mResources.getDimension(R.dimen.profile_links_height);
        int horizontalSpace = (int) mResources.getDimension(R.dimen.profile_links_horizontal_space);
        layoutParams.setMargins(0, 0, horizontalSpace, 0);
        layoutParams.setMarginEnd(horizontalSpace);
        layoutParams.width = size;
        layoutParams.height = size;
        // create a button with these layout parameters
        ImageButton button = new ImageButton(getActivity());
        button.setLayoutParams(layoutParams);
        // determine the correct icon to display
        switch (type) {
            case Personal:
                button.setBackgroundResource(R.drawable.personal_site);
                break;
            case Blog:
                button.setBackgroundResource(R.drawable.blog);
                break;
            case Linkedin:
                button.setBackgroundResource(R.drawable.linkedin);
                break;
            case Twitter:
                button.setBackgroundResource(R.drawable.twitter);
                break;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // convert to Uri for use with intent
                final String urlPrefix = "http://";
                Uri linkUri = null;
                if (link.startsWith(urlPrefix)) {
                    linkUri = Uri.parse(link);
                } else {
                    linkUri = Uri.parse(urlPrefix + link);
                }
                // open the link in the browser
                Intent intent = new Intent(Intent.ACTION_VIEW, linkUri);
                startActivity(intent);
            }
        });
        // display the button onscreen
        headerView.linksLinearLayout.addView(button);
    }

    @Override
    public void sendMessage(final Message message) {
        if (mImage != null) {
            // message contains an image to be uploaded
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            byte[] binaryData = outputStream.toByteArray();
            // use the current date to generate a unique file name for the image
            String imageName = new Date().toString() + ".jpg";
            UploadTask uploadTask = mPublicImageStorage.child(imageName).putBytes(binaryData);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // use map so server generates timestamp
                    message.setImageUrl(taskSnapshot.getDownloadUrl().toString());
                    mPostsReference.push().setValue(message.toMap());
                    // reset for a new message to be sent
                    mImage = null;
                }
            });
        } else {
            // no image? just send the message without
            mPostsReference.push().setValue(message.toMap());
        }
    }

    // on Android Marshmallow and later, permissions for reading the image must be requested at runtime
    // see: http://stackoverflow.com/questions/8854359/exception-open-failed-eacces-permission-denied-on-android

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
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
