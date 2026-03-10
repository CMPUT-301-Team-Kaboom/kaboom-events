package com.example.projecteventlotteryapp;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a list of entrants
 * ex Waitlist, Invited, Enrolled, Declined
 * TODO: decide if this should be changed to store only entrant IDs instead
 */
public class EntrantList implements Serializable {
    private ArrayList<User> entrants;
    private int listLength;

    public EntrantList() {
        entrants = new ArrayList<User>();
        listLength = 0;
    }

    public ArrayList<User> getEntrants() {
        return entrants;
    }

    public void addEntrant(User entrant) {
        if (entrant.getRole() != Role.ENTRANT) {
            throw new IllegalArgumentException("User must have role ENTRANT");
        }

        entrants.add(entrant);
        listLength++;
    }

    public void removeEntrant(User entrant) {
        if (entrant.getRole() != Role.ENTRANT) {
            throw new IllegalArgumentException("User must have role ENTRANT");
        }

        entrants.remove(entrant);
        listLength--;
    }

//    public popEntrant(Entrant entrant)

    public boolean contains(User user) {
        return entrants.contains(user);
    }

    public int getListLength() {
        return listLength;
    }
}
