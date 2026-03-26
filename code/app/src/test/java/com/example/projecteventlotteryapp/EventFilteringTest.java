package com.example.projecteventlotteryapp;

import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.Models.EventsFilter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the testing all filtering criteria (name, status, tags, dates).
 */
public class EventFilteringTest {
    @Mock
    FirebaseFirestore mockDb;
    @Mock
    CollectionReference mockEventCollectionRef;
    @Mock
    DocumentReference mockEventDocument;
    @Mock
    private QueryDocumentSnapshot mockEventDocumentSnapshot;

    private EventsFilter filter;

    /**
     * Initialize the mock objects and sets up the filter instance.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockDb.collection("events")).thenReturn(mockEventCollectionRef);

        filter = new EventsFilter();
    }

    /**
     * Test for filtering events by name match.
     */
    @Test
    public void testExactMatchFilterByName() {
        // create mock events
        Event event1 = new Event("Event1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1), LocalDateTime.of(2026, 5, 1, 0, 0), 100);
        Event event2 = new Event("Event2", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1), LocalDateTime.of(2026, 6, 1, 0, 0), 100);

        // set up mock snapshot for event
        when(mockEventDocumentSnapshot.get("name")).thenReturn(event1.getName()).thenReturn(event2.getName());
        when(mockEventDocumentSnapshot.get("enrolled")).thenReturn(List.of("user111", "user222")).thenReturn(List.of("user111"));
        when(mockEventDocumentSnapshot.get("declined")).thenReturn(List.of("user333")).thenReturn(List.of("user222"));
        when(mockEventDocumentSnapshot.get("invited")).thenReturn(List.of("user444", "user555")).thenReturn(List.of("user333"));
        when(mockEventDocumentSnapshot.get("waitlist")).thenReturn(List.of("user666")).thenReturn(List.of("user444"));
        when(mockEventDocumentSnapshot.get("tags")).thenReturn(event1.getTagsList()).thenReturn(event2.getTagsList());
        when(mockEventDocumentSnapshot.get("registrationStartDate")).thenReturn(event1.getRegistrationStartDate()).thenReturn(event2.getRegistrationStartDate());
        when(mockEventDocumentSnapshot.get("registrationEndDate")).thenReturn(event1.getRegistrationEndDate()).thenReturn(event2.getRegistrationEndDate());
        when(mockEventDocumentSnapshot.get("drawDate")).thenReturn(event1.getDrawDate()).thenReturn(event2.getDrawDate());

        // test exact name match
        filter.name = "Event1";
        boolean isMatch1a = filter.isMatch(event1, mockEventDocumentSnapshot, "user111");
        boolean isMatch2a = filter.isMatch(event2, mockEventDocumentSnapshot, "user111");

        assertTrue(isMatch1a); // event 1 should match
        assertFalse(isMatch2a); // event 2 should not match

        // test substring match
        filter.name = "event";
        boolean isMatch1b = filter.isMatch(event1, mockEventDocumentSnapshot, "user111");
        boolean isMatch2b = filter.isMatch(event2, mockEventDocumentSnapshot, "user111");

        assertTrue(isMatch1b); // event 1 should match
        assertTrue(isMatch2b); // event 2 should match
    }

    /**
     * Test for filtering events by status (e.g. enrolled).
     */
    @Test
    public void testFilterByStatus() {
        // create mock events
        Event event1 = new Event("Event1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1), LocalDateTime.of(2026, 5, 1, 0, 0), 100);
        Event event2 = new Event("Event2", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1), LocalDateTime.of(2026, 6, 1, 0, 0), 100);

        // set up mock snapshot for event
        when(mockEventDocumentSnapshot.get("name")).thenReturn(event1.getName()).thenReturn(event2.getName());
        when(mockEventDocumentSnapshot.get("enrolled")).thenReturn(List.of("user111", "user222")).thenReturn(List.of("user111"));
        when(mockEventDocumentSnapshot.get("declined")).thenReturn(List.of("user333")).thenReturn(List.of("user222"));
        when(mockEventDocumentSnapshot.get("invited")).thenReturn(List.of("user444", "user555")).thenReturn(List.of("user333"));
        when(mockEventDocumentSnapshot.get("waitlist")).thenReturn(List.of("user666")).thenReturn(List.of("user444", "user555", "user666"));
        when(mockEventDocumentSnapshot.get("tags")).thenReturn(event1.getTagsList()).thenReturn(event2.getTagsList());
        when(mockEventDocumentSnapshot.get("registrationStartDate")).thenReturn(event1.getRegistrationStartDate()).thenReturn(event2.getRegistrationStartDate());
        when(mockEventDocumentSnapshot.get("registrationEndDate")).thenReturn(event1.getRegistrationEndDate()).thenReturn(event2.getRegistrationEndDate());
        when(mockEventDocumentSnapshot.get("drawDate")).thenReturn(event1.getDrawDate()).thenReturn(event2.getDrawDate());

        // test for a status
        filter.status = "waitlist";
        boolean isMatch1 = filter.isMatch(event1, mockEventDocumentSnapshot, "user444");
        boolean isMatch2 = filter.isMatch(event2, mockEventDocumentSnapshot, "user444");

        assertFalse(isMatch1);  // event 1 should not match
        assertTrue(isMatch2); // event 2 should match
    }

    /**
     * Test for filtering events by tags.
     */
    @Test
    public void testFilterByTags() {
        // create mock events
        Event event1 = new Event("Event1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1), LocalDateTime.of(2026, 5, 1, 0, 0), 100);
        Event event2 = new Event("Event2", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1), LocalDateTime.of(2026, 6, 1, 0, 0), 100);
        event1.setTagsList(new ArrayList<>(List.of("TAG1")));
        event2.setTagsList(new ArrayList<>(List.of("TAG1", "TAG2")));

        // set up mock snapshot for event
        when(mockEventDocumentSnapshot.get("name")).thenReturn(event1.getName()).thenReturn(event2.getName());
        when(mockEventDocumentSnapshot.get("enrolled")).thenReturn(List.of("user111", "user222")).thenReturn(List.of("user111"));
        when(mockEventDocumentSnapshot.get("declined")).thenReturn(List.of("user333")).thenReturn(List.of("user222"));
        when(mockEventDocumentSnapshot.get("invited")).thenReturn(List.of("user444", "user555")).thenReturn(List.of("user333"));
        when(mockEventDocumentSnapshot.get("waitlist")).thenReturn(List.of("user666")).thenReturn(List.of("user444"));
        when(mockEventDocumentSnapshot.get("tags")).thenReturn(event1.getTagsList()).thenReturn(event2.getTagsList());
        when(mockEventDocumentSnapshot.get("registrationStartDate")).thenReturn(event1.getRegistrationStartDate()).thenReturn(event2.getRegistrationStartDate());
        when(mockEventDocumentSnapshot.get("registrationEndDate")).thenReturn(event1.getRegistrationEndDate()).thenReturn(event2.getRegistrationEndDate());
        when(mockEventDocumentSnapshot.get("drawDate")).thenReturn(event1.getDrawDate()).thenReturn(event2.getDrawDate());

        // test for tags
        filter.tags.add("TAG1");
        filter.tags.add("TAG2");
        boolean isMatch1 = filter.isMatch(event1, mockEventDocumentSnapshot, "user111");
        boolean isMatch2 = filter.isMatch(event2, mockEventDocumentSnapshot, "user111");

        assertFalse(isMatch1); // event 1 should not match
        assertTrue(isMatch2); // event 2 should match
    }

    /**
     * Test for filtering events by registration start date.
     */
    @Test
    public void testFilterByRegStartDate() {
        // create mock events
        Event event1 = new Event("Event1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1), LocalDateTime.of(2026, 5, 1, 0, 0), 100);
        Event event2 = new Event("Event2", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1), LocalDateTime.of(2026, 6, 1, 0, 0), 100);

        // set up mock snapshot for event
        when(mockEventDocumentSnapshot.get("name")).thenReturn(event1.getName()).thenReturn(event2.getName());
        when(mockEventDocumentSnapshot.get("enrolled")).thenReturn(List.of("user111", "user222")).thenReturn(List.of("user111"));
        when(mockEventDocumentSnapshot.get("declined")).thenReturn(List.of("user333")).thenReturn(List.of("user222"));
        when(mockEventDocumentSnapshot.get("invited")).thenReturn(List.of("user444", "user555")).thenReturn(List.of("user333"));
        when(mockEventDocumentSnapshot.get("waitlist")).thenReturn(List.of("user666")).thenReturn(List.of("user444"));
        when(mockEventDocumentSnapshot.get("tags")).thenReturn(event1.getTagsList()).thenReturn(event2.getTagsList());
        when(mockEventDocumentSnapshot.get("registrationStartDate")).thenReturn(event1.getRegistrationStartDate()).thenReturn(event2.getRegistrationStartDate());
        when(mockEventDocumentSnapshot.get("registrationEndDate")).thenReturn(event1.getRegistrationEndDate()).thenReturn(event2.getRegistrationEndDate());
        when(mockEventDocumentSnapshot.get("drawDate")).thenReturn(event1.getDrawDate()).thenReturn(event2.getDrawDate());

        // test a registration start date onward from given date
        filter.regStart = LocalDate.of(2026, 3, 15);
        boolean isMatch1 = filter.isMatch(event1, mockEventDocumentSnapshot, "user111");
        boolean isMatch2 = filter.isMatch(event2, mockEventDocumentSnapshot, "user111");

        assertFalse(isMatch1); // event 1 should not match
        assertTrue(isMatch2);  // event 2 should match
    }

    /**
     * Test for filtering events by registration end date.
     */
    @Test
    public void testFilterByRegEndDate() {
        // create mock events
        Event event1 = new Event("Event1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1), LocalDateTime.of(2026, 5, 1, 0, 0), 100);
        Event event2 = new Event("Event2", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1), LocalDateTime.of(2026, 6, 1, 0, 0), 100);

        // set up mock snapshot for event
        when(mockEventDocumentSnapshot.get("name")).thenReturn(event1.getName()).thenReturn(event2.getName());
        when(mockEventDocumentSnapshot.get("enrolled")).thenReturn(List.of("user111", "user222")).thenReturn(List.of("user111"));
        when(mockEventDocumentSnapshot.get("declined")).thenReturn(List.of("user333")).thenReturn(List.of("user222"));
        when(mockEventDocumentSnapshot.get("invited")).thenReturn(List.of("user444", "user555")).thenReturn(List.of("user333"));
        when(mockEventDocumentSnapshot.get("waitlist")).thenReturn(List.of("user666")).thenReturn(List.of("user444"));
        when(mockEventDocumentSnapshot.get("tags")).thenReturn(event1.getTagsList()).thenReturn(event2.getTagsList());
        when(mockEventDocumentSnapshot.get("registrationStartDate")).thenReturn(event1.getRegistrationStartDate()).thenReturn(event2.getRegistrationStartDate());
        when(mockEventDocumentSnapshot.get("registrationEndDate")).thenReturn(event1.getRegistrationEndDate()).thenReturn(event2.getRegistrationEndDate());
        when(mockEventDocumentSnapshot.get("drawDate")).thenReturn(event1.getDrawDate()).thenReturn(event2.getDrawDate());

        // test a registration end date and previous exact
        filter.regEnd = LocalDate.of(2026, 4, 1);
        boolean isMatch1 = filter.isMatch(event1, mockEventDocumentSnapshot, "user111");
        boolean isMatch2 = filter.isMatch(event2, mockEventDocumentSnapshot, "user111");

        assertTrue(isMatch1); // event 1 should match
        assertFalse(isMatch2); // event 2 should not match
    }

    /**
     * Test for filtering events by draw date.
     */
    @Test
    public void testFilterByDrawDate() {
        // create mock events
        Event event1 = new Event("Event1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1), LocalDateTime.of(2026, 5, 1, 0, 0), 100);
        Event event2 = new Event("Event2", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1), LocalDateTime.of(2026, 6, 1, 0, 0), 100);

        // set up mock snapshot for event
        when(mockEventDocumentSnapshot.get("name")).thenReturn(event1.getName()).thenReturn(event2.getName());
        when(mockEventDocumentSnapshot.get("enrolled")).thenReturn(List.of("user111", "user222")).thenReturn(List.of("user111"));
        when(mockEventDocumentSnapshot.get("declined")).thenReturn(List.of("user333")).thenReturn(List.of("user222"));
        when(mockEventDocumentSnapshot.get("invited")).thenReturn(List.of("user444", "user555")).thenReturn(List.of("user333"));
        when(mockEventDocumentSnapshot.get("waitlist")).thenReturn(List.of("user666")).thenReturn(List.of("user444"));
        when(mockEventDocumentSnapshot.get("tags")).thenReturn(event1.getTagsList()).thenReturn(event2.getTagsList());
        when(mockEventDocumentSnapshot.get("registrationStartDate")).thenReturn(event1.getRegistrationStartDate()).thenReturn(event2.getRegistrationStartDate());
        when(mockEventDocumentSnapshot.get("registrationEndDate")).thenReturn(event1.getRegistrationEndDate()).thenReturn(event2.getRegistrationEndDate());
        when(mockEventDocumentSnapshot.get("drawDate")).thenReturn(event1.getDrawDate()).thenReturn(event2.getDrawDate());

        // test a draw date
        filter.drawDate = LocalDate.of(2026, 5, 1);
        boolean isMatch1 = filter.isMatch(event1, mockEventDocumentSnapshot, "user111");
        boolean isMatch2 = filter.isMatch(event2, mockEventDocumentSnapshot, "user111");

        assertTrue(isMatch1); // event 1 should match
        assertFalse(isMatch2); // event 2 should not match
    }

    /**
     * Test for filtering events when no filters are applied.
     */
    @Test
    public void testNoFiltersApplied() {
        // create mock events
        Event event1 = new Event("Event1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 1), LocalDateTime.of(2026, 5, 1, 0, 0), 100);
        Event event2 = new Event("Event2", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1), LocalDateTime.of(2026, 6, 1, 0, 0), 100);

        // test no filters applied
        filter = new EventsFilter();
        boolean isMatch1 = filter.isMatch(event1, mockEventDocumentSnapshot, "user111");
        boolean isMatch2 = filter.isMatch(event2, mockEventDocumentSnapshot, "user111");

        // both events should match since no filters are applied
        assertTrue(isMatch1);
        assertTrue(isMatch2);
    }
}