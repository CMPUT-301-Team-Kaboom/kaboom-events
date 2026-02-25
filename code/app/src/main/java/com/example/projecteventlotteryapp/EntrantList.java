package com.example.projecteventlotteryapp;

import java.util.ArrayList;

/**
 * Represents a list of entrants
 * ex Waitlist, Invited, Enrolled, Declined
 */
public class EntrantList {
    private ArrayList<Entrant> entrants;
    private int listLength;

    public EntrantList() {
        entrants = new ArrayList<Entrant>();
        listLength = 0;
    }

    public ArrayList<Entrant> getEntrants() {
        return entrants;
    }

    public void addEntrant(Entrant entrant) {
        entrants.add(entrant);
        listLength++;
    }

//    public popEntrant(Entrant entrant)

    public int getListLength() {
        return listLength;
    }
}
