package com.example.projecteventlotteryapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EventsListActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.events_list_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // create EventListFragment
        /*
        The following code is adapted from...
        Title: "Create a fragment | App architecture | Android Developers"
        Source: https://developer.android.com/guide/fragments/create#java
        Date: 2026-02-26
        Retrieved: 2026-02-28
        */
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fl_events_list, EventsListFragment.class, null)
                    .commit();
        }

    }
}