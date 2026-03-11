package com.example.projecteventlotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.tabs.TabLayout;

public class EventsListActivity extends AppCompatActivity {
    private Button organizerController;
    private TabLayout entrantController;
    private User globalUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_events_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.events_list_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        MyApp app = (MyApp) getApplication();
        globalUser = app.getCurrentUser();

        organizerController = findViewById(R.id.btn_create_event);
        entrantController = findViewById(R.id.tl_events_list);

        configureUIForRole(globalUser);

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

        /*
        Kevin Cao
        finding and setting click listener to route info button to criteria activity
        */
        // find button
        ImageButton infoButton = findViewById(R.id.btn_info);

        // Set click listener
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EventsListActivity.this, CriteriaAppGuideActivity.class));
            }
        });
    }

    private void configureUIForRole(User user) {
        if (user.getRole() == Role.ORGANIZER) {
            organizerController.setVisibility(View.VISIBLE);
            entrantController.setVisibility(View.GONE);

            organizerController.setOnClickListener(view -> {

                CreateEventDialogFragment createEventDialogFragment = new CreateEventDialogFragment();
                createEventDialogFragment.show(getSupportFragmentManager(), "Create Event");
            });

        } else if (user.getRole() == Role.ENTRANT) {
            organizerController.setVisibility(View.GONE);
            entrantController.setVisibility(View.VISIBLE);

            entrantController.addTab(entrantController.newTab().setText("Available"));
            entrantController.addTab(entrantController.newTab().setText("WaitList"));
            entrantController.addTab(entrantController.newTab().setText("Enrolled"));
            entrantController.addTab(entrantController.newTab().setText("Declined"));
            entrantController.addTab(entrantController.newTab().setText("History"));

            // todo: add filtering with the tabs might need to refactor code
        }
    }
}