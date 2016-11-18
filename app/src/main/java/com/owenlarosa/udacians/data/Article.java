package com.owenlarosa.udacians.data;

import static android.R.attr.author;

/**
 * Created by Owen LaRosa on 11/18/16.
 */

public class Article {

    private String title;
    private String author;
    private String url;
    private double longitude;
    private double latitude;

    public Article() {}

    public Article(String title, String author, String url, double longitude, double latitude) {
        this.title = title;
        this.author = author;
        this.url = url;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

}
