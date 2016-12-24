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
                    if (Utils.getJobsForKeyword(mContext, "android", city)) {
                        // update list and widget on main thread if task succeeds
                        updateLists();
                    }
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

    /**
     * Update the adapter and widget on main thread
     */
    private void updateLists() {
        view.post(new Runnable() {
            @Override
            public void run() {
                mJobsAdapter.notifyDataSetChanged();
                Utils.updateWidget(mContext);
            }
        });
    }

}
