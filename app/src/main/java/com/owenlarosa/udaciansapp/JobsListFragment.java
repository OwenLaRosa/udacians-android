package com.owenlarosa.udaciansapp;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.owenlarosa.udaciansapp.adapter.JobsListAdapter;
import com.owenlarosa.udaciansapp.contentprovider.JobsListColumns;
import com.owenlarosa.udaciansapp.contentprovider.JobsProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class JobsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 0;

    @BindView(R.id.jobs_list_view)
    ListView listView;

    private Unbinder mUnbinder;
    private Context mContext;
    private View view;
    private OkHttpClient mClient = new OkHttpClient();

    private JobsListAdapter mJobsAdapter;
    private Cursor mCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_jobs_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mContext = getActivity();

        mJobsAdapter = new JobsListAdapter(mContext, mCursor);
        listView.setAdapter(mJobsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCursor.moveToPosition(i);
                String urlString = mCursor.getString(mCursor.getColumnIndex(JobsListColumns.URL));
                Uri uri = Uri.parse(urlString);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mContext.startActivity(intent);
            }
        });
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String city = Utils.getJobSearchLocation(mContext);
                if (city != null) {
                    getJobsForKeyword("android", city);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(mContext,
                JobsProvider.Jobs.JOBS,
                new String[] {JobsListColumns._ID, JobsListColumns.TITLE, JobsListColumns.COMPANY, JobsListColumns.LOCATION, JobsListColumns.URL},
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mJobsAdapter.swapCursor(cursor);
        mCursor = cursor;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mJobsAdapter.swapCursor(null);
    }

    private void getJobsForKeyword(String keyword, String city) {
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
                contentValuesVector.toArray(cvArray);
                mContext.getContentResolver().delete(JobsProvider.Jobs.JOBS, null, null);
                mContext.getContentResolver().bulkInsert(JobsProvider.Jobs.JOBS, cvArray);
            }
            view.post(new Runnable() {
                @Override
                public void run() {
                    mJobsAdapter.notifyDataSetChanged();
                    Utils.updateWidget(mContext);
                }
            });
        } catch (Exception e) {
            Log.e("", e.getLocalizedMessage());
        }
    }

}
