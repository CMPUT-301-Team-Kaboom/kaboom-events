package com.example.projecteventlotteryapp;

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

import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
            db.collection("events").document(event.getEventId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        eventDataList.remove(event);
                        eventAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.e("AdminEvents", "Error deleting event", e));
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
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                eventDataList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    Event event = eventUtils.fetchEventFromSnapshot(doc);
                    eventDataList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            }
        });
    }
}
