package com.owenlarosa.udacians;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Owen LaRosa on 11/21/16.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // enable persistence only after the first launch
        // referenced: http://stackoverflow.com/questions/37753991/com-google-firebase-database-databaseexception-calls-to-setpersistenceenabled
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
