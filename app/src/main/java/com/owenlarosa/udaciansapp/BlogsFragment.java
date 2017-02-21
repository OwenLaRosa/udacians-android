package com.owenlarosa.udaciansapp;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udaciansapp.adapter.BlogFeedAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/14/16.
 */

public class BlogsFragment extends Fragment {

    @BindView(R.id.blogs_list_view)
    ListView listView;

    Unbinder mUnbinder;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference articlesReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_blogs_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        articlesReference = mFirebaseDatabase.getReference().child(Keys.ARTICLES);

        final BlogFeedAdapter adapter = new BlogFeedAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String articleId = adapter.getItem(i);
                DatabaseReference urlReference = articlesReference.child(articleId).child(Keys.URL);
                urlReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String urlString = dataSnapshot.getValue(String.class);
                        Uri articleUri = Uri.parse(urlString);
                        Intent intent = new Intent(Intent.ACTION_VIEW, articleUri);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        return rootView;
    }
}
