package com.owenlarosa.udaciansapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.owenlarosa.udaciansapp.R;
import com.owenlarosa.udaciansapp.contentprovider.JobsDatabase;
import com.owenlarosa.udaciansapp.contentprovider.JobsListColumns;
import com.owenlarosa.udaciansapp.contentprovider.JobsProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class JobsListAdapter extends CursorAdapter {

    private Context mContext;

    public JobsListAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.jobs_list_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String title = cursor.getString(cursor.getColumnIndex(JobsListColumns.TITLE));
        viewHolder.titleTextView.setText(title);
        String company = cursor.getString(cursor.getColumnIndex(JobsListColumns.COMPANY));
        viewHolder.companyTextView.setText(company);
        String location = cursor.getString(cursor.getColumnIndex(JobsListColumns.LOCATION));
        viewHolder.locationTextView.setText(location);
    }


    static class ViewHolder {
        @BindView(R.id.job_title_text_view)
        TextView titleTextView;
        @BindView(R.id.job_company_text_view)
        TextView companyTextView;
        @BindView(R.id.job_location_text_view)
        TextView locationTextView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}

