package com.owenlarosa.udaciansapp;

import android.content.Context;
import android.content.Intent;

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

}
