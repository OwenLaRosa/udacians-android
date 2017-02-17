package com.owenlarosa.udaciansapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udaciansapp.contentprovider.JobsListColumns;
import com.owenlarosa.udaciansapp.contentprovider.JobsProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Owen LaRosa on 12/23/16.
 */

public class Utils {

    public static final String ACTION_DATA_UPDATED = "com.owenlarosa.udaciansapp.ACTION_DATA_UPDATED";

    /**
     * Alert the jobs widget that data has changed
     * @param context Context/activity calling this method
     */
    public static void updateWidget(Context context) {
        Intent intent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    /**
     * Sets the city/location used when performing job searches
     * @param context Used to access shared preferences
     * @param location Address from reverse geocoding this user's location
     */
    public static void setJobSearchLocation(Context context, Address location) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String city = null;
        if (location.getPostalCode() != null) {
            city = location.getPostalCode();
        } else if (location.getLocality() != null) {
            city = location.getLocality();
        } else if (location.getSubAdminArea() != null) {
            city = location.getSubAdminArea();
        }
        // set the new city if it was returned, otherwise keep the old one
        if (city != null) {
            preferences.edit().putString(context.getString(R.string.pref_city), city).apply();
        }
    }

    /**
     * Retrieve the city/location used to perform job searches
     * @param context Used to access shared preferences
     * @return City name or postal code, null if nonexistent
     */
    public static String getJobSearchLocation(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_city), null);
    }

    // Downloading and updating jobs for the list and widget

    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Searches for jobs in the user's location based on their keywords
     * Clears database of old jobs replacing them with new ones
     * @param context Used to access preference manager
     * @return true if successfully updated jobs, otherwise false
     */
    public static boolean updateJobs(Context context) {
        String city = Utils.getJobSearchLocation(context);
        if (city == null) {
            // don't fetch jobs if the city is not known
            return false;
        }
        String[] keywords = Utils.readKeywordsFromPreferences(context);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < keywords.length; i++) {
            stringBuilder.append(keywords[i]);
            if (i != keywords.length - 1) {
                // add plus symbol between all keywords, not after the last one
                stringBuilder.append("+");
            }
        }
        String searchText = stringBuilder.toString();
        if (searchText.equals("")) {
            // exit if no job search keywords match the user
            return false;
        }
        return getJobsForKeyword(context, searchText, city);
    }

    public static boolean getJobsForKeyword(final Context context, String keyword, String city) {
        final String BASE_URL = "http://service.dice.com/api/rest/jobsearch/v1/simple.json?";
        final String PARAM_SEARCH_TEXT = "text";
        String url = new StringBuilder()
                .append(BASE_URL)
                .append(PARAM_SEARCH_TEXT)
                .append("=")
                .append(keyword)
                .append("&city=")
                .append(city)
                .append("&pgcnt=30")
                .append("&sort=1")
                .toString();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
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
                contentValuesVector.toArray(cvArray);
                context.getContentResolver().delete(JobsProvider.Jobs.JOBS, null, null);
                context.getContentResolver().bulkInsert(JobsProvider.Jobs.JOBS, cvArray);
            }
            return true;
        } catch (Exception e) {
            Log.e("", e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Download job keywords based on enrolled courses and write them to shared preferences
     * this should be called after syncing profile/enrollments data in the login procedure
     * @param context Used to access the preference manager
     */
    public static void storeJobKeywords(final Context context) {
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference enrollmentsReference = firebaseDatabase.getReference().child("users").child(user).child("enrollments");
        enrollmentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // determine the number of enrollments of the user
                // this ensures we know when all possible job keywords have downloaded
                int enrollments = 0;
                final ArrayList<String> courseIds = new ArrayList<>();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    enrollments++;
                    courseIds.add(child.getKey());
                }
                if (enrollments == 0) {
                    // exit if the user has no enrollments
                    return;
                }
                final ArrayList<String> keywords = new ArrayList<>();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    final String courseId = child.getKey();
                    DatabaseReference jobKeywordReference = firebaseDatabase.getReference().child("nano_degrees").child(courseId).child("keyword");
                    jobKeywordReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String keyword = dataSnapshot.getValue(String.class);
                            handleAdd(keyword);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // default if no matching keywords were found
                            handleAdd(null);
                        }

                        private void handleAdd(String keyword) {
                            keywords.add(keyword);
                            if (keywords.size() >= courseIds.size()) {
                                // all keywords have been downloaded, should save them for later use
                                writeKeywordsToPreferences(keywords, context);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Store job keywords as encoded string into shared preferences
     * @param keywords List of keywords, may contain elements that are null
     * @param context Used to access preference manager
     */
    private static void writeKeywordsToPreferences(ArrayList<String> keywords, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringBuilder stringBuilder = new StringBuilder();
        for (String keyword: keywords) {
            // omit courses that do not have relevant keywords
            if (keyword != null) {
                stringBuilder.append(keyword).append(",");
            }
        }
        String encodedKeywords = stringBuilder.toString();
        preferences.edit().putString(context.getString(R.string.pref_job_keywords), encodedKeywords).apply();
    }

    /**
     * Clear job keywords from preferences
     * Should be called when the user logs out
     * @param context Used to access preference manager
     */
    public static void resetKeywordsInPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(context.getString(R.string.pref_job_keywords), "").apply();
    }

    /**
     * Retrieve saved job keywords as an array
     * @param context Used to access preference manager
     * @return Array of job keywords, empty if there are none
     */
    public static String[] readKeywordsFromPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_job_keywords), "").split(",");
    }

    // direct messaging

    /**
     * Gets a database reference of a direct message node
     * References start at the direct_messages node
     * The two next children are the user IDs, lesser one first, then greater one
     * This indicates no particular relationship about the data hierarchy
     * It only ensures the paths are consistent and that both user IDs are captured
     * by the security rules
     * @param user1 Currently logged in user
     * @param user2 User they're messaging
     * @return Reference to the direct messages
     */
    public static DatabaseReference getDirectChatReference(String user1, String user2) {
        // User IDs are long integers, so we can compare their numeric values
        Long user1long = Long.parseLong(user1);
        Long user2long = Long.parseLong(user2);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference messagesReference = firebaseDatabase.getReference().child("direct_messages");
        DatabaseReference chatReference;
        if (user1long < user2long) {
            chatReference = messagesReference.child(user1).child(user2);
        } else {
            chatReference = messagesReference.child(user2).child(user1);
        }
        return chatReference;
    }

    /**
     * Format the date as a string
     * @param date date to be formatted
     * @return time with AM/PM marker is less than a day ago, otherwise month/date/year
     */
    public static String formatTime(Date date) {
        Date today = new Date();
        SimpleDateFormat dateFormat;
        if (today.getTime() - date.getTime() <= 86400000) {
            dateFormat = new SimpleDateFormat("h:m a");
        } else {
            dateFormat = new SimpleDateFormat("M/d/yy");
        }
        return dateFormat.format(date);
    }

    /**
     * Determine if a URL is valid
     * @param string url to check as String
     * @return true if it is valid, otherwise false
     */
    public static boolean isValidUrl(String string) {
        // referenced: http://obscuredclarity.blogspot.com/2011/10/validate-url-in-java.html
        URL url = null;
        try {
            url = new URL(string);
        } catch (MalformedURLException e) {
            return false;
        }
        try {
            url.toURI();
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }

}
