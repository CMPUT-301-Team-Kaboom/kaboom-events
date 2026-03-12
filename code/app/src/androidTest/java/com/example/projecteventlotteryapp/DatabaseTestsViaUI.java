package com.example.projecteventlotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static junit.framework.TestCase.assertTrue;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
public class DatabaseTestsViaUI {
    private FirebaseFirestore db;
    private CollectionReference entrantsRef;
    private final List<String> createdEntrantIds = new ArrayList<>();

    @Rule
    public ActivityScenarioRule<RegistrationActivity>scenario=new ActivityScenarioRule<RegistrationActivity>(RegistrationActivity.class);

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
        entrantsRef = db.collection("entrants");
    }

    @After
    public void tearDown()  throws InterruptedException {
        for (String docId : createdEntrantIds) {
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("entrants").document(docId).delete()
                    .addOnSuccessListener(aVoid -> latch.countDown())
                    .addOnFailureListener(e -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void registerEntrant()  throws InterruptedException {

        // Set fields
        onView(withId(R.id.et_registration_name)).perform(ViewActions.typeText("TestUser1"));
        onView(withId(R.id.et_registration_email)).perform(ViewActions.typeText("testUser1@test.com"));
        onView(withId(R.id.btn_registration_entrant)).perform(click());
        onView(withId(R.id.btn_registration_signup)).perform(click());

        CountDownLatch latch = new CountDownLatch(1);

        final boolean[] userExists = {false};
        final String[] newEntrantId = {null};
        db.collection("entrants")
                .whereEqualTo("name", "TestUser1")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        userExists[0] = true;

                        // Get the documentId
                        newEntrantId[0] = querySnapshot.getDocuments().get(0).getId();
                    }

                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    latch.countDown();
                });

        // Wait up to 5 seconds for Firestore
        latch.await(5, TimeUnit.SECONDS);

        // Verify user exists
        assertTrue(userExists[0]);
        createdEntrantIds.add(newEntrantId[0]);
    }

}
