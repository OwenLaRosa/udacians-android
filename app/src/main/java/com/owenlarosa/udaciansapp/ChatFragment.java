package com.owenlarosa.udaciansapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.owenlarosa.udaciansapp.adapter.MessageListAdapter;
import com.owenlarosa.udaciansapp.data.Message;
import com.owenlarosa.udaciansapp.views.ChatInputView;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static android.view.View.VISIBLE;

/**
 * Created by Owen LaRosa on 11/11/16.
 */

public class ChatFragment extends Fragment {

    // ID of the chat to be displayed
    public static final String EXTRA_CHAT = "chat";
    // whether or not this chat is a direct message
    public static final String EXTRA_DIRECT = "direct";

    private static final String TEXT_KEY = "text";
    private static final String IMAGE_KEY = "image";

    // user selected an image from the gallery
    private static final int RESULT_PICK_IMAGE = 1;

    @BindView(R.id.chat_list_view)
    ListView messagesListView;
    @BindView(R.id.chat_entry)
    ChatInputView chatEntry;

    private Context mContext;
    Unbinder mUnbinder;
    // id of the chat, either course ID or user ID of the creator
    String chatId = "";

    // image currently displayed in post authoring view
    private Bitmap mImage;
    // true if sending a message will cause the user to join the chat
    // false if already joined chat
    private Boolean shouldJoinChat;

    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mSenderDirectMessageReference;
    private DatabaseReference mRecipientDirectMessageReference;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPublicImageStorage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mContext = getActivity();

        final boolean direct;

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            chatId = intent.getStringExtra(EXTRA_CHAT);
            direct = intent.getBooleanExtra(EXTRA_DIRECT, false);
        } else {
            chatId = getArguments().getString(EXTRA_CHAT);
            direct = getArguments().getBoolean(EXTRA_DIRECT, false);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // set up storage for uploading images
        final String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = mFirebaseStorage.getReferenceFromUrl(Keys.STORAGE_BASE_URL);
        mPublicImageStorage = storageReference.child(user).child(Keys.PUBLIC).child(Keys.IMAGES);

        final DatabaseReference chatReference;
        mSenderDirectMessageReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(user).child(Keys.DIRECT_MESSAGES).child(chatId);
        mRecipientDirectMessageReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(chatId).child(Keys.DIRECT_MESSAGES).child(user);
        if (direct) {
            // direct messages
            chatReference = Utils.getDirectChatReference(user, chatId);
            DatabaseReference nameReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(chatId).child(Keys.BASIC).child(Keys.NAME);
            // title should be name of user sending DMs to
            nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.getValue(String.class);
                    ((AppCompatActivity) getActivity()).setTitle(name);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            chatReference = mFirebaseDatabase.getReference().child(Keys.TOPICS).child(chatId).child(Keys.MESSAGES);
            if (chatId.startsWith(Keys.ND)) {
                DatabaseReference nameReference = mFirebaseDatabase.getReference().child(Keys.NANO_DEGREES).child(chatId).child(Keys.NAME);
                // title should be name of the Nanodegree
                nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.getValue(String.class);
                        ((AppCompatActivity) getActivity()).setTitle(name);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                // this discussion is already on the user's list
                shouldJoinChat = false;
            } else {
                DatabaseReference nameReference = mFirebaseDatabase.getReference().child(Keys.TOPICS).child(chatId).child(Keys.INFO).child(Keys.NAME);
                // title should be name/prompt of the topic
                nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.getValue(String.class);
                        ((AppCompatActivity) getActivity()).setTitle(name);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                // determine if sending a message will cause the user to join (add to discussions list)
                DatabaseReference isMemberReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(user).child(Keys.TOPICS).child(chatId);
                isMemberReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            // user has not joined yet
                            shouldJoinChat = true;
                        } else {
                            // discussion is already in the user's list
                            shouldJoinChat = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        MessageListAdapter adapter = new MessageListAdapter(getActivity(), chatReference);
        messagesListView.setAdapter(adapter);

        chatEntry.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // messages without text unless there's an image should be rejected
                if (chatEntry.messageTextField.getText().toString().equals("") && mImage == null) {
                    return;
                }
                // create a message to be sent
                final Message message = new Message();
                message.setSender(FirebaseAuth.getInstance().getCurrentUser().getUid());
                message.setContent(chatEntry.messageTextField.getText().toString());
                if (mImage != null) {
                    // message contains an image to be uploaded
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    mImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                    byte[] binaryData = outputStream.toByteArray();
                    // use the current date to generate a unique file name for the image
                    String imageName = new Date().toString() + Keys.JPEG_EXTENSION;
                    UploadTask uploadTask = mPublicImageStorage.child(imageName).putBytes(binaryData);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // use map so server generates timestamp
                            message.setImageUrl(taskSnapshot.getDownloadUrl().toString());
                            chatReference.push().setValue(message.toMap());
                        }
                    });
                } else {
                    // sending standard message without an image
                    chatReference.push().setValue(message.toMap());
                }

                // ensure future messages don't use previous image
                mImage = null;
                // clear the chat input
                chatEntry.messageTextField.setText("");
                chatEntry.imagePreview.setVisibility(View.GONE);
                chatEntry.messageTextField.setCompoundDrawables(null, null, null, null);

                // add discussion to the user's list if they haven't already been added
                if (shouldJoinChat != null && shouldJoinChat) {
                    DatabaseReference isMemberReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(user).child(Keys.TOPICS).child(chatId);
                    isMemberReference.setValue(true);
                    shouldJoinChat = false;
                }

                if (direct) {
                    mSenderDirectMessageReference.setValue(ServerValue.TIMESTAMP);
                    mRecipientDirectMessageReference.setValue(ServerValue.TIMESTAMP);
                }
            }
        });

        chatEntry.pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make sure read permissions are enabled before loading image
                verifyStoragePermissions((Activity) mContext);
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_PICK_IMAGE);
            }
        });

        if (savedInstanceState != null) {
            chatEntry.messageTextField.setText(savedInstanceState.getString(TEXT_KEY));
            if (savedInstanceState.containsKey(IMAGE_KEY)) {
                mImage = savedInstanceState.getParcelable(IMAGE_KEY);
                chatEntry.imagePreview.setVisibility(VISIBLE);
                chatEntry.imagePreview.setImageBitmap(mImage);
            }
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TEXT_KEY, chatEntry.messageTextField.getText().toString());
        if (mImage != null) {
            outState.putParcelable(IMAGE_KEY, mImage);
        }
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
                chatEntry.imagePreview.setVisibility(VISIBLE);
                chatEntry.imagePreview.setImageBitmap(mImage);
            }
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


