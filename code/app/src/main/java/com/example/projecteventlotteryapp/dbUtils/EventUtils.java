package com.example.projecteventlotteryapp.dbUtils;

import android.util.Log;

import com.example.projecteventlotteryapp.EntrantListType;
import com.example.projecteventlotteryapp.Event;
import com.example.projecteventlotteryapp.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to provide database operations on the Event class
 *
 * <p>This class is designed to encapsulated all database operates for Events. An instance of this
 * class should be declared with an injection of the database instance that the operations are
 * performed on.</p>
 *
 * Example usage:
 * EventUtils eventUtils = new EventUtils(FirebaseFirestore.getInstance());
 *
 * @see Event
 */
public class EventUtils {

    private FirebaseFirestore db;

    public EventUtils(FirebaseFirestore db){
        this.db = db;
    }

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
    public String getListField(EntrantListType type) {
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
    public Task<Boolean> entrantListContains(EntrantListType listType, User entrant, String eventId) {
        DocumentReference eventDoc = db.collection("events").document(eventId);
        return eventDoc.get().continueWith(task -> {
            if (!task.isSuccessful()) { return false; }

            DocumentSnapshot doc = task.getResult();
            if (!doc.exists()) { return false;}

            ArrayList<String> entrantList = (ArrayList<String>) doc.get(getListField(listType));

            return entrantList != null && entrantList.contains(entrant.getUserId());
        });
    }

    /**
     * Adds an entrant to a specified entrant list for this event.
     *
     * <p>This method updates the Firestore event document by adding the entrant's
     * user ID to the corresponding list field using {@code FieldValue.arrayUnion}.
     * If the user ID already exists in the list, Firestore will not add a duplicate.</p>
     *
     * @param listType the entrant list to which the user should be added
     * @param entrant the user being added to the list
     * @return a {@link Task} representing the asynchronous Firestore update operation
     */
    public Task<Void> addToEntrantList(EntrantListType listType, User entrant, String eventId) {
        Log.d("AddToEntrantList", String.format("Type: %s | userId: %s",
                listType.toString(),
                entrant.getUserId())
        );

        DocumentReference eventDoc = db.collection("events").document(eventId);

        HashMap<String, Object> updates = new HashMap<>();
        String listField = getListField(listType);
        updates.put(listField, FieldValue.arrayUnion(entrant.getUserId()));

        if (listField.equals("waitlist")) {
            updates.put("waitlistSize", FieldValue.increment(1));
        }
        return eventDoc.update(updates);
    }

    /**
     * Removes an entrant from a specified entrant list for this event.
     *
     * <p>This function updates the Firestore event document by removing an entrant's userID from
     * the corresponding list field using {@code FieldValue.arrayRemove}.</p>
     * @param listType the list to remove the entrant from
     * @param entrant the entrant to add to the list
     * @return a {@link Task} representing the asynchronous Firestore update operation
     */
    public Task<Void> removeFromEntrantList(EntrantListType listType, User entrant, String eventId) {
        DocumentReference eventDoc = db.collection("events").document(eventId);

        HashMap<String, Object> updates = new HashMap<>();
        String listField = getListField(listType);
        updates.put(listField, FieldValue.arrayRemove(entrant.getUserId()));
        if (listField.equals("waitlist")) {
            updates.put("waitlistSize", FieldValue.increment(-1));
        }

        return eventDoc.update(updates);
    }

    /**
     * Fetches the organizer document from Firestore and updates the given Event with organizer info.
     *
     * @param event the Event to update
     * @param organizerRef the DocumentReference to the organizer
     * @return a {@link Task} representing the asynchronous Firestore update operation
     */
    public Task<Void> fetchOrganizerForEvent(Event event, DocumentReference organizerRef) {
        if (organizerRef == null) return Tasks.forResult(null);

        return organizerRef.get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                /*
                The following code is adapted from...
                Source: https://firebase.google.com/docs/firestore/query-data/get-data
                Title: "Get data with Cloud Firestore"
                Retrieved: 2026-03-03
                */
                DocumentSnapshot organizerDoc = task.getResult();
                event.setOrganizerId(organizerDoc.getId());
                event.setOrganizerName(organizerDoc.getString("name"));
            }
            return null;
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
    public Event fetchEventFromSnapshot(DocumentSnapshot snapshot) {
        // get string fields
        String eventId = snapshot.getId();
        String description = snapshot.getString("description");
        String location = snapshot.getString("location");
        String name = snapshot.getString("name");
        String qrcodePath = snapshot.getString("qrCodePath");

        // get number fields
        int attendeesLimit = fetchInt(snapshot, "entrantsLimit");
        int waitlistLimit = fetchInt(snapshot, "waitlistLimit");
        int waitlistSize = fetchInt(snapshot, "waitlistSize");

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
        event.setWaitlistSize(waitlistSize);
        // event.setLocation(location);
        event.setGeolocationEnabled(geolocationEnabled);
        event.setTagsList(tagsList);
        event.setWaitlistLimit(waitlistLimit);

        Log.d("Event", "Fetched event.\nEventId: " + eventId + "\nname: " + name);
        return event;
    }

    /**
     * Fetches an int value from an event document snapshot
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @return the value of the fetched field
     */
    private int fetchInt(DocumentSnapshot snapshot, String field) {
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
    private LocalDate fetchLocalDate(DocumentSnapshot snapshot, String field) {
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
    private LocalDateTime fetchLocalDateTime(DocumentSnapshot snapshot, String field) {
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
    private boolean fetchBoolean(DocumentSnapshot snapshot, String field) {
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
    private ArrayList<String> fetchStringArrayList(DocumentSnapshot snapshot, String field) {
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

    /**
     * Creates and uploads a new Event database object
     *
     * <p>Uses an Event instance and uploads it to the database. Only populates the following attributes:
     * organizer
     * name
     * drawDate
     * registrationEndDate
     * registrationStartDate
     * drawDate
     * entrantsLimit
     *
     * All other values are initialized to null or the equivalent default value
     * Further event refinement is handled by event editing</p>
     *
     * @param event the Event that is to be uploaded
     * @param organizerId the user ID of the organizer of the event
     */
    public void createNewEventDbItem(Event event, String organizerId) {
        // Inputted values
        HashMap<String, Object> eventData = new HashMap<>();

        DocumentReference organizerRef =
                db.collection("organizers").document(organizerId);
        DocumentReference defaultPoster =
                db.collection("posters").document("default_poster");
        eventData.put("organizer", organizerRef);
        eventData.put("name", event.getName());
        eventData.put("poster", defaultPoster);

        ZoneId zoneId = ZoneId.systemDefault();
        // TODO: update to grab system zoneid
        eventData.put(
                "drawDate",
                FirestoreUtils.localDateTimeToTimestamp(
                        event.getDrawDate(),
                        zoneId
                )
        );
        eventData.put(
                "registrationEndDate",
                FirestoreUtils.localDateToTimestamp(
                        event.getRegistrationEndDate(),
                        zoneId
                )
        );
        eventData.put(
                "registrationStartDate",
                FirestoreUtils.localDateToTimestamp(
                        event.getRegistrationStartDate(),
                        zoneId
                )
        );
        eventData.put("entrantsLimit", event.getAttendeesLimit());

        // setting null values
        eventData.put("waitlistSize", 0);
        eventData.put("description", null);
        eventData.put("geoLocationEnabled", false);
        eventData.put("location", null);
        eventData.put("qrCodePath", null);
        eventData.put("tags", null);
        eventData.put("waitlistLimit", -1);  // -1 indicates no limit
        eventData.put("waitlist", new ArrayList<>());
        eventData.put("enrolled", new ArrayList<>());
        eventData.put("invited", new ArrayList<>());
        eventData.put("declined", new ArrayList<>());

        db.collection("events")
                .add(eventData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Document added to events with id: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding document", e);
                });
    }

    /**
     * Takes an existing event and updates its fields in the database
     *
     * <p>The method assumes that the event already exists, it populates everything except for:
     * location
     * qr-code
     * poster
     * waitlist
     * invited
     * enrolled
     * declined
     * organizer</p>
     *
     * @param updates contains a map of all the edits being made to the event
     * @param eventId ID of the event being updated
     */
    public void updateEventInDB(Map<String, Object> updates, String eventId){
        db.collection("events").document(eventId).update(updates);
    }
}
