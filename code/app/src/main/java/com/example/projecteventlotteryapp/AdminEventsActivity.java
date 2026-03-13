package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminEventsActivity extends AppCompatActivity {
    private ListView eventListView;
    private AdminEventArrayAdapter eventAdapter;
    private ArrayList<Event> eventDataList;
    private FirebaseFirestore db;

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

        loadEvents();
    }

    private void loadEvents() {
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                eventDataList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Event event = Event.fetchEventFromSnapshot(doc);

                    DocumentReference organizerRef = doc.getDocumentReference("organizer");
                    if (organizerRef != null) {
                        organizerRef.get().addOnSuccessListener(orgDoc -> {
                            if (orgDoc.exists()) {
                                String organizerName = orgDoc.getString("name");
                                event.setOrganizerName(organizerName);
                                eventAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    
                    eventDataList.add(event);
                }
                eventAdapter.notifyDataSetChanged();
            }
        });
    }
}
