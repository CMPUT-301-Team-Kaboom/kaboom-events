package com.example.projecteventlotteryapp;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertFalse;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.projecteventlotteryapp.Activities.AdminHomeActivity;
import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminTestsViaUI {
    private FirebaseFirestore db;
    private final List<String> entrantIdsToDelete = new ArrayList<>();
    private final List<String> organizerIdsToDelete = new ArrayList<>();
    private final List<String> eventIdsToDelete = new ArrayList<>();

    @Rule
    public ActivityScenarioRule<AdminHomeActivity> scenario = new ActivityScenarioRule<>(AdminHomeActivity.class);

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
    }

    @After
    public void tearDown() throws InterruptedException {
        // clean up any test entrants created
        for (String id : entrantIdsToDelete) {
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("entrants").document(id).delete()
                    .addOnCompleteListener(task -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
        // clean up any test organizers created
        for (String id : organizerIdsToDelete) {
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("organizers").document(id).delete()
                    .addOnCompleteListener(task -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }


    }

    @Test
    public void testDeleteEntrant() throws InterruptedException {
        // put test entrant into db
        final String testId = "test_entrant_ui_" + System.currentTimeMillis();
        User testUser = new User(Role.ENTRANT, testId, "Delete Me Entrant", "delete_e@test.com", "1234567890");

        CountDownLatch setupLatch = new CountDownLatch(1);
        db.collection("entrants").document(testId).set(testUser)
                .addOnCompleteListener(task -> setupLatch.countDown());
        setupLatch.await(5, TimeUnit.SECONDS);
        entrantIdsToDelete.add(testId);

        // navigate from admin home to entrant list screen
        onView(withId(R.id.btn_view_entrants)).perform(click());

        // code adapted from https://developer.android.com/training/testing/espresso/lists
        onData(new TypeSafeMatcher<User>() {
            @Override
            protected boolean matchesSafely(User item) {
                return testId.equals(item.getUserId());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("User with userId " + testId);
            }
        })
                .inAdapterView(withId(R.id.lv_entrant_list))
                .onChildView(withId(R.id.iv_profile_item_delete))
                .perform(click());

        onView(withText("Delete"))
                .inRoot(isDialog()).perform(click());

        Thread.sleep(3000);

        CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] exists = {true};
        db.collection("entrants").document(testId).get()
                .addOnSuccessListener(doc -> {
                    exists[0] = doc.exists();
                    verifyLatch.countDown();
                })
                .addOnFailureListener(e -> verifyLatch.countDown());

        Thread.sleep(5000);
        verifyLatch.await(5, TimeUnit.SECONDS);
        assertFalse("Entrant should have been deleted from Firestore", exists[0]);
    }

    @Test
    public void testDeleteOrganizer() throws InterruptedException {
        // put test organizer into db
        final String testId = "test_org_ui_" + System.currentTimeMillis();
        User testUser = new User(Role.ORGANIZER, testId, "Delete Me Organizer", "delete_o@test.com", "0987654321");

        CountDownLatch setupLatch = new CountDownLatch(1);
        db.collection("organizers").document(testId).set(testUser)
                .addOnCompleteListener(task -> setupLatch.countDown());
        setupLatch.await(5, TimeUnit.SECONDS);
        organizerIdsToDelete.add(testId);

        // navigate from admin home to organizer list screen
        onView(withId(R.id.btn_view_organizers)).perform(click());

        onData(new TypeSafeMatcher<User>() {
            @Override
            protected boolean matchesSafely(User item) {
                return testId.equals(item.getUserId());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("User with userId " + testId);
            }
        })
                .inAdapterView(withId(R.id.lv_organizer_list))
                .onChildView(withId(R.id.iv_organizer_item_delete))
                .perform(click());

        onView(withText("Delete"))
                .inRoot(isDialog()).perform(click());

        CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] exists = {true};
        db.collection("organizers").document(testId).get()
                .addOnSuccessListener(doc -> {
                    exists[0] = doc.exists();
                    verifyLatch.countDown();
                })
                .addOnFailureListener(e -> verifyLatch.countDown());

        Thread.sleep(5000);
        verifyLatch.await(5, TimeUnit.SECONDS);
        assertFalse(exists[0]);
    }


}
