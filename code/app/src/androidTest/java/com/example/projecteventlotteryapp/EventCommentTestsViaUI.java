package com.example.projecteventlotteryapp;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.projecteventlotteryapp.Activities.EventCommentsActivity;
import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.example.projecteventlotteryapp.dbUtils.FirestoreUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import junit.framework.TestCase;

import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
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
public class EventCommentTestsViaUI {
    private FirebaseFirestore db;
    private EventUtils eventUtils;
    private final List<String> commentIdsToDelete = new ArrayList<>();
    private String eventID;
    private Event event;
    private String organizerID;

    @Before
    public void setup() throws InterruptedException {
        db = FirebaseFirestore.getInstance();
        eventUtils = new EventUtils(db);

        Context context = ApplicationProvider.getApplicationContext();
        MyApp app = (MyApp) context.getApplicationContext();
        User testUser = new User(Role.ORGANIZER, "test_device_id", "Test User", "test@example.com", "1234567890");
        app.setCurrentUser(testUser);

        eventID = "comment_test_event";
        organizerID = "comment_event_organizer";

        event = new Event(eventID, "event123",
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

        CountDownLatch setupLatch = new CountDownLatch(1);

        db.collection("organizers").document(organizerID).set(organizer)
                .addOnSuccessListener(aVoid -> createEvent().addOnSuccessListener(docRef -> setupLatch.countDown()));

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
        for (String comment : commentIdsToDelete) {
            if (comment == null) continue;
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("comments").document(comment).delete()
                    .addOnCompleteListener(task -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    public Task<Void> createEvent(){
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
        eventData.put("invited",  new ArrayList<>());
        eventData.put("declined", new ArrayList<>());
        eventData.put("comments", new ArrayList<>());

        return db.collection("events")
                .document(eventID)
                .set(eventData);
    }

    @Test
    public void testAddComment() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCommentsActivity.class);
        intent.putExtra("eventId", eventID);

        try (ActivityScenario<EventCommentsActivity> scenario = ActivityScenario.launch(intent)) {

            onView(withId(R.id.et_event_comments_textbox))
                    .perform(typeText("test comment"), closeSoftKeyboard());

            onView(withId(R.id.btn_event_comments_post_comment))
                    .perform(click());

            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] commentExists = {false};
            final String[] newCommentID = {null};

            db.collection("comments")
                    .whereEqualTo("text", "test comment")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            commentExists[0] = true;
                            newCommentID[0] = querySnapshot.getDocuments().get(0).getId();
                            commentIdsToDelete.add(newCommentID[0]);
                        }
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> latch.countDown());

            latch.await(5, TimeUnit.SECONDS);

            Thread.sleep(3000);
            TestCase.assertTrue(commentExists[0]);
            onView(withText("test comment")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testAddCommentAndVerify() throws InterruptedException {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCommentsActivity.class);
        intent.putExtra("eventId", eventID);

        try (ActivityScenario<EventCommentsActivity> scenario = ActivityScenario.launch(intent)) {

            onView(withId(R.id.et_event_comments_textbox))
                    .perform(typeText("test comment"), closeSoftKeyboard());

            onView(withId(R.id.btn_event_comments_post_comment))
                    .perform(click());

            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] commentExists = {false};
            final String[] newCommentID = {null};

            db.collection("comments")
                    .whereEqualTo("text", "test comment")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            commentExists[0] = true;
                            newCommentID[0] = querySnapshot.getDocuments().get(0).getId();
                            commentIdsToDelete.add(newCommentID[0]);
                        }
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> latch.countDown());

            latch.await(5, TimeUnit.SECONDS);

            Thread.sleep(3000);
            TestCase.assertTrue(commentExists[0]);
            onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.lv_event_comments_list))
                    .atPosition(0)
                    .onChildView(withId(R.id.tv_comment_text))
                    .check(matches((withText("test comment"))));
        }
    }

    @Test
    public void testDeleteComment() throws InterruptedException{
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCommentsActivity.class);
        intent.putExtra("eventId", eventID);

        try (ActivityScenario<EventCommentsActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.et_event_comments_textbox))
                    .perform(typeText("test comment"), closeSoftKeyboard());

            onView(withId(R.id.btn_event_comments_post_comment))
                    .perform(click());

            onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.lv_event_comments_list)).atPosition(0)
                    .onChildView(withId(R.id.btn_comment_delete)).perform(click());

            onView(withText("Delete"))
                    .inRoot(isDialog()).perform(click());

            Thread.sleep(3000);

            onView(withText("test comment")).check(doesNotExist());
        }
    }
}
