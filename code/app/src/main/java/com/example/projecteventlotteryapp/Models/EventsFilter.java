package com.example.projecteventlotteryapp.Models;

import android.util.Log;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the filtering criteria used to limit the events list.
 *
 * <p>This class represents all possible filters a user can apply when searching for events including:
 *  name, enrollment status, tags, registration period, draw date. </p>
 *
 * <p>All fields are optional. If a field is null or empty, that filter is not applied.</p>
 */
public class EventsFilter {
    public String name;
    public String status;
    public ArrayList<String> tags;
    public LocalDate regStart;
    public LocalDate regEnd;
    public LocalDate drawDate;

    public EventsFilter() {
        tags = new ArrayList<>();
    }

    /**
     * Checks whether no filters have been applied.
     *
     * @return true if all filter fields are null or empty, false otherwise
     */
    public boolean isEmpty() {
        Log.d("EventsFilter", "Name: " + name);
        Log.d("EventsFilter", "Status: " + status);
        Log.d("EventsFilter", "Tags: " + tags);
        Log.d("EventsFilter", "RegStart: " + regStart);
        Log.d("EventsFilter", "RegEnd: " + regEnd);
        Log.d("EventsFilter", "DrawDate: " + drawDate);
        return name == null && status == null && tags.isEmpty() && regStart == null && regEnd == null && drawDate == null;
    }

    /**
     * Check if an event satisfies the filter.
     *
     * <p>All non-null filters are applied.</p>
     *
     * @param event to check
     * @param snapshot to check status
     * @param currentUserId to check status
     * @return true if all filters match the event, false otherwise
     */
    public boolean isMatch(Event event, QueryDocumentSnapshot snapshot, String currentUserId) {
        Log.d("EventsFilter", "Is Match Event: " + event.getName());
        if (name != null) {
            String eventName = event.getName().trim().toLowerCase();
            String filterName = name.trim().toLowerCase();

            // check for exact match
            if (!eventName.equalsIgnoreCase(filterName) && !eventName.contains(filterName)) {
                return false;
            }
        }

        // check statuses
        if (status != null) {
            List<String> ids = (List<String>) snapshot.get(status);
            if (ids == null || !ids.contains(currentUserId)) {
                return false;
            }
        }

        // check tags
        if (!tags.isEmpty()) {
            if (event.getTagsList() == null || event.getTagsList().isEmpty()) {
                return false;
            }
            for (String tag: tags) {
                if (!event.getTagsList().contains(tag)) {
                    return false;
                }
            }
        }

        // check registration period
        if (regStart != null && regEnd != null) {
            if (event.getRegistrationStartDate() == null || event.getRegistrationStartDate().isAfter(regStart) || event.getRegistrationEndDate() == null || event.getRegistrationEndDate().isBefore(regEnd)) {
                return false;
            }
        } else if (regStart != null) {
            if (event.getRegistrationStartDate() == null || event.getRegistrationStartDate().isBefore(regStart)) {
                return false;
            }
        } else if (regEnd != null) {
            if (event.getRegistrationEndDate() == null || event.getRegistrationEndDate().isAfter(regEnd)) {
                return false;
            }
        }

        // check draw date
        if (drawDate != null) {
            if (event.getDrawDate() == null || !event.getDrawDate().toLocalDate().isEqual(drawDate)) {
                return false;
            }
        }

        return true;
    }

    public boolean isAvailable(Event event, QueryDocumentSnapshot snapshot, LocalDate today) {
        // check registration period still available
        if (today.isBefore(event.getRegistrationStartDate()) || today.isAfter(event.getRegistrationEndDate())) {
            return false;
        }

        // check waitlist limit not surpassed
        int waitlistSize = (int) snapshot.get("waitlistSize");
        int waitlistLimit = (int) snapshot.get("waitlistLimit");
        if (waitlistSize >= waitlistLimit) {
            return false;
        }

        return true;
    }

}
