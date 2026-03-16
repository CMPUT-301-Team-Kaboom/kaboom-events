package com.example.projecteventlotteryapp.dbUtils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
}
