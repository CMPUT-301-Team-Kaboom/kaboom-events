// reference: https://developer.android.com/training/testing/espresso/intents
package com.example.projecteventlotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.projecteventlotteryapp.Activities.AdminEventsActivity;
import com.example.projecteventlotteryapp.Activities.AdminHomeActivity;
import com.example.projecteventlotteryapp.Activities.AdminImagesActivity;
import com.example.projecteventlotteryapp.Activities.AdminOrganizersListActivity;
import com.example.projecteventlotteryapp.Activities.AdminProfilesListActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminTestsViaIntent {

    @Rule
    public ActivityScenarioRule<AdminHomeActivity> scenario = new ActivityScenarioRule<>(AdminHomeActivity.class);

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testNavigateToEvents() {
        onView(withId(R.id.btn_view_events)).perform(click());
        intended(hasComponent(AdminEventsActivity.class.getName()));
    }

    @Test
    public void testNavigateToEntrants() {
        onView(withId(R.id.btn_view_entrants)).perform(click());
        intended(hasComponent(AdminProfilesListActivity.class.getName()));
    }

    @Test
    public void testNavigateToOrganizers() {
        onView(withId(R.id.btn_view_organizers)).perform(click());
        intended(hasComponent(AdminOrganizersListActivity.class.getName()));
    }

    @Test
    public void testNavigateToImages() {
        onView(withId(R.id.btn_view_images)).perform(click());
        intended(hasComponent(AdminImagesActivity.class.getName()));
    }
}
