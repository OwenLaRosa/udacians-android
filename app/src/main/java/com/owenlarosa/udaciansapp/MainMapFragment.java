package com.owenlarosa.udaciansapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.database.ValueEventListener;
import com.owenlarosa.udaciansapp.data.Article;
import com.owenlarosa.udaciansapp.data.Location;
import com.owenlarosa.udaciansapp.views.MultipleInputView;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Owen LaRosa on 11/17/16.
 */

public class MainMapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    private Unbinder mUnbinder;
    private Context mContext;
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
    private HashMap<Marker, PinData> topicIds = new HashMap<Marker, PinData>();
    // reference markers by ID for their corresponding data
    // if data with a certain ID is deleted or replaced, old pins will be removed
    private HashMap<String, Marker> userMappings = new HashMap<>();
    private HashMap<String, Marker> eventMappings = new HashMap<>();
    private HashMap<String, Marker> topicMappings = new HashMap<>();
    private HashMap<String, Marker> articleMappings = new HashMap<>();

    // a carefully crafted leaky object to preserve the state of the input view
    // this should be migrated to DialogFragment in the future, but will preserve the input view's contents
    public static MultipleInputView inputView = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mContext = getActivity();

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                setupMap();
            }
        });
        if (inputView != null) {
            // show previous input view if it existed
            inputView.show();
        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (inputView != null) {
            if (inputView.isShowing()) {
                // this allows us to re-show the input view when the view state restores
                inputView.dismiss();
            } else {
                // non-showing view was dismissed by the user and should not be recreated
                inputView.dismiss();
                inputView = null;
            }
        }
    }

    /**
     * Configure properties of the map, set click listeners, load initial data
     */
    private void setupMap() {
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.setOnMapLongClickListener(this);
        syncData();
    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mContext.getString(R.string.add_new_prompt));
        builder.setItems(R.array.add_new_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0: // topic
                        inputView = new MultipleInputView(mContext, MultipleInputView.Type.Topic, latLng);
                        break;
                    case 1: // article
                        inputView = new MultipleInputView(mContext, MultipleInputView.Type.Article, latLng);
                        break;
                    case 2: // event
                        inputView = new MultipleInputView(mContext, MultipleInputView.Type.Event, latLng);
                        break;
                    default:
                        break;
                }
                if (inputView != null) {
                    inputView.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            inputView = null;
                        }
                    });
                    inputView.show();
                }
            }
        });
        builder.create().show();
    }

    private void syncData() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mLocationsReference = mFirebaseDatabase.getReference().child(Keys.LOCATIONS);
        mEventsReference = mFirebaseDatabase.getReference().child(Keys.EVENT_LOCATIONS);
        mTopicsReference = mFirebaseDatabase.getReference().child(Keys.TOPIC_LOCATIONS);
        mArticlesReference = mFirebaseDatabase.getReference().child(Keys.ARTICLES);

        mLocationsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addPin(dataSnapshot, PinType.Person);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeMarkerWithKey(dataSnapshot.getKey(), PinType.Person);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mLocationsReference.orderByChild(Keys.TIMESTAMP).limitToLast(100).addChildEventListener(mLocationsEventListener);

        mEventsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addPin(dataSnapshot, PinType.Event);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeMarkerWithKey(dataSnapshot.getKey(), PinType.Event);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mEventsReference.orderByChild(Keys.TIMESTAMP).limitToLast(20).addChildEventListener(mEventsEventListener);

        mTopicsEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addPin(dataSnapshot, PinType.Topic);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeMarkerWithKey(dataSnapshot.getKey(), PinType.Topic);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mTopicsReference.orderByChild(Keys.TIMESTAMP).limitToLast(20).addChildEventListener(mTopicsEventListener);

        mArticlesEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addPin(dataSnapshot, PinType.Article);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                removeMarkerWithKey(dataSnapshot.getKey(), PinType.Article);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mArticlesReference.orderByChild(Keys.TIMESTAMP).limitToLast(20).addChildEventListener(mArticlesEventListener);
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
                pin.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_pin));
                // add the marker and store it for handling click events later
                marker = mGoogleMap.addMarker(pin);
                pinMappings.put(marker, data);
                addIdMap(data.key, marker, type);
                break;
            case Event:
                Location eventLocation = dataSnapshot.getValue(Location.class);
                pin.position(new LatLng(eventLocation.getLatitude(), eventLocation.getLongitude()));
                pin.icon(BitmapDescriptorFactory.fromResource(R.drawable.event_pin));
                marker = mGoogleMap.addMarker(pin);
                pinMappings.put(marker, data);
                addIdMap(data.key, marker, type);
                break;
            case Topic:
                Location topicLocation = dataSnapshot.getValue(Location.class);
                pin.position(new LatLng(topicLocation.getLatitude(), topicLocation.getLongitude()));
                pin.icon(BitmapDescriptorFactory.fromResource(R.drawable.topic_pin));
                marker = mGoogleMap.addMarker(pin);
                pinMappings.put(marker, data);
                // key is the topic ID
                topicIds.put(marker, data);
                addIdMap(data.key, marker, type);
                break;
            case Article:
                Article article = dataSnapshot.getValue(Article.class);
                pin.position(new LatLng(article.getLatitude(), article.getLongitude()));
                pin.title(article.getTitle());
                pin.snippet(article.getAuthor());
                pin.icon(BitmapDescriptorFactory.fromResource(R.drawable.blog_pin));
                marker = mGoogleMap.addMarker(pin);
                pinMappings.put(marker, data);
                // save url so it can be referenced later
                articleUrls.put(marker, article.getUrl());
                addIdMap(data.key, marker, type);
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
                // pass the user id to the profile screen
                intent.putExtra(ProfileFragment.EXTRA_USERID, data.key);
                startActivity(intent);
                ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case Event:
                intent = new Intent(getActivity(), EventActivity.class);
                intent.putExtra(EventFragment.EXTRA_USERID, data.key);
                startActivity(intent);
                ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case Topic:
                intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatFragment.EXTRA_CHAT, data.key);
                // all discussions here are group topics, not direct messages
                intent.putExtra(ChatFragment.EXTRA_DIRECT, false);
                startActivity(intent);
                ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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

    // when pin is tapped, populate its popup window with relevant data
    @Override
    public boolean onMarkerClick(Marker marker) {
        PinData data = pinMappings.get(marker);
        switch (data.type) {
            case Person:
                loadPersonData(marker, data.key);
                break;
            case Event:
                loadEventData(marker, data.key);
                break;
            case Topic:
                loadTopicData(marker, data.key);
                break;
            case Article:
                loadArticleData(marker, data.key);
            default:
                break;
        }
        // no custom action, popup window will be displayed
        return false;
    }

    // methods for loading data to be displayed on a marker's info window

    private void loadPersonData(final Marker marker, String userId) {
        // reference to basic profile info
        DatabaseReference userReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(userId).child(Keys.BASIC);
        // user's display name
        DatabaseReference nameReference = userReference.child(Keys.NAME);
        // user's title
        DatabaseReference titleReference = userReference.child(Keys.TITLE);
        setMarkerString(marker, nameReference, true);
        setMarkerString(marker, titleReference, false);
    }

    private void loadEventData(Marker marker, String userId) {
        DatabaseReference eventReference = mFirebaseDatabase.getReference().child(Keys.EVENTS).child(userId).child(Keys.INFO);
        // title of the event
        DatabaseReference nameReference = eventReference.child(Keys.NAME);
        // full address of the event
        DatabaseReference placeReference = eventReference.child(Keys.PLACE);
        setMarkerString(marker, nameReference, true);
        setMarkerString(marker, placeReference, false);
    }

    private void loadTopicData(Marker marker, String userId) {
        // name of the topic
        DatabaseReference topicNameReference = mFirebaseDatabase.getReference().child(Keys.TOPICS).child(userId).child(Keys.INFO).child(Keys.NAME);
        // author of the topic
        DatabaseReference authorNameReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(userId).child(Keys.BASIC).child(Keys.NAME);
        setMarkerString(marker, topicNameReference, true);
        setMarkerString(marker, authorNameReference, false);
    }

    private void loadArticleData(Marker marker, String userId) {
        // title of the article
        DatabaseReference articleTitleReference = mFirebaseDatabase.getReference().child(Keys.ARTICLES).child(userId).child(Keys.TITLE);
        // poster of the article
        DatabaseReference authorNameReference = mFirebaseDatabase.getReference().child(Keys.USERS).child(userId).child(Keys.BASIC).child(Keys.NAME);
        setMarkerString(marker, articleTitleReference, true);
        setMarkerString(marker, authorNameReference, false);
    }

    private void addIdMap(String key, Marker marker, PinType type) {
        switch (type) {
            case Person:
                userMappings.put(key, marker);
                break;
            case Event:
                eventMappings.put(key, marker);
                break;
            case Topic:
                topicMappings.put(key, marker);
                break;
            case Article:
                articleMappings.put(key, marker);
                break;
        }
    }

    /**
     * Delete a marker with the corresponding key from the map
     * @param key Key from a database snapshot that was removed
     */
    private void removeMarkerWithKey(String key, PinType type) {
        Marker marker = null;
        switch (type) {
            case Person:
                marker = userMappings.get(key);
                break;
            case Event:
                marker = eventMappings.get(key);
                break;
            case Topic:
                marker = topicMappings.get(key);
                break;
            case Article:
                marker = articleMappings.get(key);
                break;
        }
        if (marker != null) {
            marker.remove();
        }
    }


    /**
     * Download title or snippet for the marker
     * Show the info window once both title and snippet are present
     * @param marker A pin of any type
     * @param reference reference to the string to be loaded into info window
     * @param isTitle true to set title, false to set snippet
     */
    private void setMarkerString(final Marker marker, DatabaseReference reference, final boolean isTitle) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String string = dataSnapshot.getValue(String.class);
                if (isTitle) {
                    marker.setTitle(string);
                } else {
                    marker.setSnippet(string);
                }
                if (marker.getTitle() != null && marker.getSnippet() != null) {
                    // marker's will not be displayed automatically
                    // when both fields have been filled, the window should be shown
                    marker.showInfoWindow();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
