package com.example.projecteventlotteryapp.dbUtils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.logging.LogFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper class for Firestore actions
 *
 * <p>This class is designed to encapsulate commonly used Firestore actions. All methods should
 * be static and an instance of this class should not be needed in order to use its methods.</p>
 *
 * Example usage: var = FirestoreUtils.LocalDate(date, zoneId);
 */
public class FirestoreUtils {
    /**
     * This function authenticates an anonymous user with the Firestore db
     *
     * <p>This function authenticates with the Firestore db. It will technically return immediately
     * despite the actions being asynchronous. As such, it is incredibly important that this
     * function is NOT called immediately before a database operation. Ideally, this is only called
     * once in the onCreate method of the RegistrationActivity or LogInActivity</p>
     *
     * References:
     *      The following method has been adapted from https://firebase.google.com/docs/auth/android/anonymous-auth
     */
    public static void anonymousAuth() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            return;
        }

        auth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Auth", "signInAnonymously: Success");
                    } else {
                        Log.e("Auth", "signInAnonymously: Failure, " + task.getException());
                    }
                });
    }

    /**
     * Converts a LocalDate to a Firestore Timestamp at the start of the day in the given time zone.
     *
     * @param localDate the LocalDate to convert
     * @param zoneId the time zone to use (e.g., ZoneId.systemDefault(), ZoneId.of("UTC"))
     * @return Firebase Timestamp representing the start of the day
     */
    public static Timestamp localDateToTimestamp(LocalDate localDate, ZoneId zoneId) {
        if (localDate == null || zoneId == null) {
            throw new IllegalArgumentException("localDate and zoneId must not be null");
        }
        Date date = Date.from(localDate.atStartOfDay(zoneId).toInstant());
        return new Timestamp(date);
    }

    /**
     * Converts a LocalDateTime to a Firestore Timestamp in the given time zone.
     *
     * @param localDateTime the LocalDateTime to convert
     * @param zoneId the time zone to use (e.g., ZoneId.systemDefault(), ZoneId.of("UTC"))
     * @return Firebase Timestamp representing the LocalDateTime
     */
    public static Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null || zoneId == null) {
            throw new IllegalArgumentException("localDateTime and zoneId must not be null");
        }
        Date date = Date.from(localDateTime.atZone(zoneId).toInstant());
        return new Timestamp(date);
    }


    /**
     * Fetches a Firestore TimeStamp value from an DocumentSnapshot and converts it to a LocalDate
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @return the value of the fetched field as a LocalDate or null if field does not exist
     *
     * Citations:
     * The following code is adapted from...
     * Author: Ruslan https://stackoverflow.com/users/2032701/ruslan
     * Title: "How to convert java.sql.timestamp to LocalDate (java8) java.time?"
     * Answer: https://stackoverflow.com/a/57101544
     * Date: 2019-07-18
     * Retrieved: 2026-02-28
     * License: CC-BY-SA 4.0
     */
    public static LocalDate fetchLocalDate(DocumentSnapshot snapshot, String field) {
        Timestamp value = snapshot.getTimestamp(field);

        if (value != null) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

    /**
     * Fetches a LocalDateTime value from a DocumentSnapshot
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @return the value of the fetched field as a LocalDateTime or null if field does not exist
     */
    public static LocalDateTime fetchLocalDateTime(DocumentSnapshot snapshot, String field) {
        Timestamp value = snapshot.getTimestamp(field);

        if (value != null) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else {
            return null;
        }
    }
    /**
     * Fetches an int value from a DocumentSnapshot
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @return the value of the fetched field or null if field does not exist
     */
    public static int fetchInt(DocumentSnapshot snapshot, String field, int defaultValue) {
        Long value = snapshot.getLong(field);

        if (value != null) {
            return value.intValue();
        }
        return defaultValue;
    }


    /**
     * Fetches a boolean value from a DocumentSnapshot
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @param defaultValue the value to be returned if the field does not exist or the value could
     *                     be fetched
     * @return the value of the fetched field or defaultValue if field does not exist
     */
    public static boolean fetchBoolean(DocumentSnapshot snapshot, String field, boolean defaultValue) {
        Boolean value = snapshot.getBoolean(field);

        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    /**
     * Fetches a String list value from an event document snapshot and returns an ArrayList representation of it
     * @param snapshot snapshot of the event
     * @param field the field we want the value of
     * @return Arraylist of the string list or an empty ArrayList if the field is null.
     *
     * Citation:
     * The following code is adapted from...
     * Author: Doug Stevenson https://stackoverflow.com/users/807126/doug-stevenson
     * Title: "How to get an array from Firestore?"
     * Answer: https://stackoverflow.com/a/50236950
     * Date: 2018-05-08
     * Retrieved: 2026-02-28
     * License: CC-BY-SA 4.0
     */
    public static ArrayList<String> fetchStringArrayList(DocumentSnapshot snapshot, String field) {
        List<?> value = (List<?>) snapshot.get(field);
        if (value == null) {
            return new ArrayList<String>();
        }

        ArrayList<String> result = new ArrayList<>();

        for (Object item : value) {
            result.add((String) item);
        }
        return result;
    }

    /**
     * Stores a notification in the database
     * @param userId
     * @param recipientID
     * @param message
     * @param eventName
     */
    public static void storeNotificationInFirestore(String userId, String recipientID, String message, String eventName, String eventId, FirebaseFirestore db, android.content.Context context) {
        // Get the sender's device ID
        db.collection("entrants").document(recipientID).get()
                .addOnSuccessListener( documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean notiEnabled = documentSnapshot.getBoolean("notificationEnabled");
                        if (notiEnabled != null && notiEnabled) {
                            Map<String, Object> notif = new HashMap<>();
                            notif.put("sender", userId);
                            notif.put("recipient", recipientID);
                            notif.put("text", message);
                            notif.put("date", com.google.firebase.Timestamp.now());
                            notif.put("eventName", eventName != null ? eventName : "Waitlist Update");
                            notif.put("eventId", eventId);

                            db.collection("notifications").add(notif)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d("Notification", "Notification stored with ID: " + documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("Firestore", "Error adding notification", e);
                                    });
                        }
                        else {
                            android.widget.Toast.makeText(context, recipientID + " does not have notifications enabled.", android.widget.Toast.LENGTH_SHORT).show();
                        }


                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error getting document", e);
                });
    }

    /**
     * Stores a rejection notification in the database
     *
     * @param organizerId
     * @param eventId
     * @param db
     * @param context
     */
    public static void sendRejections(String organizerId, String eventId, FirebaseFirestore db, android.content.Context context) {
        // Flags for final summary Toast, atomicboolean to safeguard against race conditions
        AtomicBoolean anySent = new AtomicBoolean(false);
        AtomicBoolean alreadyNotified = new AtomicBoolean(false);

        //Fetch the specific event document
        db.collection("events").document(eventId).get().addOnSuccessListener(eventDoc -> {
            if (!eventDoc.exists()) return;

            String eventName = eventDoc.getString("name");
            ArrayList<String> waitlist = fetchStringArrayList(eventDoc, "waitlist");
            ArrayList<String> invitedList = fetchStringArrayList(eventDoc, "invited");

            if (waitlist.isEmpty()) {
                android.widget.Toast.makeText(context, "Waitlist is empty.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            String rejectionText = "We're sorry, you were not selected for the event: " + (eventName != null ? eventName : "the event");

            // List to track all asynchronous notification checks
            List<Task<?>> tasks = new ArrayList<>();

            // Iterate through waitlist to find users NOT in invited list
            for (String userId : waitlist) {
                if (!invitedList.contains(userId)) {

                    // check if notification already exists
                    tasks.add(db.collection("notifications")
                            .whereEqualTo("recipient", userId)
                            .whereEqualTo("eventId", eventId)
                            .whereEqualTo("text", rejectionText)
                            .get()
                            .addOnSuccessListener(notifSnap -> {
                                if (notifSnap.isEmpty()) {
                                    // Send if not found
                                    storeNotificationInFirestore(organizerId, userId, rejectionText, eventName, eventId, db, context);
                                    anySent.set(true);
                                } else {
                                    alreadyNotified.set(true);
                                }
                            }));
                }
            }

            // Wait for ALL individual checks to finish before inspecting the flags
            Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
                // anySent checks if we actually sent out anything this time
                // alreadyNotified checks if we already notified the users
                // both being true indicates some people were notified for the first time while others were skipped
                if (anySent.get() && alreadyNotified.get()) {
                    android.widget.Toast.makeText(context, "Rejection notifications sent to some users.", android.widget.Toast.LENGTH_SHORT).show();
                }
                if (anySent.get()) {
                    android.widget.Toast.makeText(context, "Rejection notifications sent.", android.widget.Toast.LENGTH_SHORT).show();
                }
                if (alreadyNotified.get()) {
                    android.widget.Toast.makeText(context, "Rejection notification already sent to users.", android.widget.Toast.LENGTH_SHORT).show();
                }
                if (!anySent.get() && !alreadyNotified.get()) {
                    android.widget.Toast.makeText(context, "No rejections needed.", android.widget.Toast.LENGTH_SHORT).show();
                }
            });

        }).addOnFailureListener(e -> Log.e("FirestoreUtils", "Error fetching event for rejections", e));
    }
}