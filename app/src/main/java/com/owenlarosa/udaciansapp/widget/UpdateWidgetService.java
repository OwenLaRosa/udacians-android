package com.owenlarosa.udaciansapp.widget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.bumptech.glide.util.Util;
import com.owenlarosa.udaciansapp.Utils;

/**
 * Created by Owen LaRosa on 12/26/16.
 */

public class UpdateWidgetService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // once the service starts, initiate a new download of job listings
        updateJobs();

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateJobs() {
        String city = Utils.getJobSearchLocation(getApplicationContext());
        if (city == null) {
            // don't fetch jobs if the city is not known
            return;
        }
        if (Utils.getJobsForKeyword(getApplicationContext(), "android", city)) {
            // update widget once the download has succeeded
            Utils.updateWidget(getApplicationContext());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
