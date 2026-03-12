package com.example.projecteventlotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * This class handles the logic and UI support for displaying the home screen for both entrants and organizers.
 *
 * <p>The home screen includes an info button, filter button, QR code scan button, profile view button, and
 * notification button. Depending on the role additional functionalities are displayed like tabs for filtering
 * (entrant) or a button to create an event (organizer). A list of events takes up most of the UI containing
 * either all events (entrant) or specific Events (organizer). </p>
 */
public class EventsListActivity extends AppCompatActivity implements FilterEventsDialogFragment.FilterEventsListener{
private Button organizerController;
    private TabLayout entrantController;
    private User globalUser;

    private EventsListFragment eventsListFragment;

    /**
     * Entry point of the activity.
     *
     * <p>This function is the entry point of the Activity. It sets up the db instance and UI for
     * the event.</p>
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
            eventsListFragment = new EventsListFragment();
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fl_events_list, eventsListFragment)
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

        // find button
        ImageButton filterButton = findViewById(R.id.btn_filter);

        // set click listener for filter button
        filterButton.setOnClickListener(view -> {
            FilterEventsDialogFragment filterEventsDialogFragment = new FilterEventsDialogFragment();
            filterEventsDialogFragment.show(getSupportFragmentManager(), "Filter Events");
        });
    }

    /**
     * Configures the UI for a given user depending on their role
     *
     * <p>If the user is an organizer, organizer-specific controls are displayed such as
     * creating an event. Entrant controls are hidden.</p>
     *
     * <p>If the user is an entrant, entrant-specific controls are displayed such as
     * tabs for filtering the event list based on an entrant's status for an event. Organizer
     * controls are hidden.</p>
     *
     * @param user the User interacting with the app
     */
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

    @Override
    public void filterEvents(String name, String status, ArrayList<String> tags, LocalDate startDate, LocalDate endDate, LocalDate drawDate) {
        if (eventsListFragment != null) {
            eventsListFragment.applyFilters(name, status, tags, startDate, endDate, drawDate);
        }
    }
}