/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.example.owen.myapplication.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import javax.inject.Named;

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

    @ApiMethod(name = "session", path = "session", httpMethod = ApiMethod.HttpMethod.POST)
    public SessionResponse session(@Named("username") String username, @Named("password") String password) {
        SessionResponse response = new SessionResponse();
        response.setSuccess(true);
        response.setToken("");
        return response;
    }

}
