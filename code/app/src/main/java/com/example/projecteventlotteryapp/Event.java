package com.example.projecteventlotteryapp;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.checkerframework.common.returnsreceiver.qual.This;
import org.w3c.dom.Document;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Model for an Event
 *
 * <p>This class models an event entity stored in Firebase Firestore. It stores
 *  metadata such as registration dates, draw date, attendee limits, organizer
 *  information, and lists of entrants. Instances of this class are used to help display
 *  database information.</p>
 *
 *  TODO: reduce scope of the class and move non-model functionality into a controller class
 */
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
    private String description;
    private String organizerId;
    private String organizerName;
    // private QRCode
    // private location
    // private map
    private String poster;

    private static FirebaseFirestore db;
    static { db = FirebaseFirestore.getInstance(); }
    private DocumentReference eventDoc;

    /**
     * Creates a new Event object with basic event information.
     * <p> This constructor is typically used when creating a new event before
     * it has been stored in the database.</p>
     *
     * @param name name of the event
     * @param registrationStartDate the date when event registration opens
     * @param registrationEndDate the date when event registration closes
     * @param drawDate the date and time when the lottery draw occurs
     * @param attendeesLimit the maximum number of entrants that can attend the event
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

    /**
     * Creates an Event object that corresponds to an existing Firestore document.
     *
     * <p>This constructor initializes the event with a known event ID and
     * creates a reference to the associated Firestore document.</p>
     *
     * @param eventId the unique identifier of the event document in Firestore
     * @param name the name of the event
     * @param registrationStartDate the date when event registration opens
     * @param registrationEndDate the date when event registration closes
     * @param drawDate the date and time when the lottery draw occurs
     * @param attendeesLimit the maximum number of entrants that can attend the event
     */
    public Event(
            String eventId,
            String name,
            LocalDate registrationStartDate,
            LocalDate registrationEndDate,
            LocalDateTime drawDate,
            int attendeesLimit
    ) {
        this(name, registrationStartDate, registrationEndDate, drawDate, attendeesLimit);
        this.eventId = eventId;
        eventDoc = db.collection("events").document(this.eventId);
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

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }
    public void setPoster(String poster) { this.poster = poster; }
    public String getPoster() { return poster; }

    /**
     * Returns the Firestore field name corresponding to a specific entrant list type.
     *
     * <p>This method converts an {@link EntrantListType} enum value into the
     * associated Firestore field used to store that list.</p>
     *
     * TODO: Consider moving to the EntrantListType enum
     *
     * @param type the entrant list type
     * @return the Firestore field name representing the list
     * @throws IllegalArgumentException if the list type is unknown
     */
    private String getListField(EntrantListType type) {
        switch (type) {
            case WAITLIST:
                return "waitlist";
            case INVITED:
                return "invited";
            case DECLINED:
                return "declined";
            case ENROLLED:
                return "enrolled";
            default:
                throw new IllegalArgumentException("Unknown list type: " + type);
        }
    }

    /**
     * Checks whether a specific entrant exists in one of the event's entrant lists.
     *
     * <p>This method retrieves the event document from Firestore and checks if
     * the entrant's user ID exists in the specified list.</p>
     *
     * TODO: Consider turning into a query instead of fetching the eventDoc
     *
     * @param listType the entrant list to check
     * @param entrant the user whose membership in the list is being checked
     * @return a {@link Task} that resolves to {@code true} if the entrant exists
     * in the list, or {@code false} otherwise
     */
    public Task<Boolean> entrantListContains(EntrantListType listType, User entrant) {
        return eventDoc.get().continueWith(task -> {
            if (!task.isSuccessful()) { return false; }

            DocumentSnapshot doc = task.getResult();
            if (!doc.exists()) { return false;}

            ArrayList<String> entrantList = (ArrayList<String>) doc.get(getListField(listType));

            return entrantList != null && entrantList.contains(entrant.getUserId());
        });
    }


    /**
     * Creates an Event object from a Firestore document snapshot.
     *
     * <p>This method extracts fields from the snapshot and converts them into
     * the appropriate Java types used by the Event class.</p>
     *
     * @param snapshot the Firestore document snapshot representing the event
     * @return a populated Event object
     */
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

        DocumentReference organizer = snapshot.getDocumentReference("organizer");

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

        if (organizer != null) {
            organizer.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        String organizerId = documentSnapshot.getId();
                        String organizerName = documentSnapshot.getString("name");
                        event.setOrganizerId(organizerId);
                        event.setOrganizerName(organizerName);
                    }
                }
            });
        }

        DocumentReference posterRef = snapshot.getDocumentReference("poster");
        if (posterRef != null){
            posterRef.get().addOnSuccessListener(doc -> {
                if (doc.exists()){
                    String imageUrl = doc.getString("url");
                    event.setPoster(imageUrl);
                }
            });
        }

        Log.d("Event", "Fetched event.\nEventId: " + eventId + "\nname: " + name);
        return event;
    }

    /**
     * Fetches an int value from an event document snapshot
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @return the value of the fetched field
     */
    private static int fetchInt(DocumentSnapshot snapshot, String field) {
        Long value = snapshot.getLong(field);

        if (value != null) {
            return value.intValue();
        } else {
            return -1;
        }
    }

    /**
     * Fetches a localDate value from an event document snapshot
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @return the value of the fetched field
     */
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

    /**
     * Fetches a LocalDateTime value from an event document snapshot
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @return the value of the fetched field
     */
    private static LocalDateTime fetchLocalDateTime(DocumentSnapshot snapshot, String field) {
        Timestamp value = snapshot.getTimestamp(field);

        if (value != null) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else {
            return null;
        }
    }

    /**
     * Fetches a boolean value from an event document snapshot
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @return the value of the fetched field
     */
    private static boolean fetchBoolean(DocumentSnapshot snapshot, String field) {
        Boolean value = snapshot.getBoolean(field);

        if (value != null) {
            return value.booleanValue();
        } else {
            return false;
        }
    }

    /**
     * Fetches a String list value from an event document snapshot and returns an ArrayList representation of it
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @return Arraylist of the string list
     */
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
}
