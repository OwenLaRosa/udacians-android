package com.owenlarosa.udacians.data;

import static android.R.string.ok;

/**
 * Created by Owen LaRosa on 11/19/16.
 */

public class Event {

    private String name;
    private String place;
    private String day;
    private String time;
    private String about;

    public Event() {}

    public Event(String name, String place, String day, String time, String about) {
        this.name = name;
        this.place = place;
        this.day = day;
        this.time = time;
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

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
