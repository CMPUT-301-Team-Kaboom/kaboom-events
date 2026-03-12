package com.example.projecteventlotteryapp;

import com.google.firebase.Timestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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
}
