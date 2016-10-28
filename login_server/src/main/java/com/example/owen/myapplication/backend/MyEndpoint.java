/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.example.owen.myapplication.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Named;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "api",
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

    private OkHttpClient mClient = new OkHttpClient();

    @ApiMethod(name = "session", path = "session", httpMethod = ApiMethod.HttpMethod.POST)
    public SessionResponse session(@Named("username") String username, @Named("password") String password) {
        SessionResponse result = new SessionResponse();

        // attempt to login with user's credentials
        RequestBody formBody = new FormBody.Builder()
                .add(PARAM_USERNAME, username)
                .add(PARAM_PASSWORD, password)
                .build();
        Request request = new Request.Builder()
                .url(UDACITY_SESSION_URL)
                .post(formBody)
                .build();
        String loginResult = "";
        try {
            // parse the result
            Response response = mClient.newCall(request).execute();
            loginResult = response.body().string();
            JSONObject root = new JSONObject(loginResult);
            JSONObject account = root.getJSONObject(KEY_ACCOUNT);
            boolean registered = account.getBoolean(KEY_REGISTERED);
            String key = account.getString(KEY_KEY);
            // this will be the actual JWT, use user key for now
            result.setToken(key);
            result.setSuccess(registered);
        } catch (IOException e) {

        } catch (JSONException e) {

        }
        return result;
    }

}
