package com.owenlarosa.udaciansapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.preference.PreferenceManager;
import android.util.Log;

import com.owenlarosa.udaciansapp.contentprovider.JobsListColumns;
import com.owenlarosa.udaciansapp.contentprovider.JobsProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

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

    private static final OkHttpClient client = new OkHttpClient();

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

}
