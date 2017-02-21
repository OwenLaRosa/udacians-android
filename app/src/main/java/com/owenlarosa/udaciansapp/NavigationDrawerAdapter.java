package com.owenlarosa.udaciansapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    // index of the currently selected view
    // initial value is 1 (index of map item) since this is selected by default
    private int selected = 1;

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        // update the selection color of the list
        notifyDataSetChanged();
        this.selected = selected;
    }

    private FirebaseDatabase mFirebaseDatabase;

    public NavigationDrawerAdapter(Context context) {
        mContext = context;
        navItems = mContext.getResources().getStringArray(R.array.navigation_items);
        navIcons = mContext.getResources().obtainTypedArray(R.array.navigation_icons);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
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
            holder.editProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, EditProfileActivity.class);
                    mContext.startActivity(intent);
                }
            });
            // user's basic profile info is shown in header
            populateHeader(holder);
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
            // the selected standard view should be highlighted
            if (i == selected) {
                cell.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
            } else {
                cell.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
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
        @BindView(R.id.drawer_edit_profile_button)
        Button editProfileButton;
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

    private void populateHeader(final ProfileViewHolder viewHolder) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // not authenticated, don't attempt to populate the header
            return;
        }
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userBasicReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(user).child(Keys.BASIC);
        DatabaseReference nameReference = userBasicReference.child(Keys.NAME);
        nameReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.nameTextView.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference titleReference = userBasicReference.child(Keys.TITLE);
        titleReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = dataSnapshot.getValue(String.class);
                if (title != null && !title.equals("")) {
                    viewHolder.titleTextView.setText(title);
                } else {
                    viewHolder.titleTextView.setText(mContext.getString(R.string.title_default));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference photoReference = userBasicReference.child(Keys.PHOTO);
        photoReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Glide.with(mContext)
                        .load(dataSnapshot.getValue(String.class))
                        .into(viewHolder.profileImageView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
