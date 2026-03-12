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


    /**
     * Gets the entrants
     * @return entrants arraylist
     */
    public ArrayList<User> getEntrants() {
        return entrants;
    }

    /**
     * add an entrant to the list
     * @param entrant entrant to add type User
     */
    public void addEntrant(User entrant) {
        if (entrant.getRole() != Role.ENTRANT) {
            throw new IllegalArgumentException("User must have role ENTRANT");
        }

        entrants.add(entrant);
        listLength++;
    }

    /**
     * remove an entrant from the list
     * @param entrant entrant to remove type User
     */
    public void removeEntrant(User entrant) {
        if (entrant.getRole() != Role.ENTRANT) {
            throw new IllegalArgumentException("User must have role ENTRANT");
        }

        entrants.remove(entrant);
        listLength--;
    }

//    public popEntrant(Entrant entrant)

    /**
     * check if a user is in the list
     * @param user User object to check for in the entrants list
     * @return true if user is in the list, false otherwise
     */
    public boolean contains(User user) {
        return entrants.contains(user);
    }

    /**
     * gets the length of the list
     * @return listLength
     */
    public int getListLength() {
        return listLength;
    }
}
