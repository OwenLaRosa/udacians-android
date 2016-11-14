package com.owenlarosa.udacians;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);

        // populate the nav drawer with its static content
        navigationDrawer.setAdapter(new NavigationDrawerAdapter(this));
        navigationDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            private View lastSelectedView;

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    // show user's profile info
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
                            // show map fragment
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
}
