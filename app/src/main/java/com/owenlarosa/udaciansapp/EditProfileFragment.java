package com.owenlarosa.udaciansapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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
import com.owenlarosa.udaciansapp.data.BasicProfile;
import com.owenlarosa.udaciansapp.data.ProfileInfo;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static android.view.View.VISIBLE;

/**
 * Created by Owen LaRosa on 11/16/16.
 */

public class EditProfileFragment extends Fragment {

    // user selected an image from the gallery
    private static final int RESULT_PICK_IMAGE = 1;

    private static final String KEY_PHOTO = "photo";
    private static final String KEY_TITLE = "title";
    private static final String KEY_ABOUT = "about";
    private static final String KEY_SITE = "site";
    private static final String KEY_BLOG = "blog";
    private static final String KEY_LINKEDIN = "linkedin";
    private static final String KEY_TWITTER = "twitter";

    @BindView(R.id.edit_save_button)
    Button saveChangesButton;
    @BindView(R.id.edit_profile_image_button)
    ImageButton profileImageButton;
    @BindView(R.id.edit_title_text_field)
    EditText titleEditText;
    @BindView(R.id.edit_about_text_field)
    EditText aboutEditText;
    @BindView(R.id.edit_site_text_field)
    EditText siteEditText;
    @BindView(R.id.edit_blog_text_field)
    EditText blogEditText;
    @BindView(R.id.edit_linkedin_text_field)
    EditText linkedinEditText;
    @BindView(R.id.edit_twitter_text_field)
    EditText twitterEditText;

    Unbinder mUnbinder;
    private Context mContext;
    private Resources mResources;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mBasicReference;
    DatabaseReference mProfileReference;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPublicImageStorage;

    // image currently displayed in post authoring view
    private Bitmap mImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mContext = getActivity();
        mResources = mContext.getResources();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userReference = mFirebaseDatabase.getReference().child("users").child(user);
        mBasicReference = userReference.child("basic");
        mProfileReference = userReference.child("profile");

        mFirebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = mFirebaseStorage.getReferenceFromUrl("gs://udacians-df696.appspot.com");
        mPublicImageStorage = storageReference.child(user).child("public").child("images");

        if (savedInstanceState == null) {
            // prefill the data for the first launch
            loadData();
        } else {
            if (savedInstanceState.containsKey(KEY_PHOTO)) {
                // user selected a new image but did not save it
                mImage = savedInstanceState.getParcelable(KEY_PHOTO);
                profileImageButton.setImageBitmap(mImage);
            } else {
                // user keeps original image, already stored in reference
                DatabaseReference photoReference = mBasicReference.child("photo");
                photoReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Glide.with(mContext)
                                .load(dataSnapshot.getValue(String.class))
                                .into(profileImageButton);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            titleEditText.setEnabled(true);
            titleEditText.setText(savedInstanceState.getString(KEY_TITLE));
            aboutEditText.setEnabled(true);
            aboutEditText.setText(savedInstanceState.getString(KEY_ABOUT));
            siteEditText.setEnabled(true);
            siteEditText.setText(savedInstanceState.getString(KEY_SITE));
            blogEditText.setEnabled(true);
            blogEditText.setText(savedInstanceState.getString(KEY_BLOG));
            linkedinEditText.setEnabled(true);
            linkedinEditText.setText(savedInstanceState.getString(KEY_LINKEDIN));
            twitterEditText.setEnabled(true);
            twitterEditText.setText(savedInstanceState.getString(KEY_TWITTER));
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mImage != null) {
            outState.putParcelable(KEY_PHOTO, mImage);
        }
        outState.putString(KEY_TITLE, titleEditText.getText().toString());
        outState.putString(KEY_ABOUT, aboutEditText.getText().toString());
        outState.putString(KEY_SITE, siteEditText.getText().toString());
        outState.putString(KEY_BLOG, blogEditText.getText().toString());
        outState.putString(KEY_LINKEDIN, linkedinEditText.getText().toString());
        outState.putString(KEY_TWITTER, twitterEditText.getText().toString());
    }

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
                profileImageButton.setVisibility(VISIBLE);
                profileImageButton.setImageBitmap(mImage);
            }
        }
    }

    @OnClick(R.id.edit_profile_image_button)
    public void chooseImage() {
        // make sure read permissions are enabled before loading image
        verifyStoragePermissions((Activity) mContext);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_PICK_IMAGE);
    }

    /**
     * Populate text fields with current profile data
     */
    private void loadData() {
        mBasicReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                BasicProfile basicProfile = dataSnapshot.getValue(BasicProfile.class);
                if (basicProfile != null) {
                    getActivity().setTitle(basicProfile.getName());
                    titleEditText.setText(basicProfile.getTitle() != null ? basicProfile.getTitle() : "");
                    aboutEditText.setText(basicProfile.getAbout() != null ? basicProfile.getAbout() : "");
                    if (basicProfile.getPhoto() != null) {
                        Glide.with(mContext)
                                .load(basicProfile.getPhoto())
                                .into(profileImageButton);
                    }
                }
                titleEditText.setEnabled(true);
                aboutEditText.setEnabled(true);
                profileImageButton.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProfileReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    siteEditText.setText(profileInfo.getSite() != null ? profileInfo.getSite() : "");
                    blogEditText.setText(profileInfo.getBlog() != null ? profileInfo.getBlog() : "");
                    linkedinEditText.setText(profileInfo.getLinkedin() != null ? profileInfo.getLinkedin() : "");
                    twitterEditText.setText(profileInfo.getTwitter() != null ? profileInfo.getTwitter() : "");
                }
                siteEditText.setEnabled(true);
                blogEditText.setEnabled(true);
                linkedinEditText.setEnabled(true);
                twitterEditText.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Upload new profile data to the server
     */
    @OnClick(R.id.edit_save_button)
    public void saveChanges() {
        mBasicReference.child("title").setValue(titleEditText.getText().toString());
        mBasicReference.child("about").setValue(aboutEditText.getText().toString());
        String siteLink = Utils.getValidUrl(siteEditText.getText().toString());
        if (siteLink != null || siteEditText.getText().toString().equals("")) {
            siteEditText.setBackgroundColor(mResources.getColor(R.color.white));
            mProfileReference.child(KEY_SITE).setValue(siteLink);
        } else {
            siteEditText.setBackgroundColor(mResources.getColor(R.color.invalidUrl));
        }
        String blogLink = Utils.getValidUrl(blogEditText.getText().toString());
        if (blogLink != null || blogEditText.getText().toString().equals("")) {
            blogEditText.setBackgroundColor(mResources.getColor(R.color.white));
            mProfileReference.child(KEY_BLOG).setValue(blogLink);
        } else {
            blogEditText.setBackgroundColor(mResources.getColor(R.color.invalidUrl));
        }
        String linkedinLink = Utils.getValidUrl(linkedinEditText.getText().toString());
        if (linkedinLink != null || linkedinEditText.getText().toString().equals("")) {
            linkedinEditText.setBackgroundColor(mResources.getColor(R.color.white));
            mProfileReference.child(KEY_LINKEDIN).setValue(linkedinLink);
        } else {
            linkedinEditText.setBackgroundColor(mResources.getColor(R.color.invalidUrl));
        }
        String twitterLink = Utils.getValidUrl(twitterEditText.getText().toString());
        if (twitterLink != null || twitterEditText.getText().toString().equals("")) {
            twitterEditText.setBackgroundColor(mResources.getColor(R.color.white));
            mProfileReference.child(KEY_TWITTER).setValue(twitterLink);
        } else {
            twitterEditText.setBackgroundColor(mResources.getColor(R.color.invalidUrl));
        }
        if (mImage != null) {
            // don't allow multiple saves while image is uploading
            saveChangesButton.setEnabled(false);
            saveChangesButton.setText(getString(R.string.uploading_photo));
            // user has changed their profile picture
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            byte[] binaryData = outputStream.toByteArray();
            // use the current date to generate a unique file name for the image
            String imageName = new Date().toString() + ".jpg";
            UploadTask uploadTask = mPublicImageStorage.child(imageName).putBytes(binaryData);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mBasicReference.child("photo").setValue(taskSnapshot.getDownloadUrl().toString());
                    // reset so image is not uploaded twice
                    mImage = null;
                    // enable the save button for further changes
                    saveChangesButton.setEnabled(true);
                    saveChangesButton.setText(getString(R.string.save_changes));
                }
            });
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
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

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
