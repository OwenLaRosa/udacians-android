package com.example.owen.myapplication.backend;

/**
 * Model of JSON returned by the "session" method
 */
public class SessionResponse {

    private int code;
    private String token;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}