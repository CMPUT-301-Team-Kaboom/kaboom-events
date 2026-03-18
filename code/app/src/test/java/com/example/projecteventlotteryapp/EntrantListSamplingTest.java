package com.example.projecteventlotteryapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.projecteventlotteryapp.dbUtils.EventUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class EntrantListSamplingTest {
    EventUtils eventUtils;

    @Before
    public void setUp() {
        // no need to setup db connection
        eventUtils = new EventUtils(null);
    }

    @Test
    public void normalCase() {
        ArrayList<String> waitlist = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));

        ArrayList<String> result = eventUtils.sampleEntrantList(waitlist, 3, 1);

        assertEquals(2, result.size()); // 3 - 1 = 2
    }

    /**
     * When entrantsLimit exceeds waitlist size → returns entire waitlist
     */
    @Test
    public void sampleEntrantList_limitExceedsWaitlist_returnsAll() {
        ArrayList<String> waitlist = new ArrayList<>(Arrays.asList("a", "b"));

        ArrayList<String> result = eventUtils.sampleEntrantList(waitlist, 10, 0);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(waitlist));
    }

    /**
     * No remaining spots → returns empty list
     */
    @Test
    public void sampleEntrantList_noRemainingSpots_returnsEmpty() {
        ArrayList<String> waitlist = new ArrayList<>(Arrays.asList("a", "b"));

        ArrayList<String> result = eventUtils.sampleEntrantList(waitlist, 2, 2);

        assertTrue(result.isEmpty());
    }

    /**
     * Empty waitlist → always returns empty
     */
    @Test
    public void sampleEntrantList_emptyWaitlist_returnsEmpty() {
        ArrayList<String> waitlist = new ArrayList<>();

        ArrayList<String> result = eventUtils.sampleEntrantList(waitlist, 5, 0);

        assertTrue(result.isEmpty());
    }

    /**
     * Returned elements must come from original waitlist
     */
    @Test
    public void sampleEntrantList_elementsAreFromWaitlist() {
        ArrayList<String> waitlist = new ArrayList<>(Arrays.asList("a", "b", "c"));

        ArrayList<String> result = eventUtils.sampleEntrantList(waitlist, 2, 0);

        for (String s : result) {
            assertTrue(waitlist.contains(s));
        }
    }

    /**
     * Edge case: entrantsLimit < invitedListSize → should return empty (or crash if bug exists)
     */
    @Test
    public void sampleEntrantList_negativeSamplingSize_returnsEmpty() {
        ArrayList<String> waitlist = new ArrayList<>(Arrays.asList("a", "b", "c"));

        ArrayList<String> result = eventUtils.sampleEntrantList(waitlist, 1, 3);

        // Expected behavior (what it SHOULD be)
        assertTrue(result.isEmpty());
    }
}
