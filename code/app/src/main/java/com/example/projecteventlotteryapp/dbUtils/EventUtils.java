package com.example.projecteventlotteryapp.dbUtils;

import android.util.Log;
import android.widget.Toast;

import com.example.projecteventlotteryapp.Enums.EntrantListType;
import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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
     * @param type the entrant list type
     * @return the Firestore field name representing the list
     * @throws IllegalArgumentException if the list type is unknown
     */
    public String getDbEntrantListFieldName(EntrantListType type) {
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

            ArrayList<String> entrantList = (ArrayList<String>) doc.get(getDbEntrantListFieldName(listType));

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
        String listField = getDbEntrantListFieldName(listType);
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
        String listField = getDbEntrantListFieldName(listType);
        updates.put(listField, FieldValue.arrayRemove(entrant.getUserId()));
        if (listField.equals("waitlist")) {
            updates.put("waitlistSize", FieldValue.increment(-1));
        }

        return eventDoc.update(updates);
    }

    /**
     * This function moves a single entrant to toList from fromList
     *
     * <p>This function moves a single entrant across two lists. Using this method is preferred to
     * using the addToEntrantList and removeFromEntrantList together because it guarantees atomicity.
     * ie. ensures that both lists are simultaneously updated.
     *
     * Note: MUST be sure that the entrantId is already in the toList. </p>
     * @param eventId eventId of the event
     * @param entrantId id of the user to be moved
     * @param toList the EntrantListType of the list the user is currently in
     * @param fromList the EntrantListType of the list the user is moving to
     * @return
     */
    private Task<Void> moveEntrantAcrossLists(String eventId, String entrantId, EntrantListType toList, EntrantListType fromList) {
        DocumentReference eventDoc = db.collection("events").document(eventId);

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(getDbEntrantListFieldName(toList), FieldValue.arrayUnion(entrantId));
        updates.put(getDbEntrantListFieldName(fromList), FieldValue.arrayRemove(entrantId));

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
        int attendeesLimit = FirestoreUtils.fetchInt(snapshot, "entrantsLimit", -1);
        int waitlistLimit = FirestoreUtils.fetchInt(snapshot, "waitlistLimit", -1);
        int waitlistSize = FirestoreUtils.fetchInt(snapshot, "waitlistSize", -1);

        // get boolean field
        boolean geolocationEnabled = FirestoreUtils.fetchBoolean(snapshot, "geoLocationEnabled", false);
        boolean EventIsPrivate = FirestoreUtils.fetchBoolean(snapshot, "isPrivate", false);

        // get timestamp fields
        LocalDateTime drawDate = FirestoreUtils.fetchLocalDateTime(snapshot, "drawDate");
        LocalDate registrationEndDate = FirestoreUtils.fetchLocalDate(snapshot, "registrationEndDate");
        LocalDate registrationStartDate = FirestoreUtils.fetchLocalDate(snapshot, "registrationStartDate");

        // get array field
        ArrayList<String> tagsList = FirestoreUtils.fetchStringArrayList(snapshot, "tags");

        DocumentReference organizer = snapshot.getDocumentReference("organizer");

        Event event = new Event (
                eventId,
                name,
                registrationStartDate,
                registrationEndDate,
                drawDate,
                attendeesLimit,
                EventIsPrivate
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
     * All other values are initialized to null or the equivalent default value</p>
     *
     * <p>This function is ONLY to be used to create a new Event db entry with the values required at
     * creation. This is to ensure all initial values are set accordingly. Further event attribute
     * refinements are handled using the associated Event db entry update methods.</p>
     *
     * @param event the Event that is to be uploaded
     * @param organizerId the user ID of the organizer of the event
     */
    public void createNewEventDbItem(Event event, String organizerId) {
        HashMap<String, Object> eventData = new HashMap<>();

        // Helper variable initialization
        DocumentReference organizerRef =
                db.collection("organizers").document(organizerId);
        DocumentReference defaultPoster =
                db.collection("posters").document("default_poster");
        ZoneId zoneId = ZoneId.systemDefault();

        // Mandatory values
        eventData.put("organizer", organizerRef);
        eventData.put("name", event.getName());
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
        eventData.put("isPrivate", event.isPrivate());

        // setting null values these
        eventData.put("poster", defaultPoster);
        eventData.put("waitlistSize", 0);
        eventData.put("description", null);
        eventData.put("geoLocationEnabled", false);
        eventData.put("location", null);
        eventData.put("qrCodePath", null);
        eventData.put("tags", null);
        eventData.put("waitlistLimit", -1);  // -1 indicates no limit
        eventData.put("waitlist", new ArrayList<>());
        eventData.put("enrolled", new ArrayList<>());
        eventData.put("invited",  new ArrayList<>());
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

    /**
     * Fetches the size of an EntrantList for a given event (number of entrants currently in the list)
     * @param eventId the eventId of the event
     * @param listType the type of desired list
     * @return the size of the list
     */
    public Task<Integer> getEntrantListSize(String eventId, EntrantListType listType) {
        DocumentReference eventDoc = db.collection("events").document(eventId);

        return eventDoc.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            DocumentSnapshot doc = task.getResult();
            if (!doc.exists()) {
                Log.e("getEntrantListSize", "Event document does not exist: EventId: " + eventId);
                throw new Exception("Event document missing");
            }

            List<String> rawList = (List<String>) doc.get(getDbEntrantListFieldName(listType));
            if (rawList == null) {
                Log.e("getEntrantList", String.format("No Entrants found for event. EventId: %s | type: %s",
                        eventId,
                        listType.toString()
                ));
                return 0;
            }

            return ((List<?>) rawList).size();
        });
    }

    /**
     * Fetches the contents of an entrantList
     * @param eventId the eventId of the event
     * @param listType the EntrantListType of the desired list
     * @return on success, an ArrayList of entrantIds
     */
    public Task<ArrayList<String>> getEntrantList(String eventId, EntrantListType listType) {
        DocumentReference eventDoc = db.collection("events").document(eventId);
        Log.d("getEntrantList", String.format("Fetching entrantList. EventId: %s | type: %s",
                eventId,
                listType.toString()
            )
        );
        return eventDoc.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                Log.e("getEntrantList", String.format("Failed to fetch entrantList for event: EventId: %s | type: %s | Error: %s",
                        eventId,
                        listType.toString(),
                        task.getException()
                ));
                throw task.getException();
            }

            DocumentSnapshot doc = task.getResult();
            if (!doc.exists()) {
                Log.e("getEntrantList", "Event document does not exist: EventId: " + eventId);
                throw new Exception("Event document missing");
            }

            List<String> rawList = (List<String>) doc.get(getDbEntrantListFieldName(listType));
            if (rawList == null) {
                Log.e("getEntrantList", String.format("No Entrants found for event. EventId: %s | type: %s",
                        eventId,
                        listType.toString()
                ));
                return new ArrayList<>();
            }

            return new ArrayList<>(rawList);
        });
    }

    /**
     * Generates the InvitationList for an event, or adds new entrants if it already exists
     *
     * <p>This function populates the Invited list for an event based on how many free spaces exist.
     * It first gets the users in the waitlist as well as the size of the invited list before
     * creating a random sample of the waitlist and moving all users from the sampled list into the
     * invitedList.</p>
     * @param eventId the id of the event to generate the invited list for
     * @param entrantsLimit the number of amount of entrants that are allowed to be in the invited list. Must be greater than 0
     * @return a task
     */
    public Task<Void> generateInvitationList(String eventId, int entrantsLimit) {
        Task<ArrayList<String>> waitlistTask = getEntrantList(eventId, EntrantListType.WAITLIST);
        Task<Integer> invitedListTask = getEntrantListSize(eventId, EntrantListType.INVITED);

        return Tasks.whenAllSuccess(waitlistTask, invitedListTask)
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    return Tasks.forException(task.getException());
                }

                List<Object> results = task.getResult();
                ArrayList<String> waitlist = (ArrayList<String>) results.get(0);
                int invitedListSize = (Integer) results.get(1);

                // array of randomly sampled entrants on waitlist
                ArrayList<String> sampledEntrants = sampleEntrantList(waitlist, entrantsLimit, invitedListSize);

                List<Task<Void>> moveTasks = new ArrayList<>();
                for (String entrantId : sampledEntrants) {
                    moveTasks.add(
                        moveEntrantAcrossLists(
                                eventId,
                                entrantId,
                                EntrantListType.INVITED,
                                EntrantListType.WAITLIST
                        )
                    );
                }

                return Tasks.whenAll(moveTasks);
            });
    }

    /**
     * Randomly samples an arrayList of Strings
     *
     * <p>This function is used to randomly sample an ArrayList that represents the waitlist of an
     * event to return a partial invited list. It subtracts invitedListSize from entrantsLimit to
     * determine the size of the ArrayList of random entrants to return</p>
     *
     * @param waitlist an ArrayList of userIds on a waitlist
     * @param entrantsLimit The amount of entrants allowed to enroll in an event
     * @param invitedListSize the size of the associated invitedList, used to calculate return size
     * @return an ArrayList of randomly sampled users given via the waitlist of size entrantsLimit - invitedListSize
     */
    public ArrayList<String> sampleEntrantList(ArrayList<String> waitlist, int entrantsLimit, int invitedListSize) {
        // subtract to allow for sampling of only number of spots left on inviteList
        int n = entrantsLimit - invitedListSize;
        if (n <= 0) {
            Log.d("sampleEntrantsList", "invitedList is full. n: " + n);
            return new ArrayList<>();
        }
        Log.d("sampleEntrantList", String.format("Sampling %d entrants.", n));

        ArrayList<String> copy = new ArrayList<>(waitlist);
        Collections.shuffle(copy);

        // sampledList is a sublist of shuffled waitlist
        List<String> sampledList = copy.subList(0, Math.min(n, copy.size()));
        return new ArrayList<>(sampledList);
    }
}
