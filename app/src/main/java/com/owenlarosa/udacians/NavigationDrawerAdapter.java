package com.owenlarosa.udacians;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class NavigationDrawerAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_PROFILE = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    // titles of each nav item that comes after profile view
    private String[] navItems;
    private TypedArray navIcons;
    private Context mContext;

    public NavigationDrawerAdapter(Context context) {
        mContext = context;
        navItems = mContext.getResources().getStringArray(R.array.navigation_items);
        navIcons = mContext.getResources().obtainTypedArray(R.array.navigation_icons);
    }

    // number of navigation items
    @Override
    public int getCount() {
        // standard views plus header for profile info
        return navItems.length + 1;
    }

    // these 2 methods are required by base adapter, not used here
    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // profile view and standard item view
        return 2;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View cell = view;
        int viewType = getItemViewType(i);
        // the first view shows basic profile information
        if (viewType == 0) {
            ProfileViewHolder holder = null;
            if (cell == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                cell = inflater.inflate(R.layout.drawer_list_item_profile, viewGroup, false);
                holder = new ProfileViewHolder(cell);
                cell.setTag(holder);
            } else {
                holder = (ProfileViewHolder) cell.getTag();
            }
        } else {
            // subsequent views contain standard drawer tabs
            ItemViewHolder holder = null;
            if (cell == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                cell = inflater.inflate(R.layout.drawer_list_item, viewGroup, false);
                holder = new ItemViewHolder(cell);
                cell.setTag(holder);
            } else {
                holder = (ItemViewHolder) cell.getTag();
            }
            // standard views contain a title and an icon
            // because the first view is different, the first standard view is at index 0 in items array
            holder.titleTextView.setText(navItems[i - 1]);
            holder.iconImageView.setImageDrawable(navIcons.getDrawable(i - 1));
        }
        return cell;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_PROFILE : VIEW_TYPE_ITEM;
    }

    // view for standard navigation tabs
    static class ItemViewHolder {
        @BindView(R.id.drawer_title)
        TextView titleTextView;
        @BindView(R.id.drawer_icon)
        ImageView iconImageView;

        ItemViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    // view for topmost item, shows profile info
    static class ProfileViewHolder {
        @BindView(R.id.drawer_profile_image_view)
        ImageView profileImageView;
        @BindView(R.id.drawer_profile_name_text_view)
        TextView nameTextView;
        @BindView(R.id.drawer_profile_title_text_view)
        TextView titleTextView;

        ProfileViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
