/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.example.owen.myapplication.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
    private static final String KEY_STATUS = "status";

    // parameters for Udacity login
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "password";

    private static final String FIREBASE_SECRET = "BHLbOrX85fC03kD8U5fOPfpMLwu1GcYNyZJVIAZE";

    // whether or not the Firebase app has been initialized
    private boolean mInitialized = false;
    
    @ApiMethod(name = "session", path = "session", httpMethod = ApiMethod.HttpMethod.POST)
    public SessionResponse session(@Named("username") String username, @Named("password") String password) {
        SessionResponse result = new SessionResponse();
        // test values to return even if the login fails
        // this is to test the 500 error if the rest of the code isn't working

        try {
            // create JSON body for post request
            String jsonBody = String.format("{\"udacity\": {\"username\": \"%s\", \"password\": \"%s\"}}",
                    username,
                    password);

            // perform the post request
            String response = taskForPost(UDACITY_SESSION_URL, jsonBody);
            // Udacity API responses start at 5th character
            String trimmedResponse = response.substring(4);

            JSONObject root = new JSONObject(trimmedResponse);
            if (root.has(KEY_STATUS)) {
                // return specific code if there was an error
                result.setCode(root.getInt(KEY_STATUS));
            } else {
                // request was successful
                result.setCode(200);
                JSONObject account = root.getJSONObject(KEY_ACCOUNT);
                boolean registered = account.getBoolean(KEY_REGISTERED);
                String key = account.getString(KEY_KEY);
                // specify whether or not the login succeeded
                if (registered) {
                    // user logged in successfully, create their token
                    InputStream cred = getClass().getResourceAsStream("/serviceAccountCredentials.json");
                    FirebaseOptions options = new FirebaseOptions.Builder()
                            .setServiceAccount(cred)
                            .setDatabaseUrl("https://udacians.firebaseio.com/")
                            .build();
                    if (!mInitialized) {
                        // Firebase app should only be initialized the first time
                        FirebaseApp.initializeApp(options);
                        mInitialized = true;
                    }
                    String customToken = FirebaseAuth.getInstance().createCustomToken(key);
                    result.setToken(customToken);
                }
            }
        } catch (IOException e) {
            // problem with udacity or firebase, bad gateway
            result.setCode(1337);
        } catch (JSONException e) {
            // problem with this server, internal server error
            result.setCode(500);
        }
        return result;
    }

    // performs a post request
    // credit: http://stackoverflow.com/questions/21212674/android-java-lang-illegalstateexception-already-connected
    public static String taskForPost(final String url, final String body) throws IOException {
        final String charset = "UTF-8";
        // Create the connection
        HttpURLConnection connection = (HttpURLConnection) new URL (url).openConnection();
        // setDoOutput(true) implicitly set's the request type to POST
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-type", "application/json");

        // Write to the connection
        OutputStream output = connection.getOutputStream();
        output.write(body.getBytes(charset));
        output.close();

        // Check the error stream first, if this is null then there have been no issues with the request
        InputStream inputStream = connection.getErrorStream();
        if (inputStream == null)
            inputStream = connection.getInputStream();

        // Read everything from our stream
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(inputStream, charset));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = responseReader.readLine()) != null) {
            response.append(inputLine);
        }
        responseReader.close();

        return response.toString();
    }

}
