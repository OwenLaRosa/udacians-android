package com.owenlarosa.udacians.data;

/**
 * Created by Owen LaRosa on 11/18/16.
 */

public class TopicLocation {

    private String name;
    private String author;
    private double longitude;
    private double latitude;

    public TopicLocation() {}

    public TopicLocation(String name, String author, double longitude, double latitude) {
        this.name = name;
        this.author = author;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
