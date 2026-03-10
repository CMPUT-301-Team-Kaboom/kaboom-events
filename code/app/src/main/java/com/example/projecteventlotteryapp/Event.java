package com.example.projecteventlotteryapp;

import android.os.Build;

import com.google.firebase.Timestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Event {
    private String name;
    private int attendeesLimit;
    private int waitlistLimit;
    private boolean geolocationEnabled;
    private LocalDateTime drawDate;
    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
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
            LocalDate registrationStartDate,
            LocalDate registrationEndDate,
            LocalDateTime drawDate,
            int attendeesLimit
    ) {
        this.name = name;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.drawDate = drawDate;
        this.attendeesLimit = attendeesLimit;
        this.waitlist = new EntrantList();
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

    public EntrantList getWaitList(){ return waitlist; }

    public void addToWaitlist(Entrant entrant) {
        if (!waitlist.containsEntrant(entrant)) {
            waitlist.addEntrant(entrant);
        }
    }

    public void removeFromWaitlist(Entrant entrant){
        if (waitlist.containsEntrant(entrant)){
            waitlist.removeEntrant(entrant);
        }
    }

    public boolean isOnWaitlist(Entrant entrant) {return waitlist.containsEntrant(entrant);}

    public boolean isRegistrationOpen(){
        LocalDate today = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            today = LocalDate.now();
            return (today.isEqual(registrationStartDate) || today.isAfter(registrationStartDate)
                    && today.isEqual(registrationEndDate) || today.isBefore(registrationEndDate));
        }
        return false;
    }

    public int getWaitlistLimit() {
        return waitlistLimit;
    }

    public void setWaitlistLimit(int waitlistLimit) {
        this.waitlistLimit = waitlistLimit;
    }

    public boolean isWaitlistFull() {return waitlist.getEntrants().size() >= waitlistLimit; }

    public boolean joinWaitlist(Entrant entrant) {
        if(!isRegistrationOpen()) {return false;}
        if(isOnWaitlist(entrant)) {return false;}
        if(isWaitlistFull()) {return false;}
        waitlist.addEntrant(entrant);
        return true;
    }

    public boolean leaveWaitlist(Entrant entrant) {
        if(!isOnWaitlist(entrant)) {return false;}
        waitlist.removeEntrant(entrant);
        return true;
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

    public LocalDate getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(LocalDate registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public LocalDate getRegistrationEndDate() {
        return registrationEndDate;
    }

    public void setRegistrationEndDate(LocalDate registrationEndDate) {
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
