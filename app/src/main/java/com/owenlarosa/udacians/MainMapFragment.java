package com.owenlarosa.udacians;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.owenlarosa.udacians.data.Article;
import com.owenlarosa.udacians.data.EventLocation;
import com.owenlarosa.udacians.data.Location;
import com.owenlarosa.udacians.data.TopicLocation;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/17/16.
 */

public class MainMapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {

    private Unbinder mUnbinder;
    private GoogleMap mGoogleMap;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mLocationsReference;
    private DatabaseReference mEventsReference;
    private DatabaseReference mTopicsReference;
    private DatabaseReference mArticlesReference;
    private ChildEventListener mLocationsEventListener;
    private ChildEventListener mEventsEventListener;
    private ChildEventListener mTopicsEventListener;
    private ChildEventListener mArticlesEventListener;

    private HashMap<Marker, PinData> pinMappings = new HashMap<Marker, PinData>();
    private HashMap<Marker, String> articleUrls = new HashMap<Marker, String>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                setupMap();
            }
        });
        return rootView;
    }

    /**
     * Configure properties of the map, set click listeners, load initial data
     */
    private void setupMap() {
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mGoogleMap.setOnInfoWindowClickListener(this);
        syncData();
    }

    private void syncData() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mLocationsReference = mFirebaseDatabase.getReference().child("locations");
        mEventsReference = mFirebaseDatabase.getReference().child("event_locations");
        mTopicsReference = mFirebaseDatabase.getReference().child("topic_locations");
        mArticlesReference = mFirebaseDatabase.getReference().child("articles");

        mLocationsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addPin(dataSnapshot, PinType.Person);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mLocationsReference.limitToLast(100).addChildEventListener(mLocationsEventListener);

        mEventsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addPin(dataSnapshot, PinType.Event);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mEventsReference.limitToLast(20).addChildEventListener(mEventsEventListener);

        mTopicsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addPin(dataSnapshot, PinType.Topic);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mTopicsReference.limitToLast(20).addChildEventListener(mTopicsEventListener);

        mArticlesEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addPin(dataSnapshot, PinType.Article);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mArticlesReference.limitToLast(20).addChildEventListener(mArticlesEventListener);
    }

    private void addPin(DataSnapshot dataSnapshot, PinType type) {
        // store the type of data and the destination node
        PinData data = new PinData(type, dataSnapshot.getKey());
        // location to be displayed on the map
        MarkerOptions pin = new MarkerOptions();
        Marker marker;
        switch (type) {
            case Person:
                Location location = dataSnapshot.getValue(Location.class);
                pin.position(new LatLng(location.getLatitude(), location.getLongitude()));
                pin.title(location.getName());
                pin.snippet(location.getLocation());
                // add the marker and store it for handling click events later
                marker = mGoogleMap.addMarker(pin);
                pinMappings.put(marker, data);
                break;
            case Event:
                EventLocation eventLocation = dataSnapshot.getValue(EventLocation.class);
                pin.position(new LatLng(eventLocation.getLatitude(), eventLocation.getLongitude()));
                pin.title(eventLocation.getTitle());
                pin.snippet(eventLocation.getName());
                pin.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                marker = mGoogleMap.addMarker(pin);
                pinMappings.put(marker, data);
                break;
            case Topic:
                TopicLocation topicLocation = dataSnapshot.getValue(TopicLocation.class);
                pin.position(new LatLng(topicLocation.getLatitude(), topicLocation.getLongitude()));
                pin.title(topicLocation.getName());
                pin.snippet(topicLocation.getAuthor());
                pin.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                marker = mGoogleMap.addMarker(pin);
                pinMappings.put(marker, data);
                break;
            case Article:
                Article article = dataSnapshot.getValue(Article.class);
                pin.position(new LatLng(article.getLatitude(), article.getLongitude()));
                pin.title(article.getTitle());
                pin.snippet(article.getAuthor());
                pin.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                marker = mGoogleMap.addMarker(pin);
                pinMappings.put(marker, data);
                // save url so it can be referenced later
                articleUrls.put(marker, article.getUrl());
                break;
            case Job:
                break;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        // get data associated with the marker
        PinData data = pinMappings.get(marker);
        // perform appropriate intent based on the type of data
        Intent intent;
        switch (data.type) {
            case Person:
                intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
                break;
            case Event:
                intent = new Intent(getActivity(), EventActivity.class);
                startActivity(intent);
                break;
            case Topic:
                intent = new Intent(getActivity(), ChatActivity.class);
                startActivity(intent);
                break;
            case Article:
                // open the article in the browser
                String url = articleUrls.get(marker);
                Uri articleUri = Uri.parse(url);
                intent = new Intent(Intent.ACTION_VIEW, articleUri);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    // Classifications of different types of data marked by pins
    enum PinType {
        Person,
        Event,
        Topic,
        Job,
        Article
    }

    /**
     * Information regarding the type of pin and its data's location in the DB
     */
    class PinData {

        // type of data represented by the pin
        public PinType type;
        // node in which the corresponding data is stored in Firebase
        public String key;

        public PinData(PinType type, String key) {
            this.type = type;
            this.key = key;
        }
    }

}
