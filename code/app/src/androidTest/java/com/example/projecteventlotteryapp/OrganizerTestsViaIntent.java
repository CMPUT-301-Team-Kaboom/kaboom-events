// reference: https://developer.android.com/training/testing/espresso/intents
package com.example.projecteventlotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.projecteventlotteryapp.Activities.EditEventActivity;
import com.example.projecteventlotteryapp.Activities.EventDetailsActivity;
import com.example.projecteventlotteryapp.Activities.OrganizerDeclinedActivity;
import com.example.projecteventlotteryapp.Activities.OrganizerEnrolledActivity;
import com.example.projecteventlotteryapp.Activities.OrganizerInvitedActivity;
import com.example.projecteventlotteryapp.Activities.OrganizerWaitlistActivity;
import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.example.projecteventlotteryapp.dbUtils.FirestoreUtils;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerTestsViaIntent {

    private final String testEventId = "test_event_intent";
    private FirebaseFirestore db;
    private EventUtils eventUtils;

    @Before
    public void setup() {
        Intents.init();
        db = FirebaseFirestore.getInstance();
        eventUtils = new EventUtils(db);

        // sign up as organizer
        MyApp app = (MyApp) ApplicationProvider.getApplicationContext();
        User testUser = new User(Role.ORGANIZER, "test_org_ui", "Test Organizer", "org@test.com", "0987654321");
        app.setCurrentUser(testUser);

        // create test event in Firestore
        createTestEvent();
    }

    @After
    public void tearDown() {
        Intents.release();
        // clean up: delete test event
        db.collection("events").document(testEventId).delete();
    }

    private void createTestEvent() {
        Event event = new Event(
                testEventId,
                "Test Event",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                LocalDateTime.now().plusDays(7),
                100,
                false,
                false
        );
        event.setDescription("Test Description");
        event.setWaitlistLimit(20);
        event.setWaitlistSize(0);
        event.setGeolocationEnabled(false);
        event.setTagsList(new ArrayList<>());

        Map<String, Object> eventData = new HashMap<>();
        DocumentReference organizerRef = db.collection("organizers").document("test_org_ui");
        DocumentReference defaultPoster = db.collection("posters").document("default_poster");

        eventData.put("organizer", organizerRef);
        eventData.put("name", event.getName());
        eventData.put("poster", defaultPoster);

        ZoneId zoneId = ZoneId.systemDefault();
        eventData.put("drawDate", FirestoreUtils.localDateTimeToTimestamp(event.getDrawDate(), zoneId));
        eventData.put("registrationEndDate", FirestoreUtils.localDateToTimestamp(event.getRegistrationEndDate(), zoneId));
        eventData.put("registrationStartDate", FirestoreUtils.localDateToTimestamp(event.getRegistrationStartDate(), zoneId));
        eventData.put("entrantsLimit", event.getAttendeesLimit());

        eventData.put("waitlistSize", event.getWaitlistSize());
        eventData.put("description", event.getDescription());
        eventData.put("geoLocationEnabled", event.isGeolocationEnabled());
        eventData.put("isPrivate", event.isPrivate());
        eventData.put("location", null);
        eventData.put("qrCodePath", null);
        eventData.put("tags", event.getTagsList());
        eventData.put("waitlistLimit", event.getWaitlistLimit());
        eventData.put("waitlist", new ArrayList<String>());
        eventData.put("enrolled", new ArrayList<String>());
        eventData.put("invited", new ArrayList<String>());
        eventData.put("declined", new ArrayList<String>());

        try {
            Tasks.await(db.collection("events").document(testEventId).set(eventData));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateNewEventDBItem() throws ExecutionException, InterruptedException {
        Event event = new Event(
                testEventId,
                "Test Event",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                LocalDateTime.now().plusDays(7),
                10,
                false,
                false
        );

        // Act
        MyApp app = (MyApp) ApplicationProvider.getApplicationContext();
        try {
            // Act: create the event
            eventUtils.createNewEventDbItem(event, app.getCurrentUser().getUserId());

            // Assert: verify it exists in Firestore
            DocumentSnapshot snapshot = Tasks.await(db.collection("events").document(testEventId).get());
            assertTrue(snapshot.exists());
            assertEquals("Test Event", snapshot.getString("name"));

        } finally {
            // Tear down: delete the event so it doesn't persist after the test
            try {
                Tasks.await(db.collection("events").document(testEventId).delete());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testNavigateToWaitlist() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class)
                .putExtra("eventId", testEventId);
        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            Thread.sleep(3000);

            onView(withId(R.id.btn_eventDetails_organizer_waitlist)).perform(click());
            intended(allOf(
                    hasComponent(OrganizerWaitlistActivity.class.getName()),
                    hasExtra("eventID", testEventId)
            ));
        }
    }

    @Test
    public void testNavigateToInvited() throws InterruptedException{
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class)
                .putExtra("eventId", testEventId);
        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            Thread.sleep(3000);

            onView(withId(R.id.btn_eventDetails_organizer_invited)).perform(click());
            intended(allOf(
                    hasComponent(OrganizerInvitedActivity.class.getName()),
                    hasExtra("eventID", testEventId)
            ));
        }
    }

    @Test
    public void testNavigateToEnrolled() throws InterruptedException{
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class)
                .putExtra("eventId", testEventId);
        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            Thread.sleep(3000);

            onView(withId(R.id.btn_eventDetails_organizer_enrolled)).perform(click());
            intended(allOf(
                    hasComponent(OrganizerEnrolledActivity.class.getName()),
                    hasExtra("eventID", testEventId)
            ));
        }
    }

    @Test
    public void testNavigateToDeclined() throws  InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class)
                .putExtra("eventId", testEventId);
        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            Thread.sleep(3000);

            onView(withId(R.id.btn_eventDetails_organizer_declined)).perform(click());
            intended(allOf(
                    hasComponent(OrganizerDeclinedActivity.class.getName()),
                    hasExtra("eventID", testEventId)
            ));
        }
    }

    @Test
    public void testNavigateToEditEvent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class)
                .putExtra("eventId", testEventId);
        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btn_eventDetails_edit)).perform(click());
            intended(allOf(
                    hasComponent(EditEventActivity.class.getName()),
                    hasExtra("eventId", testEventId)
            ));
        }
    }

    @Test
    public void testNavigateToCreateEvent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventsListActivity.class);
        try (ActivityScenario<EventsListActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btn_create_event)).perform(click());
            onView(withText("Confirm")).check(matches(isDisplayed()));
        }
    }
}
