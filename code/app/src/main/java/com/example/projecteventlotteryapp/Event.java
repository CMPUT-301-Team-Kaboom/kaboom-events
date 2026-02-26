package com.example.projecteventlotteryapp;

import com.google.firebase.Timestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Event {
    private String name;
    private int attendeesLimit;
    private int waitlistLimit;
    private boolean geolocationEnabled;
    private Timestamp drawDate;
    private Timestamp registrationStartDate;
    private Timestamp registrationEndDate;
    private ArrayList<String> tagsList;
    private EntrantList waitlist;
    private EntrantList invited;
    private EntrantList declined;
    private EntrantList enrolled;
    private String description;
    // private QRCode
    // private location
    // private map
    // private image

    /**
     * Constructor for Event class
     *
     * TODO: Remove waitlistLimit from UI Diagram
     * @param name
     * @param registrationStartDate
     * @param registrationEndDate
     * @param drawDate
     */
    public Event(
            String name,
            Timestamp registrationStartDate,
            Timestamp registrationEndDate,
            Timestamp drawDate
    ) {
        this.name = name;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.drawDate = drawDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAttendeesLimit() {
        return attendeesLimit;
    }

    public void setAttendeesLimit(int attendeesLimit) {
        this.attendeesLimit = attendeesLimit;
    }

    public int getWaitlistLimit() {
        return waitlistLimit;
    }

    public void setWaitlistLimit(int waitlistLimit) {
        this.waitlistLimit = waitlistLimit;
    }

    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }

    public Timestamp getDrawDate() {
        return drawDate;
    }

    public void setDrawDate(Timestamp drawDate) {
        this.drawDate = drawDate;
    }

    public Timestamp getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(Timestamp registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public Timestamp getRegistrationEndDate() {
        return registrationEndDate;
    }

    public void setRegistrationEndDate(Timestamp registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    public ArrayList<String> getTagsList() {
        return tagsList;
    }

    public void setTagsList(ArrayList<String> tagsList) {
        this.tagsList = tagsList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
