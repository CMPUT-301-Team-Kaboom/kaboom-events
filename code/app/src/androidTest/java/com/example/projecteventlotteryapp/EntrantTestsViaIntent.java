// reference: https://developer.android.com/training/testing/espresso/intents
package com.example.projecteventlotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntrantTestsViaIntent {

    @Before
    public void setup() {
        Intents.init();
        // new user
        MyApp app = (MyApp) ApplicationProvider.getApplicationContext();
        User testUser = new User(Role.ENTRANT, "test_entrant_ui", "Test Entrant", "entrant@test.com", "1234567890");
        app.setCurrentUser(testUser);
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testNavigateToCriteria() {
        try (ActivityScenario<EventsListActivity> scenario = ActivityScenario.launch(EventsListActivity.class)) {
            onView(withId(R.id.btn_info)).perform(click());
            intended(hasComponent(CriteriaAppGuideActivity.class.getName()));
        }
    }

    @Test
    public void testNavigateToSettings() {
        try (ActivityScenario<EventsListActivity> scenario = ActivityScenario.launch(EventsListActivity.class)) {
            onView(withId(R.id.profile)).perform(click());
            intended(hasComponent(EntrantSettingsActivity.class.getName()));
        }
    }
}
