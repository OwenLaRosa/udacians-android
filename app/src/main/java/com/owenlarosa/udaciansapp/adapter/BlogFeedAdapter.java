package com.owenlarosa.udaciansapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udaciansapp.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class BlogFeedAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> articles = new ArrayList<String>();

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference articlesReference;

    public BlogFeedAdapter(Context context) {
        mContext = context;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        articlesReference = mFirebaseDatabase.getReference().child("articles");
        articlesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // user ID of the article poster is the key
                String userId = dataSnapshot.getKey();
                articles.add(userId);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // articles will be removed if a user adds a new one (replaces it)
                String userId = dataSnapshot.getValue(String.class);
                articles.remove(userId);
                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getCount() {
        return articles.size();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public String getItem(int i) {
        return articles.get(i);
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
        String userId = articles.get(i);
        populateViewHolder(holder, userId);
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

    private void populateViewHolder(final ViewHolder viewHolder, String userId) {
        DatabaseReference articleReference = articlesReference.child(userId);
        DatabaseReference titleReference = articleReference.child("title");
        titleReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.titleTextView.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference authorReference = mFirebaseDatabase.getReference().child("users").child(userId).child("basic").child("name");
        authorReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.authorTextView.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference urlReference = articleReference.child("url");
        urlReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                viewHolder.urlTextView.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
