package com.example.projecteventlotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static junit.framework.TestCase.assertTrue;

import android.provider.Settings;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.User;
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
    private final List<String> createdOrganizerIds = new ArrayList<>();
    private final List<String> createdAdminIds = new ArrayList<>();

    @Rule
    public ActivityScenarioRule<RegistrationActivity> scenario = new ActivityScenarioRule<>(RegistrationActivity.class);

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
        entrantsRef = db.collection("entrants");
    }

    @After
    public void tearDown() throws InterruptedException {
        // Clean up entrants
        for (String docId : createdEntrantIds) {
            if (docId == null) continue;
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("entrants").document(docId).delete()
                    .addOnCompleteListener(task -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
        // Clean up organizers
        for (String docId : createdOrganizerIds) {
            if (docId == null) continue;
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("organizers").document(docId).delete()
                    .addOnCompleteListener(task -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
        // Clean up admins
        for (String docId : createdAdminIds) {
            if (docId == null) continue;
            CountDownLatch latch = new CountDownLatch(1);
            db.collection("admins").document(docId).delete()
                    .addOnCompleteListener(task -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void registerEntrant() throws InterruptedException {
        // Set fields
        onView(withId(R.id.et_registration_name)).perform(typeText("TestUser1"), closeSoftKeyboard());
        onView(withId(R.id.et_registration_email)).perform(typeText("testUser1@test.com"), closeSoftKeyboard());
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
                        newEntrantId[0] = querySnapshot.getDocuments().get(0).getId();
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);
        Thread.sleep(3000);
        assertTrue(userExists[0]);
        createdEntrantIds.add(newEntrantId[0]);
    }

    @Test
    public void registerOrganizer() throws InterruptedException {
        // Set fields
        onView(withId(R.id.et_registration_name)).perform(typeText("TestOrg1"), closeSoftKeyboard());
        onView(withId(R.id.et_registration_email)).perform(typeText("testOrg1@test.com"), closeSoftKeyboard());
        onView(withId(R.id.btn_registration_organizer)).perform(click());
        onView(withId(R.id.btn_registration_signup)).perform(click());

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] userExists = {false};
        final String[] newOrganizerId = {null};

        db.collection("organizers")
                .whereEqualTo("name", "TestOrg1")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        userExists[0] = true;
                        newOrganizerId[0] = querySnapshot.getDocuments().get(0).getId();
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);
        Thread.sleep(3000);
        assertTrue(userExists[0]);
        createdOrganizerIds.add(newOrganizerId[0]);
    }

    @Test
    public void registerAdmin() throws InterruptedException {
        // Set fields
        onView(withId(R.id.et_registration_name)).perform(typeText("TestAdmin1"), closeSoftKeyboard());
        onView(withId(R.id.et_registration_email)).perform(typeText("testAdmin1@test.com"), closeSoftKeyboard());
        onView(withId(R.id.btn_registration_admin)).perform(click());
        onView(withId(R.id.btn_registration_signup)).perform(click());

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] userExists = {false};
        final String[] newAdminId = {null};

        db.collection("admins")
                .whereEqualTo("name", "TestAdmin1")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        userExists[0] = true;
                        newAdminId[0] = querySnapshot.getDocuments().get(0).getId();
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);
        Thread.sleep(3000);
        assertTrue(userExists[0]);
        createdAdminIds.add(newAdminId[0]);
    }

    @Test
    public void loginExistingUser() throws InterruptedException {
        // get device id
        String deviceId = Settings.Secure.getString(
                InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // create existing user
        User testUser = new User(Role.ENTRANT, deviceId, "ExistingUser", "existing@test.com", "780-000-0000");

        CountDownLatch setupLatch = new CountDownLatch(1);
        db.collection("entrants").document(deviceId).set(testUser)
                .addOnCompleteListener(task -> setupLatch.countDown());
        setupLatch.await(5, TimeUnit.SECONDS);
        createdEntrantIds.add(deviceId);

        // login
        onView(withId(R.id.btn_registration_login)).perform(click()); // click login button from registration screen
        onView(withId(R.id.btn_registration_entrant)).perform(click());
        onView(withId(R.id.btn_registration_login)).perform(click()); // click login button from login screen

        // check the user document still exists in db
        CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] stillExists = {false};
        db.collection("entrants").document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        stillExists[0] = true;
                    }
                    verifyLatch.countDown();
                })
                .addOnFailureListener(e -> verifyLatch.countDown());

        verifyLatch.await(5, TimeUnit.SECONDS);
        Thread.sleep(3000);
        assertTrue(stillExists[0]);
    }
}
