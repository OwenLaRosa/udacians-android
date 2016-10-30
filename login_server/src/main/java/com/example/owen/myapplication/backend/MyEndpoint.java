/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.example.owen.myapplication.backend;

import com.firebase.security.token.TokenGenerator;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "myApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.myapplication.owen.example.com",
                ownerName = "backend.myapplication.owen.example.com",
                packagePath = ""
        )
)
public class MyEndpoint {

    // Udacity login endpoing
    private static final String UDACITY_SESSION_URL = "https://www.udacity.com/api/session";

    // json parsing keys for session method
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_REGISTERED = "registered";
    private static final String KEY_KEY = "key";
    private static final String KEY_SESSION = "session";
    private static final String KEY_ID = "id";

    // parameters for Udacity login
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "password";

    private static final String FIREBASE_SECRET = "<FIREBASE_SECRET>";

    //private OkHttpClient mClient = new OkHttpClient();

    @ApiMethod(name = "session", path = "session", httpMethod = ApiMethod.HttpMethod.POST)
    public SessionResponse session(@Named("username") String username, @Named("password") String password) {
        SessionResponse result = new SessionResponse();
        // test values to return even if the login fails
        // this is to test the 500 error if the rest of the code isn't working

        try {
            // attempt to login with user's credentials
            HttpRequest request = new HttpRequest(UDACITY_SESSION_URL);
            // make sure the data is read as JSON
            request.con.addRequestProperty("Accept", "application/json");
            request.con.addRequestProperty("Content-Type", "application/json");
            // set the JSON body for post request
            JSONObject credentials = new JSONObject();
            JSONObject parent = new JSONObject();
            credentials.put(PARAM_USERNAME, username);
            credentials.put(PARAM_PASSWORD, password);
            parent.put("udacity", credentials);
            // set the JSON body
            BufferedWriter jsonBody = new BufferedWriter(new OutputStreamWriter(request.con.getOutputStream()));
            jsonBody.write(parent.toString());
            jsonBody.close();
            String root = request.prepare(HttpRequest.Method.POST).sendAndReadString();
            // return the json to the client for debugging purposes
            result.setToken(root);
            result.setSuccess(true);
            /*JSONObject account = root.getJSONObject(KEY_ACCOUNT);
            boolean registered = account.getBoolean(KEY_REGISTERED);
            String key = account.getString(KEY_KEY);
            // specify whether or not the login succeeded
            result.setSuccess(registered);
            if (registered) {
                // user logged in successfully, create their token
                result.setToken(generateTokenForId(key));
            }*/
        } catch (IOException e) {

        } catch (JSONException e) {

        }
        return result;
    }

    /**
     * Generate an auth token for the user
     * @param userId Unique identifier, same ID used by Udacity
     * @return The user's auth token
     */
    private String generateTokenForId(Integer userId) {
        // based on the example from https://www-staging.firebase.com/docs/android/guide/login/custom.html
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("uid", userId);

        // create this user's token
        TokenGenerator tokenGenerator = new TokenGenerator(FIREBASE_SECRET);
        return tokenGenerator.createToken(payload);
    }

}
