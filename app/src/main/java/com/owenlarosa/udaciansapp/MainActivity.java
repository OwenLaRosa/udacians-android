package com.owenlarosa.udaciansapp;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.owenlarosa.udaciansapp.syncadapter.JobsSyncAdapter;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = "FTAG";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 0;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.view_container)
    FrameLayout viewContainer;
    @BindView(R.id.left_drawer)
    ListView navigationDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    Unbinder unbinder;

    // listen for authentication events
    FirebaseAuth.AuthStateListener mAuthStateListener;

    // For Google location services
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // accessing the database
    FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);

        attemptLogin();

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user has logged in, must notify adapter so nav drawer's header is populated
                    ((NavigationDrawerAdapter) navigationDrawer.getAdapter()).notifyDataSetChanged();
                    int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        // once authenticated, register for location to sync this with database
                        setupGoogleApiClient();
                    } else {
                        // not granted, request the permission
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);

        mDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                R.drawable.menu,
                R.string.open_drawer,
                R.string.close_drawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);

        // populate the nav drawer with its static content
        final NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(this);
        navigationDrawer.setAdapter(adapter);
        navigationDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    // show user's profile info
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra(ProfileFragment.EXTRA_USERID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                    startActivity(intent);
                } else if (i == 9) {
                    // logout button tapped
                    logout();
                } else {
                    if (i == adapter.getSelected()) {
                        // tab currently selected, just return
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return;
                    }

                    adapter.setSelected(i);

                    // insert appropriate fragment into frame layout
                    Fragment fragment = new Fragment();
                    switch (i) {
                        case 1:
                            fragment = new MainMapFragment();
                            break;
                        case 2:
                            fragment = new BlogsFragment();
                            break;
                        case 3:
                            fragment = new ConnectionsListFragment();
                            break;
                        case 4:
                            fragment = new ChatsListFragment();
                            break;
                        case 5:
                            fragment = new EventsListFragment();
                            break;
                        case 6:
                            fragment = new JobsListFragment();
                            break;
                        case 7:
                            fragment = new SettingsFragment();
                            break;
                        case 8:
                            fragment = new HelpFragment();
                            break;
                        default:
                            return;
                    }
                    getFragmentManager().beginTransaction()
                            .replace(R.id.view_container, fragment, FRAGMENT_TAG)
                            .commit();

                    // after updaing the layout, dismiss the drawer
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);
        if (savedInstanceState == null) {
            // show map screen the first time the app launches
            Fragment fragment = new MainMapFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.view_container, fragment, FRAGMENT_TAG)
                    .commit();
            //adapter.getView(1, null, null).setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // should disconnect from Google APIs when activity dismisses
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // once permission is granted, we can start listening for locations
                setupGoogleApiClient();
            }
        }
    }

    private void attemptLogin() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String authToken = sharedPreferences.getString(getString(R.string.pref_auth_token), "");
        if (authToken.equals("")) {
            presentLoginScreen();
        } else {
            FirebaseAuth.getInstance().signInWithCustomToken(authToken);
        }
    }

    private void presentLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Signs out the current user and presents the login screen
     */
    private void logout() {
        // remove XSRF token from cookie store
        // ensures future logins won't use cookies from the previous user's session
        PersistentCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));
        cookieJar.clear();
        // unauthenticate from the database
        FirebaseAuth.getInstance().signOut();
        // remove auth toke, future launches will go directly to login screen if logged out
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(getString(R.string.pref_auth_token), "").apply();
        // allow the user to login and finish this activity
        presentLoginScreen();
    }

    public void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Upload's the user's current location to the database
     * @param latitude Latitude of the location
     * @param longitude Longitude of the location
     * @param place String representing place (e.g. Cambridge, MA) can be null
     */
    public void syncUserLocation(double latitude, double longitude, String place) {
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference locationReference = mFirebaseDatabase.getReference().child("locations").child(user);
        com.owenlarosa.udaciansapp.data.Location location = new com.owenlarosa.udaciansapp.data.Location();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setLocation(place);
        locationReference.setValue(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setInterval(360000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // ensure users in same city will have pins in slightly different locations
        Location obfuscatedLocation = obfuscateLocation(location);
        double longitude = obfuscatedLocation.getLongitude();
        double latitude = obfuscatedLocation.getLatitude();
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this);
            try {
                // only the first result is needed from the reverse geocoder
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.size() != 0) {
                    Address address = addresses.get(0);
                    String place = null;
                    if (address.getLocality() != null) {
                        // locality will generally return city and postal code
                        place = address.getLocality();
                    } else if (address.getSubAdminArea() != null) {
                        // for some places the sub admin area contains the city name
                        place = address.getSubAdminArea();
                    }
                    if (place != null) {
                        syncUserLocation(latitude, longitude, place);
                    } else {
                        // if we can't find a city, we can still sync the location without
                        syncUserLocation(latitude, longitude, null);
                    }
                }
            } catch (IOException e) {
                // sync without a place name if geocoding fails altogether
                syncUserLocation(latitude, longitude, null);
            }
        }
        JobsSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // retry the connection to location services
        setupGoogleApiClient();
    }

    /**
     * Shifts coordinates randomly by a few decimal places
     * The shift in either cardinal direction is no greater than 0.001 degrees latitude/longitude
     * The purpose is to randomize locations that fall on the same coordinate
     * So if two users live in the same city, their pins will be distinguishable
     * @param location original location
     * @return location near to the original with randomly offset coordinates
     */
    public Location obfuscateLocation(Location location) {
        double maxOffset = 0.005;
        // do not obfuscate coordinates that could potentially become invalid (off the map)
        if (Math.abs(location.getLatitude()) > (90.0 - 0.25)) return location;
        if (Math.abs(location.getLongitude()) > (180.0 - 0.25)) return location;
        // random number is between 0 and 1
        // multiply to ensure it's between 0 and 0.25
        double latOffset = new Random().nextDouble() * maxOffset;
        double lonOffset = new Random().nextDouble() * maxOffset;
        // randomly negate the offsets
        if (new Random().nextBoolean()) latOffset = -latOffset;
        if (new Random().nextBoolean()) lonOffset = -lonOffset;
        // shift latitude and longitude by their respective offsets
        location.setLatitude(location.getLatitude() + latOffset);
        location.setLongitude(location.getLongitude() + lonOffset);
        return location;
    }

}
