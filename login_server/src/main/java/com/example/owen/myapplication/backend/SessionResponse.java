package com.example.owen.myapplication.backend;

/**
 * Model of JSON returned by the "session" method
 */
public class SessionResponse {

    private boolean success;
    private String token;

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}