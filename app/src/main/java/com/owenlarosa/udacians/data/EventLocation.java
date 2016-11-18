package com.owenlarosa.udacians.data;

/**
 * Created by Owen LaRosa on 11/18/16.
 */

public class EventLocation {

    private String title;
    private String name;
    private double longitude;
    private double latitude;

    public EventLocation() {}

    public EventLocation(String title, String name, double longitude, double latitude) {
        this.title = title;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
