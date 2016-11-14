package com.owenlarosa.udacians.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.owenlarosa.udacians.NavigationDrawerAdapter;
import com.owenlarosa.udacians.R;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class BlogFeedAdapter extends BaseAdapter {

    private Context mContext;

    public BlogFeedAdapter(Context context) {
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
            cell = inflater.inflate(R.layout.blogs_list_item, viewGroup, false);
            holder = new ViewHolder(cell);
            cell.setTag(holder);
        } else {
            holder = (ViewHolder) cell.getTag();
        }
        return cell;
    }

    static class ViewHolder {
        @BindView(R.id.blog_title_text_view)
        TextView titleTextView;
        @BindView(R.id.blog_author_text_view)
        TextView authorTextView;
        @BindView(R.id.blog_url_text_view)
        TextView urlTextView;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
