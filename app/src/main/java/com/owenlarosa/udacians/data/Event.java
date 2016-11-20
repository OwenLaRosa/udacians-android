package com.owenlarosa.udacians.data;

/**
 * Created by Owen LaRosa on 11/19/16.
 */

public class Event {

    private String name;
    private String place;
    private String about;

    public Event() {}

    public Event(String name, String place, String about) {
        this.name = name;
        this.place = place;
        this.about = about;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
