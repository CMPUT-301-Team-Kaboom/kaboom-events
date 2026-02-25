package com.example.projecteventlotteryapp;

import java.util.ArrayList;

/**
 * Represents a list of entrants
 * ex Waitlist, Invited, Enrolled, Declined
 */
public class EntrantList {
    private ArrayList<Entrant>  entrants;
    private int listLength;

    public ArrayList<Entrant> getEntrants() {
        return entrants;
    }

    public void setEntrants(ArrayList<Entrant> entrants) {
        this.entrants = entrants;
    }
}
