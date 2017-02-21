package com.owenlarosa.udaciansapp.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.owenlarosa.udaciansapp.Keys;
import com.owenlarosa.udaciansapp.R;
import com.owenlarosa.udaciansapp.contentprovider.JobsListColumns;
import com.owenlarosa.udaciansapp.contentprovider.JobsProvider;

/**
 * Created by Owen LaRosa on 12/23/16.
 */

public class ListWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                // clear and restore data when using non-exported content provider
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(JobsProvider.Jobs.JOBS,
                        null,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.jobs_list_item);
                String title = data.getString(data.getColumnIndex(JobsListColumns.TITLE));
                remoteViews.setTextViewText(R.id.job_title_text_view, title);
                String company = data.getString(data.getColumnIndex(JobsListColumns.COMPANY));
                remoteViews.setTextViewText(R.id.job_company_text_view, company);
                String location = data.getString(data.getColumnIndex(JobsListColumns.LOCATION));
                remoteViews.setTextViewText(R.id.job_location_text_view, location);
                String url = data.getString(data.getColumnIndex(JobsListColumns.URL));
                Intent fillInIntent = new Intent();
                fillInIntent.setAction(ListWidgetProvider.ACTION_OPEN_JOB_POSTING);
                Bundle bundle = new Bundle();
                bundle.putString(Keys.URL, url);
                fillInIntent.putExtras(bundle);
                remoteViews.setOnClickFillInIntent(R.id.job_list_item, fillInIntent);

                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.jobs_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    return data.getLong(data.getColumnIndex(JobsListColumns._ID));
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}