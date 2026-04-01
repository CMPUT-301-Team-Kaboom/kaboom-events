package com.example.projecteventlotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projecteventlotteryapp.Activities.CriteriaAppGuideActivity;
import com.example.projecteventlotteryapp.Activities.EntrantSettingsActivity;
import com.example.projecteventlotteryapp.Activities.EventDetailsActivity;
import com.example.projecteventlotteryapp.Activities.NotificationsListActivity;
import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.EventsFilter;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.dbUtils.FirestoreUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

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
public class EventsListActivity extends AppCompatActivity implements FilterEventsDialogFragment.FilterEventsListener, CreateEventDialogFragment.OnEventCreatedListener {
    private Button organizerController;
    private TabLayout entrantController;
    private User globalUser;
    private EventsListFragment eventsListFragment;

    // qr scanner launcher
    private final ActivityResultLauncher<ScanOptions> qrCodeScannerLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            // successful scan
            String scannedEventId = result.getContents();
            Intent intent = new Intent(EventsListActivity.this, EventDetailsActivity.class);
            intent.putExtra("eventId", scannedEventId);
            startActivity(intent);
        }
    });

    /**
     * Entry point of the activity.
     *
     * <p>This function is the entry point of the Activity. It sets up the db instance and UI for
     * the event.</p>
     *
     * Citations:
     *      [1] Title: "Create a fragment | App architecture | Android Developers"
     *          Source: https://developer.android.com/guide/fragments/create#java
     *          Date: 2026-02-26
     *          Retrieved: 2026-02-28
     *      [2] https://www.geeksforgeeks.org/android/how-to-implement-bottom-navigation-with-activities-in-android/
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

        // create EventListFragment (see citation [1])
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

        // set click listener
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
            if (eventsListFragment != null) {
                eventsListFragment.clearFilters();
            }

            FilterEventsDialogFragment filterEventsDialogFragment = new FilterEventsDialogFragment();
            filterEventsDialogFragment.show(getSupportFragmentManager(), "Filter Events");
        });

        /*
        Ashley Kang
        finding bottom navigation and setting on click for tabs
         */

        BottomNavigationView bottomNavigation = findViewById(R.id.bn_events_list_menu);

        // (see citation [2])
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.profile){
                startActivity(new Intent(this, EntrantSettingsActivity.class));
                return true;
            } else if (id == R.id.scan_qrcode){
                ScanOptions options = new ScanOptions();
                options.setPrompt("Scan an Event QR code");
                options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                qrCodeScannerLauncher.launch(options);
                return true;
            } else if (id == R.id.notification){
                startActivity(new Intent(this, NotificationsListActivity.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Refresh the EventsListFragment upon returning to the EventsListActivity from another activity.
     *
     * Code Citation:
     *          [1] Author: user2742861 https://stackoverflow.com/users/2742861/user2742861
     *          Title: "Android refresh activity on close of another"
     *          Answer: https://stackoverflow.com/questions/19277414/android-refresh-activity-on-close-of-another
     *          Date: 2013-10-09
     *          Retrieved: 2026-03-29
     *
     */
      @Override protected void onResume() {
          Log.d("EventsListFragment", "resume");
          super.onResume();

          EventsListFragment fragment = (EventsListFragment)
                  getSupportFragmentManager().findFragmentById(R.id.fl_events_list);

          if (fragment != null) {
              fragment.refreshEventList();
          }
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
     * Code Citation:
     *      [1] Author: Ahmad Sabeh https://stackoverflow.com/users/8614703/ahmad-sabeh
     *                  Title: "How to add tab listener to the tabs"
     *                  Answer: https://stackoverflow.com/a/57358785
     *                  Date: 2019-08-05
     *                  Retrieved: 2026-03-23
     *                  License: CC-BY-SA 4.0
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

            entrantController.addTab(entrantController.newTab().setText("All"));
            entrantController.addTab(entrantController.newTab().setText("Available"));
            entrantController.addTab(entrantController.newTab().setText("History"));

            // (see code citation [1])
            entrantController.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    if (position == 0) { // get all events
                        if (eventsListFragment != null) {
                            eventsListFragment.refreshEventList();
                        }
                    } else if (position == 1) { // get available events
                        if (eventsListFragment != null) {
                            EventsFilter filter = new EventsFilter();
                            LocalDate today = LocalDate.now();
                            filter.regStart = today;
                            filter.regEnd = today;

                            eventsListFragment.applyFilters(filter);
                        }
                    } else if (position == 2) { // get entrant's history of events
                        if (eventsListFragment != null) {
                            eventsListFragment.getEventsHistory(globalUser.getUserId());
                        }
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    // necessary for OnTabSelectedListener but does not do anything
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    // necessary for OnTabSelectedListener but does not do anything
                }
            });
        }
    }

    /**
     * Get the EventsListFragment to apply the filters.
     * @param filter to apply
     */
    @Override
    public void filterEvents(EventsFilter filter) {
        if (eventsListFragment != null) {
            eventsListFragment.applyFilters(filter);
        }
    }

    /**
     * Get the EventsListFragment to clear all the filters.
     */
    @Override
    public void clearFilters() {
        if (eventsListFragment != null) {
            eventsListFragment.clearFilters();
        }
    }

    /**
     * Reset the EventsListFragment to reflect the changes of a newly created Event.
     */
    @Override
    public void OnEventCreated() { if (eventsListFragment != null) eventsListFragment.refreshEventList(); }
}
