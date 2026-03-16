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

import com.example.projecteventlotteryapp.Activities.LogInActivity;
import com.example.projecteventlotteryapp.Activities.RegistrationActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegistrationTestsViaIntent {

    @Rule
    public ActivityScenarioRule<RegistrationActivity> scenario = new ActivityScenarioRule<>(RegistrationActivity.class);

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testNavigateToLogin() {
        onView(withId(R.id.btn_registration_login)).perform(click());
        intended(hasComponent(LogInActivity.class.getName()));
    }
}
