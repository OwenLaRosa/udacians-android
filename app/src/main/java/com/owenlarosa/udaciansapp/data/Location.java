package com.owenlarosa.udaciansapp.data;

/**
 * Created by Owen LaRosa on 11/18/16.
 */

public class Location {

    private String location;
    private double longitude;
    private double latitude;

    public Location() {}

    public Location(String location, double longitude, double latitude) {
        this.location = location;
        this.longitude = longitude;
        this.latitude = latitude;
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
