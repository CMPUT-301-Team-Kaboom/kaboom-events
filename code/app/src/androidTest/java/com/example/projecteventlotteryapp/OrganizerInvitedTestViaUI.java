package com.example.projecteventlotteryapp;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.projecteventlotteryapp.Activities.OrganizerInvitedActivity;
import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.example.projecteventlotteryapp.dbUtils.FirestoreUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerInvitedTestViaUI {
    private FirebaseFirestore db;
    private Event event;
    private String eventID;
    private String eventName;
    private String organizerID;
    private ArrayList<String> entrantIDsToDelete = new ArrayList<>();

    @Before
    public void setup() throws InterruptedException {
        db = FirebaseFirestore.getInstance();

        eventID = "invitedList_test_event";
        organizerID = "invitedList_event_organizer";
        eventName = "event123";

        event = new Event(eventID, eventName,
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                LocalDateTime.now().plusDays(1),
                100,
                false,
                false);

        Map<String, Object> organizer = new HashMap<>();

        organizer.put("deviceID", organizerID);
        organizer.put("email", "delete_this@email");
        organizer.put("name", "Delete me organizer");
        organizer.put("phone", "");

        CountDownLatch setupLatch = new CountDownLatch(7);

        for (int i = 0; i < 3; i++){
            Map<String, Object> entrant = new HashMap<>();
            String entrantID = "entrant" + i;
            entrantIDsToDelete.add(entrantID);

            entrant.put("deviceID", entrantID);
            entrant.put("name", entrantID);
            entrant.put("email", "");
            db.collection("entrants").document(entrantID).set(entrant)
                    .addOnSuccessListener(aVoid -> setupLatch.countDown());
        }

        db.collection("organizers").document(organizerID).set(organizer)
                .addOnSuccessListener(aVoid -> createEvent(entrantIDsToDelete).addOnSuccessListener(docRef -> setupLatch.countDown()));

//        Map<String, Object> invitedList = new HashMap<>();
//        invitedList.put("invited", entrantIDsToDelete);
//
//        db.collection("events").document(eventID).update(invitedList)
//                .addOnSuccessListener(aVoid -> setupLatch.countDown());

        setupLatch.await(10, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws InterruptedException {
        // clean up event
        CountDownLatch eventLatch = new CountDownLatch(1);
        db.collection("events").document(eventID).delete()
                .addOnCompleteListener(task -> eventLatch.countDown());
        eventLatch.await(5, TimeUnit.SECONDS);

        // clean up organizer
        CountDownLatch orgLatch = new CountDownLatch(1);
        db.collection("organizers").document(organizerID).delete()
                .addOnCompleteListener(task -> orgLatch.countDown());
        orgLatch.await(5, TimeUnit.SECONDS);

        // Clean up comments
        for (String comment : entrantIDsToDelete) {
            if (comment == null) continue;
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("entrants").document(comment).delete()
                    .addOnCompleteListener(task -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void uninvitedSingleUserTest() throws InterruptedException{
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), OrganizerInvitedActivity.class);
        intent.putExtra("eventID", eventID);
        intent.putExtra("eventName", eventName);

        try (ActivityScenario<OrganizerInvitedActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btn_organizer_invited_select)).perform(click());

            onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.lv_organizer_invited_list)).atPosition(0)
                    .onChildView(withId(R.id.cb_entrant_select)).perform(click());

            onView(withId(R.id.btn_organizer_invited_uninvite)).perform(click());

            onView(withText("Uninvite"))
                    .inRoot(isDialog()).perform(click());

            Thread.sleep(3000);

            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] userExists = {true};

            db.collection("events").document(eventID).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()){
                            ArrayList<String> invitedUsers = (ArrayList<String>) doc.get("invited");
                            userExists[0] = invitedUsers.contains("entrant0");
                        }
                        latch.countDown();
                    });

            latch.await(5, TimeUnit.SECONDS);

            assertFalse(userExists[0]);

            onView(withText("entrant0")).check(doesNotExist());
        }
    }

    @Test
    public void uninvitedAllUsersTest() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), OrganizerInvitedActivity.class);
        intent.putExtra("eventID", eventID);
        intent.putExtra("eventName", eventName);

        try (ActivityScenario<OrganizerInvitedActivity> scenario = ActivityScenario.launch(intent)){
            onView(withId(R.id.btn_organizer_invited_select)).perform(click());

            onView(withId(R.id.btn_organizer_invited_select)).perform(click());

            onView(withId(R.id.btn_organizer_invited_uninvite)).perform(click());

            onView(withText("Uninvite"))
                    .inRoot(isDialog()).perform(click());

            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] userExists = {false};

            db.collection("events").document(eventID).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()){
                            ArrayList<String> invitedUsers = (ArrayList<String>) doc.get("invited");
                            userExists[0] = invitedUsers.isEmpty();
                        }
                        latch.countDown();
                    });

            latch.await(5, TimeUnit.SECONDS);

            assertTrue(userExists[0]);

            onView(withText("entrant1")).check(doesNotExist());
            onView(withText("entrant2")).check(doesNotExist());
            onView(withText("entrant3")).check(doesNotExist());
        }
    }

    public Task<Void> createEvent(ArrayList<String> invitedList){
        HashMap<String, Object> eventData = new HashMap<>();

        // Helper variable initialization
        DocumentReference organizerRef =
                db.collection("organizers").document(organizerID);
        DocumentReference defaultPoster =
                db.collection("posters").document("default_poster");
        ZoneId zoneId = ZoneId.systemDefault();

        // Mandatory values
        eventData.put("organizer", organizerRef);
        eventData.put("name", event.getName());
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
        eventData.put("isPrivate", event.isPrivate());

        // setting null values these
        eventData.put("poster", defaultPoster);
        eventData.put("waitlistSize", 0);
        eventData.put("description", null);
        eventData.put("geoLocationEnabled", false);
        eventData.put("location", null);
        eventData.put("qrCodePath", null);
        eventData.put("tags", null);
        eventData.put("waitlistLimit", -1);  // -1 indicates no limit
        eventData.put("waitlist", new ArrayList<>());
        eventData.put("enrolled", new ArrayList<>());
        eventData.put("invited",  invitedList);
        eventData.put("declined", new ArrayList<>());
        eventData.put("comments", new ArrayList<>());

        return db.collection("events")
                .document(eventID)
                .set(eventData);
    }
}
