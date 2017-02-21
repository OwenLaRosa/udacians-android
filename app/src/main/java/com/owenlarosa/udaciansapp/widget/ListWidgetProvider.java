package com.owenlarosa.udaciansapp.widget;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.owenlarosa.udaciansapp.R;
import com.owenlarosa.udaciansapp.Utils;

/**
 * Created by Owen LaRosa on 12/23/16.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ListWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_OPEN_JOB_POSTING = "com.owenlarosa.udaciansapp.widget.ACTION_OPEN_JOB_POSTING";

    // repeating alarm to perform automatic updates for widget
    // referenced from http://www.parallelrealities.co.uk/2011/09/using-alarmmanager-for-updating-android.html

    private PendingIntent service = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int id: appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list);

            // launch the main activity when user taps icon
            Intent intent = new Intent(ACTION_OPEN_JOB_POSTING);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setPendingIntentTemplate(R.id.widget_list, pendingIntent);
            remoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, remoteViews);
            } else {
                setRemoteAdapterV11(context, remoteViews);
            }
            appWidgetManager.updateAppWidget(id, remoteViews);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (Utils.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            // refresh the list widget when data updates
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        } else if (intent.getAction().equals(ACTION_OPEN_JOB_POSTING)) {
            Uri jobLink = Uri.parse(intent.getExtras().getString("url"));
            Intent jobIntent = new Intent(Intent.ACTION_VIEW, jobLink);
            context.startActivity(jobIntent);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, ListWidgetRemoteViewsService.class));
    }

    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_list,
                new Intent(context, ListWidgetRemoteViewsService.class));
    }

}