package com.example.projecteventlotteryapp;

import android.util.Log;

import java.util.ArrayList;

/**
 * Represents a list of entrants
 * ex Waitlist, Invited, Enrolled, Declined
 */
public class EntrantList {
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

//    public popEntrant(Entrant entrant)

    public boolean contains(User user) {
        return entrants.contains(user);
    }

    public int getListLength() {
        return listLength;
    }
}
