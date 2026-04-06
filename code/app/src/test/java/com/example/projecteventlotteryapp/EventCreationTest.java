package com.example.projecteventlotteryapp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.example.projecteventlotteryapp.dbUtils.FirestoreUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class EventCreationTest {
    @Mock
    FirebaseFirestore db;
    @Mock CollectionReference eventCollection;
    @Mock Task<DocumentReference> docTask;
    @Mock DocumentReference eventDoc;
    @Mock CollectionReference organizerCollection;
    @Mock DocumentReference organizerRef;
    @Mock CollectionReference posterCollection;
    @Mock DocumentReference posterRef;
    Event event;
    EventUtils eventUtils;
    String eventId;
    String eventName;
    LocalDate regStart;
    LocalDate regEnd;
    LocalDateTime drawDate;
    int entrantLimit;
    boolean eventIsPrivate;

    @Before
    public void setUpEvent(){
        MockitoAnnotations.openMocks(this);

        when(db.collection("events")).thenReturn(eventCollection);
        when(eventCollection.add(any())).thenReturn(docTask);
        when(eventCollection.document(anyString())).thenReturn(eventDoc);
        when(db.collection("organizers")).thenReturn(organizerCollection);
        when(organizerCollection.document(anyString())).thenReturn(organizerRef);
        when(db.collection("posters")).thenReturn(posterCollection);
        when(posterCollection.document(anyString())).thenReturn(posterRef);

        eventUtils = new EventUtils(db);
        eventId = "event123";
        drawDate = LocalDateTime.parse("2027-03-03T12:00:00");
        regEnd = LocalDate.parse("2027-03-02");
        regStart = LocalDate.parse("2027-03-01");
        eventName = "testEvent";
        entrantLimit = 1;

        event = new Event(eventName, regStart, regEnd, drawDate, entrantLimit);
    }

    @Test
    public void setEntrantLimitTest(){
        int newEntrantLimit = 2;
        event.setAttendeesLimit(newEntrantLimit);

        assertEquals(newEntrantLimit, event.getAttendeesLimit());
    }

    @Test
    public void setRegistrationPeriodTest(){
        LocalDate newRegStart = LocalDate.parse("2026-03-01");
        LocalDate newRegEnd = LocalDate.parse("2026-03-02");

        event.setRegistrationStartDate(newRegStart);
        event.setRegistrationEndDate(newRegEnd);

        assertEquals(newRegStart, event.getRegistrationStartDate());
        assertEquals(newRegEnd, event.getRegistrationEndDate());
    }

    @Test
    public void testIsPrivate_DefaultFalse() {
        Event eventTest = new Event(eventName, regStart, regEnd, drawDate, entrantLimit, false);

        assertFalse(eventTest.isPrivate());
    }

    @Test
    public void testSetPrivate_True() {
        Event eventTest = new Event(eventName, regStart, regEnd, drawDate, entrantLimit, false);
        eventTest.setPrivate(true);

        assertTrue(eventTest.isPrivate());
    }

    @Test
    public void testSetPrivate_False() {
        Event eventTest = new Event(eventName, regStart, regEnd, drawDate, entrantLimit, false);

        eventTest.setPrivate(true);
        eventTest.setPrivate(false);

        assertFalse(eventTest.isPrivate());
    }

    @Test
    public void eventDeepCreationTest(){
        when(eventDoc.getId()).thenReturn(eventId);

        when(docTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<DocumentReference> listener = invocation.getArgument(0);
            listener.onSuccess(eventDoc);
            return docTask;
        });
        HashMap<String, Object> eventData = new HashMap<>();

        eventData.put("organizer", organizerRef);
        eventData.put("name", event.getName());
        eventData.put("poster", posterRef);

        ZoneId zoneId = ZoneId.systemDefault();
        // TODO: update to grab system zoneid
        eventData.put(
                "drawDate",
                FirestoreUtils.localDateTimeToTimestamp(
                        event.getDrawDate(),
                        zoneId
                )
        );
        eventData.put(
                "registrationEndDate",
                FirestoreUtils.localDateToTimestamp(
                        event.getRegistrationEndDate(),
                        zoneId
                )
        );
        eventData.put(
                "registrationStartDate",
                FirestoreUtils.localDateToTimestamp(
                        event.getRegistrationStartDate(),
                        zoneId
                )
        );
        eventData.put("entrantsLimit", event.getAttendeesLimit());

        // setting null values
        eventData.put("description", null);
        eventData.put("geoLocationEnabled", false);
        eventData.put("location", null);
        eventData.put("qrCode", null);
        eventData.put("tags", null);
        eventData.put("waitlistLimit", -1);  // -1 indicates no limit
        eventData.put("isPrivate", false);
        eventData.put("waitlistSize", 0);
        eventData.put("coorganizers", new ArrayList<>());
        eventData.put("comments", new ArrayList<>());
        eventData.put("waitlist", new ArrayList<>());
        eventData.put("enrolled", new ArrayList<>());
        eventData.put("invited", new ArrayList<>());
        eventData.put("declined", new ArrayList<>());

        when(docTask.addOnFailureListener(any())).thenReturn(docTask);
        String organizer = "org123";

        eventUtils.createNewEventDbItem(event, organizer);

        verify(eventCollection).add(eventData);
        verify(docTask).addOnSuccessListener(any());
    }
}
