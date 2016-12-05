package com.owenlarosa.udacians.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.owenlarosa.udacians.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 12/5/16.
 */

public class MessageListAdapter extends BaseAdapter {

    private Context mContext;

    DatabaseReference messagesListReference;

    public MessageListAdapter(Context context, DatabaseReference messagesListReference) {
        mContext = context;
        this.messagesListReference = messagesListReference;
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View cell = view;
        ViewHolder holder = null;
        if (cell == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            cell = inflater.inflate(R.layout.message_view, viewGroup, false);
            holder = new ViewHolder(cell);
            cell.setTag(holder);
        } else {
            holder = (ViewHolder) cell.getTag();
        }
        return cell;
    }

    class ViewHolder {
        @BindView(R.id.message_name_text_view)
        TextView nameTextView;
        @BindView(R.id.message_profile_image_view)
        ImageView profileImageView;
        @BindView(R.id.message_time_text_view)
        TextView timeTextView;
        @BindView(R.id.message_content_text_view)
        TextView contentTextView;
        @BindView(R.id.message_content_image_view)
        ImageView contentImageView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
