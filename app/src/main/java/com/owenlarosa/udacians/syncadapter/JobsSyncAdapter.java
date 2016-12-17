package com.owenlarosa.udacians.syncadapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udacians.contentprovider.JobsListColumns;
import com.owenlarosa.udacians.contentprovider.JobsProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Vector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Owen LaRosa on 12/17/16.
 */

public class JobsSyncAdapter extends AbstractThreadedSyncAdapter {

    private OkHttpClient mClient = new OkHttpClient();
    private Context mContext;

    private FirebaseDatabase mFirebaseDatabase;

    public JobsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();
        DatabaseReference enrollmentsReference = mFirebaseDatabase.getReference().child("users").child(userId).child("enrollments");
        enrollmentsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String courseId = dataSnapshot.getKey();
                DatabaseReference jobTitleReference = mFirebaseDatabase.getReference().child("nano_degrees").child(courseId).child("job");
                jobTitleReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // search term to be sent to Dice API
                        String jobKeyword = dataSnapshot.getValue(String.class);
                        getJobsForKeyword(jobKeyword);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
    }

    private void getJobsForKeyword(String keyword) {
        final String BASE_URL = "http://service.dice.com/api/rest/jobsearch/v1/simple.json?";
        final String PARAM_SEARCH_TEXT = "text";
        String url = new StringBuilder()
                .append(BASE_URL)
                .append(PARAM_SEARCH_TEXT)
                .append("=")
                .append(keyword)
                .append("&pgcnt=20")
                .toString();
        Request request = new Request.Builder()
                .url(BASE_URL)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            String responseText = response.body().string();
            JSONObject root = new JSONObject(responseText);
            JSONArray results = root.getJSONArray("resultItemList");

            Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(results.length());

            for (int i = 0; i < results.length(); i++) {
                JSONObject job = results.getJSONObject(i);
                String link = job.getString("detailUrl");
                String title = job.getString("jobTitle");
                String company = job.getString("company");
                String location = job.getString("location");
                String date = job.getString("date");

                ContentValues jobValues = new ContentValues();
                jobValues.put(JobsListColumns.URL, link);
                jobValues.put(JobsListColumns.TITLE, title);
                jobValues.put(JobsListColumns.COMPANY, company);
                jobValues.put(JobsListColumns.LOCATION, location);
                jobValues.put(JobsListColumns.DATE, 1);

                contentValuesVector.add(jobValues);
            }
            if (contentValuesVector.size() > 0) {
                // delete all existing jobs from the database and replace them with the new jobs
                ContentValues[] cvArray = new ContentValues[contentValuesVector.size()];
                mContext.getContentResolver().delete(JobsProvider.Jobs.JOBS, null, null);
                mContext.getContentResolver().bulkInsert(JobsProvider.Jobs.JOBS, cvArray);
            }
        } catch (Exception e) {

        }
    }

}
