package com.example.projecteventlotteryapp.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projecteventlotteryapp.AdminEventArrayAdapter;
import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.R;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Displays a list of all events for the admin to see and manage.
 */
public class AdminEventsActivity extends AppCompatActivity {
    private ListView eventListView;
    private AdminEventArrayAdapter eventAdapter;
    private ArrayList<Event> eventDataList;
    private FirebaseFirestore db;
    private EventUtils eventUtils;

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

        db = FirebaseFirestore.getInstance();
        eventUtils = new EventUtils(db);

        ImageButton backButton = findViewById(R.id.btn_admin_events_back);
        backButton.setOnClickListener(v -> finish());

        eventListView = findViewById(R.id.lv_admin_events_list);
        eventDataList = new ArrayList<>();

        eventAdapter = new AdminEventArrayAdapter(this, eventDataList, event -> {
            new AlertDialog.Builder(this, R.style.DeleteGuard)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Delete", ((dialog, which) -> {
                        db.collection("events").document(event.getEventId()).delete()
                                .addOnSuccessListener(aVoid -> {
                                    eventDataList.remove(event);
                                    eventAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Log.e("AdminEvents", "Error deleting event", e));
                    }))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        eventListView.setAdapter(eventAdapter);

        // Set click listener to navigate to event details
        eventListView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventDataList.get(position);
            Intent intent = new Intent(AdminEventsActivity.this, EventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getEventId());
            startActivity(intent);
        });

        loadEvents();
    }

    private void loadEvents() {
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            eventDataList.clear();

            // handle empty list
            if (queryDocumentSnapshots.isEmpty()) {
                eventAdapter.notifyDataSetChanged();
                return;
            }

            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                Event event = eventUtils.fetchEventFromSnapshot(snapshot);

                // get organizer
                DocumentReference organizerRef = snapshot.getDocumentReference("organizer");
                if (organizerRef != null) {
                    eventUtils.fetchOrganizerForEvent(event, organizerRef)
                            .addOnSuccessListener(aVoid -> {
                                eventAdapter.notifyDataSetChanged();
                            });
                }

                // get poster
                DocumentReference posterRef = snapshot.getDocumentReference("poster");
                if (posterRef != null) {
                    eventUtils.fetchPosterForEvent(event, posterRef)
                            .addOnSuccessListener(aVoid -> {
                                eventAdapter.notifyDataSetChanged();
                            });
                }

                // update adapter
                eventDataList.add(event);
                eventAdapter.notifyDataSetChanged();
            }
        });
    }
}
