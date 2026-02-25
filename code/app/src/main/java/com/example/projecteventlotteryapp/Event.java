package com.example.projecteventlotteryapp;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Event {
    private String name;
    private int attendeesLimit;
    private int waitlistLimit;
    private boolean geolocationEnabled;
    private LocalDateTime drawDate;
    private LocalDateTime registrationStartDate;
    private LocalDateTime registrationEndDate;
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

    public LocalDateTime getDrawDate() {
        return drawDate;
    }

    public void setDrawDate(LocalDateTime drawDate) {
        this.drawDate = drawDate;
    }

    public LocalDateTime getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(LocalDateTime registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public LocalDateTime getRegistrationEndDate() {
        return registrationEndDate;
    }

    public void setRegistrationEndDate(LocalDateTime registrationEndDate) {
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
