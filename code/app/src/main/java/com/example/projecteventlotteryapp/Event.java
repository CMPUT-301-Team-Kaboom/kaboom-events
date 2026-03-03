package com.example.projecteventlotteryapp;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public class Event {
    private String eventId;
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
    }

    public Event(
            String eventId,
            String name,
            LocalDate registrationStartDate,
            LocalDate registrationEndDate,
            LocalDateTime drawDate,
            int attendeesLimit
    ) {
        this.eventId = eventId;
        this.name = name;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.drawDate = drawDate;
        this.attendeesLimit = attendeesLimit;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    public boolean waitlistContains(Entrant entrant) {
        return waitlist.contains(entrant);
    }

    public static Event fetchEventFromSnapshot(DocumentSnapshot snapshot) {
        // get string fields
        String eventId = snapshot.getId();
        String description = snapshot.getString("description");
        String location = snapshot.getString("location");
        String name = snapshot.getString("name");
        String qrcodePath = snapshot.getString("qrCodePath");

        // get number fields
        int attendeesLimit = fetchInt(snapshot, "entrantsLimit");
        int waitlistLimit = fetchInt(snapshot, "waitlistLimit");

        // get boolean field
        boolean geolocationEnabled = fetchBoolean(snapshot, "geoLocationEnabled");

        // get timestamp fields
        LocalDateTime drawDate = fetchLocalDateTime(snapshot, "drawDate");
        LocalDate registrationEndDate = fetchLocalDate(snapshot, "registrationEndDate");
        LocalDate registrationStartDate = fetchLocalDate(snapshot, "registrationStartDate");

        // get array field
        ArrayList<String> tagsList = fetchStringArrayList(snapshot, "tags");


        Event event = new Event (
                eventId,
                name,
                registrationStartDate,
                registrationEndDate,
                drawDate,
                attendeesLimit
        );

        event.setDescription(description);
        // event.setLocation(location);
        event.setGeolocationEnabled(geolocationEnabled);
        event.setTagsList(tagsList);
        event.setWaitlistLimit(waitlistLimit);

        Log.d("Event", "Fetched event.\nEventId: " + eventId + "\nname: " + name);
        return event;
    }

    private static int fetchInt(DocumentSnapshot snapshot, String field) {
        Long value = snapshot.getLong(field);

        if (value != null) {
            return value.intValue();
        } else {
            return -1;
        }
    }

    private static LocalDate fetchLocalDate(DocumentSnapshot snapshot, String field) {
        /*
        The following code is adapted from...
        Author: Ruslan https://stackoverflow.com/users/2032701/ruslan
        Title: "How to convert java.sql.timestamp to LocalDate (java8) java.time?"
        Answer: https://stackoverflow.com/a/57101544
        Date: 2019-07-18
        Retrieved: 2026-02-28
        License: CC-BY-SA 4.0
        */
        Timestamp value = snapshot.getTimestamp(field);

        if (value != null) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

    private static LocalDateTime fetchLocalDateTime(DocumentSnapshot snapshot, String field) {
        Timestamp value = snapshot.getTimestamp(field);

        if (value != null) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else {
            return null;
        }
    }

    private static boolean fetchBoolean(DocumentSnapshot snapshot, String field) {
        Boolean value = snapshot.getBoolean(field);

        if (value != null) {
            return value.booleanValue();
        } else {
            return false;
        }
    }

    private static ArrayList<String> fetchStringArrayList(DocumentSnapshot snapshot, String field) {
        /*
        The following code is adapted from...
        Author: Doug Stevenson https://stackoverflow.com/users/807126/doug-stevenson
        Title: "How to get an array from Firestore?"
        Answer: https://stackoverflow.com/a/50236950
        Date: 2018-05-08
        Retrieved: 2026-02-28
        License: CC-BY-SA 4.0
        */
        Object value = snapshot.get(field);

        if (value instanceof ArrayList) {
            return (ArrayList<String>) value;
        } else {
            return new ArrayList<String>();
        }
    }

    public boolean invitedListContains(Entrant user) {
        return waitlist.contains(user);
    }
}
