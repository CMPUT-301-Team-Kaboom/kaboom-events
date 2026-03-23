package com.example.projecteventlotteryapp;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.projecteventlotteryapp.Activities.EventCommentsActivity;

import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventCommentTestsViaUI {
    @Rule
    public ActivityScenarioRule<EventCommentsActivity> scenario = new ActivityScenarioRule<>(EventCommentsActivity.class);


}
