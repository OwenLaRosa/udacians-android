package com.owenlarosa.udacians.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.owenlarosa.udacians.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class EventsListAdapter extends BaseAdapter {

    private Context mContext;

    public EventsListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        // 10 views as placeholders
        return 10;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View cell = view;
        ViewHolder holder = null;
        if (cell == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            cell = inflater.inflate(R.layout.events_list_item, viewGroup, false);
            holder = new ViewHolder(cell);
            cell.setTag(holder);
        } else {
            holder = (ViewHolder) cell.getTag();
        }
        return cell;
    }

    static class ViewHolder {
        @BindView(R.id.event_name_text_view)
        TextView nameTextView;
        @BindView(R.id.event_about_text_view)
        TextView aboutTextView;
        @BindView(R.id.event_location_text_view)
        TextView urlTextView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
