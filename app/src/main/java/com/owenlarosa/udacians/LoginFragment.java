package com.owenlarosa.udacians;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Owen LaRosa on 10/30/16.
 */

public class LoginFragment extends Fragment {

    private static final String LOG_TAG = LoginFragment.class.getSimpleName();

    @BindView(R.id.login_email_edit_text)
    EditText emailEditText;
    @BindView(R.id.login_password_edit_text)
    EditText passwordEditText;
    String authToken = "";

    private Unbinder mUnbinder;

    // used to monitor firebase authentication status
    FirebaseAuth.AuthStateListener mAuthStateListener;

    public LoginFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // store the auth token to speed up future logins
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                    editor.putString(getString(R.string.pref_auth_token), authToken);
                    editor.apply();
                    getActivity().finish();
                    // login was successful
                    Log.d(LOG_TAG, "firebase authentication succeeded" + user.getUid());
                } else {
                    // login failed, show error message
                    Log.d(LOG_TAG, "firebase authentication failed");
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);

        return rootView;
    }

    @OnClick(R.id.login_auth_button)
    void loginTapped() {
        Log.d(LOG_TAG, emailEditText.getText().toString());
        Log.d(LOG_TAG, passwordEditText.getText().toString());
        new LoginTask().execute(emailEditText.getText().toString(), passwordEditText.getText().toString());
    }

    /**
     * Get a the session and authenticate with Firebase
     * First string should be the username
     * Second string should be the password
     * If successful, use token to login to the database
     * Otherwise, display an error message to the user
     */
    private class LoginTask extends AsyncTask<String, Void, String> {

        private OkHttpClient mClient = new OkHttpClient();

        @Override
        protected String doInBackground(String... strings) {
            // get the username and password that were passed in
            String username = strings[0];
            String password = strings[1];
            try {
                if (!getXSRFToken(username, password)) {
                    // immediately return if the login fails
                    Log.d(LOG_TAG, "failed to get xsrf token");
                    return null;
                }
                // if successful, proceed to get the Firebase auth token
                // build the url request
                RequestBody formBody = new FormBody.Builder()
                        .add("username", username)
                        .add("password", password)
                        .build();
                // localhost IP address recognized by Genymotion emulator
                Request request = new Request.Builder()
                        .url("http://10.0.0.5:8080/_ah/api/myApi/v1/session")
                        .post(formBody)
                        .build();
                String loginResult = "";
                Response response = mClient.newCall(request).execute();
                JSONObject root = new JSONObject(response.body().string());
                int code = root.getInt("code");
                if (code == 200) {
                    // if login is successful, pass auth token as result
                    String token = root.getString("token");
                    return token;
                }
                // return nothing if the login failed, no token
                return null;
            } catch (JSONException e) {
                // error occurred while connecting to Udacity API
                Log.d(LOG_TAG, "json exception occurred: " + e.getLocalizedMessage());
                return null;
            } catch (IOException e) {
                Log.d(LOG_TAG, "i/o exception occurred: " + e.getLocalizedMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                // received the token, proceed to authenticate with Firebase
                Log.d(LOG_TAG, "logging in with firebase");
                Log.d(LOG_TAG, "this is the token");
                authToken = s;
                Log.d(LOG_TAG, authToken);
                FirebaseAuth.getInstance().signInWithCustomToken(authToken);
            } else {
                Log.d(LOG_TAG, "token is null");
            }
        }

        /**
         * Authenticate with Udacity to download necessary cookies
         * The cookies allow access to more detailed enrollment data
         * @param username email address of Udacity account
         * @param password password of Udacity account
         * @return true if login succeeds, otherwise false
         * @throws IOException Network error
         * @throws JSONException Parsing error
         */
        private boolean getXSRFToken(String username, String password) throws IOException, JSONException {
            // build request body
            RequestBody formBody = new FormBody.Builder()
                    .add("udacity", String.format("{\"username\": \"%s\", \"password\": \"%s\"}",
                            username,
                            password))
                    .build();
            Request request = new Request.Builder()
                    .url("https://www.udacity.com/api/session")
                    .post(formBody)
                    .build();
            // perform the request
            Response response = mClient.newCall(request).execute();
            Log.d(LOG_TAG, "response: " + response.body().string());
            // return whether or not the login was successful
            Log.d(LOG_TAG, String.format("response: %d", response.code()));
            return response.code() == 200;
        }
    }

}
