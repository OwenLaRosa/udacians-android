package com.owenlarosa.udaciansapp.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Owen LaRosa on 12/17/16.
 */

public class JobsSyncService extends Service {

    private static final Object syncAdapterLock = new Object();
    private static JobsSyncAdapter syncAdapter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new JobsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
