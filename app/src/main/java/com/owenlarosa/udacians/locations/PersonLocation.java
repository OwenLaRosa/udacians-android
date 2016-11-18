package com.owenlarosa.udacians.locations;

/**
 * Created by Owen LaRosa on 11/18/16.
 */

public class PersonLocation {

    private String name;
    private String location;
    private double longitude;
    private double latitude;

    public PersonLocation() {}

    public PersonLocation(String name, String location, double longitude, double latitude) {
        this.name = name;
        this.location = location;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
