package com.owenlarosa.udacians;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.owenlarosa.udacians.locations.PersonLocation;

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
    private ChildEventListener mLocationsEventListener;

    private HashMap<Marker, PinData> pinMappings = new HashMap<Marker, PinData>();

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

        mLocationsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("", dataSnapshot.getKey());
                // store the type of data and the destination node
                PinData data = new PinData(PinType.Person, dataSnapshot.getKey());
                // create a location to be displayed on the map
                PersonLocation location = dataSnapshot.getValue(PersonLocation.class);
                MarkerOptions pin = new MarkerOptions();
                pin.position(new LatLng(location.getLatitude(), location.getLongitude()));
                pin.title(location.getName());
                pin.snippet(location.getLocation());
                // add the marker and store it for handling click events later
                Marker marker = mGoogleMap.addMarker(pin);
                pinMappings.put(marker, data);
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
