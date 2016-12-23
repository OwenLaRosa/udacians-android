package com.owenlarosa.udaciansapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.preference.PreferenceManager;

import java.util.List;
import java.util.prefs.Preferences;

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

}
