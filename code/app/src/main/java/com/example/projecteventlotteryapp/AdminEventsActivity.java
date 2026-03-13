package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Displays a list of all events for the admin to see.
 */
public class AdminEventsActivity extends AppCompatActivity {

    /**
     * Entry point of the activity.
     *
     * <p>This function is the entry point of the Activity. It sets up the UI for the event.</p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_events_list_main), (v, insets) -> {
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
                    .add(R.id.fl_admin_events_list, EventsListFragment.class, null)
                    .commit();
        }

        ImageButton backButton = findViewById(R.id.btn_admin_events_back);
        backButton.setOnClickListener(v -> finish());
    }
}