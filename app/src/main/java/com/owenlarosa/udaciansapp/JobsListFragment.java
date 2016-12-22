package com.owenlarosa.udaciansapp;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.owenlarosa.udaciansapp.adapter.JobsListAdapter;
import com.owenlarosa.udaciansapp.contentprovider.JobsListColumns;
import com.owenlarosa.udaciansapp.contentprovider.JobsProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class JobsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 0;

    @BindView(R.id.jobs_list_view)
    ListView listView;

    private Unbinder mUnbinder;
    private Context mContext;

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
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        return rootView;
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
                new String[] {JobsListColumns.TITLE, JobsListColumns.COMPANY, JobsListColumns.LOCATION, JobsListColumns.URL},
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mJobsAdapter.swapCursor(null);
    }

}
