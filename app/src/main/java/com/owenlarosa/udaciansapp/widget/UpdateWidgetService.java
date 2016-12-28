package com.owenlarosa.udaciansapp.widget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
        if (Utils.updateJobs(getApplicationContext())) {
            Utils.updateWidget(getApplicationContext());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
