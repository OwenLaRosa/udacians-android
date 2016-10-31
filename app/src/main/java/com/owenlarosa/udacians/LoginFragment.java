package com.owenlarosa.udacians;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

    @BindView(R.id.login_email_edit_text)
    EditText emailEditText;
    @BindView(R.id.login_password_edit_text)
    EditText passwordEditText;

    private Unbinder mUnbinder;

    public LoginFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @OnClick(R.id.login_auth_button)
    void loginTapped() {

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
            // build the url request
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .build();
            // localhost IP address recognized by Genymotion emulator
            Request request = new Request.Builder()
                    .url("http://10.0.3.2:8080/_ah/api/myApi/v1/session")
                    .post(formBody)
                    .build();
            String loginResult = "";
            try {
                // execute request and parse the result
                Response response = mClient.newCall(request).execute();
                JSONObject root = new JSONObject(response.body().string());
                boolean success = root.getBoolean("success");
                if (success) {
                    // if login is successful, pass auth token as result
                    String token = root.getString("token");
                    return token;
                }
                // return nothing if the login failed, no token
                return null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null) {
                // received the token, proceed to authenticate wit Firebase
            }
        }
    }

}
