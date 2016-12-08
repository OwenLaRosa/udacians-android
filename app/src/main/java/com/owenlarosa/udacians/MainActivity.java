package com.owenlarosa.udacians;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    private static final String FRAGMENT_TAG = "FTAG";

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.view_container)
    FrameLayout viewContainer;
    @BindView(R.id.left_drawer)
    ListView navigationDrawer;

    Unbinder unbinder;

    // listen for authentication events
    FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);

        attemptLogin();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user has logged in, must notify adapter so nav drawer's header is populated
                    ((NavigationDrawerAdapter) navigationDrawer.getAdapter()).notifyDataSetChanged();
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);

        // populate the nav drawer with its static content
        navigationDrawer.setAdapter(new NavigationDrawerAdapter(this));
        navigationDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            private View lastSelectedView;

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    // show user's profile info
                } else if (i == 9) {
                    // logout button tapped
                    logout();
                } else {
                    if (lastSelectedView == view) {
                        // tab currently selected, just return
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return;
                    }

                    // reset color of previous view, highlight selected view's background
                    if (lastSelectedView != null) {
                        lastSelectedView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }
                    view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    lastSelectedView = view;

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
    }

    /**
     * Signs out the current user and presents the login screen
     */
    private void logout() {
        // logout button
        PersistentCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));
        cookieJar.clear();
        FirebaseAuth.getInstance().signOut();
        presentLoginScreen();
    }

}
