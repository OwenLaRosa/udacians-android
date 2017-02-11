package com.owenlarosa.udaciansapp.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udaciansapp.R;
import com.owenlarosa.udaciansapp.Utils;
import com.owenlarosa.udaciansapp.contentprovider.JobsListColumns;
import com.owenlarosa.udaciansapp.contentprovider.JobsProvider;

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

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    // data should update every 6 hours
    public static final int SYNC_INTERVAL = 10;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public JobsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(JobsSyncAdapter.class.getSimpleName(), "performing jobs sync...");
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

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                JobsProvider.AUTHORITY, bundle);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = JobsProvider.AUTHORITY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, JobsProvider.AUTHORITY, true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
