package com.example.projecteventlotteryapp.Activities;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projecteventlotteryapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView notificationsListView;
    private List<Map<String, Object>> notificationsList;
    private SimpleAdapter adapter;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_list);

        // find UI elements
        notificationsListView = findViewById(R.id.lv_notificationsListView);
        backButton = findViewById(R.id.BackButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        notificationsList = new ArrayList<>();
        // Using R.layout.notification_list_item which contains the IDs used in the mapping
        adapter = new SimpleAdapter(
                this,
                notificationsList,
                R.layout.notification_list_item,
                new String[]{"title", "organizerName", "notificationText"},
                new int[]{R.id.txt_event_name, R.id.txt_organizer_name, R.id.txt_notification_text}
        );

        if (notificationsListView != null) {
            notificationsListView.setAdapter(adapter);
            notificationsListView.setOnItemClickListener((parent, view, position, id) -> {
                // Handle item click
                Map<String, Object> item = notificationsList.get(position);
                String title = (String) item.get("title");
                String sender = (String) item.get("organizerName");
                String body = (String) item.get("notificationText");

                // Create and show fragment
                FullNotificationWindowFragment.newInstance(title, sender, body)
                        .show(getSupportFragmentManager(), "FullNotificationWindow");
            });
        }

        fetchNotifications();
    }

    private void fetchNotifications() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        db.collection("notifications")
                .whereEqualTo("recipient", deviceId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("NotificationsList", "Listen failed", error);
                        return;
                    }

                    if (value != null) {
                        List<QueryDocumentSnapshot> docs = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            docs.add(doc);
                        }

                        // Sort locally by date descending to avoid requiring a composite index
                        Collections.sort(docs, (d1, d2) -> {
                            java.util.Date date1 = d1.getDate("date");
                            java.util.Date date2 = d2.getDate("date");
                            if (date1 == null && date2 == null) return 0;
                            if (date1 == null) return 1;
                            if (date2 == null) return -1;
                            return date2.compareTo(date1);
                        });

                        notificationsList.clear();
                        for (QueryDocumentSnapshot document : docs) {

                            db.collection("organizers")
                                    .document(document.getString("sender")).get()
                                    .addOnSuccessListener(organizerDocument -> {
                                        Map<String, Object> item = new HashMap<>();
                                        // fetch organizer name
                                        String name = organizerDocument.getString("name");

                                        item.put("title", document.getString("event"));
                                        item.put("organizerName", name );
                                        item.put("notificationText", document.getString("text"));
                                        notificationsList.add(item);

                                        if (notificationsList.size() == docs.size()) {
                                            adapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                    // Handle failure by using the ID as a fallback
                                    Map<String, Object> item = new HashMap<>();
                                    item.put("title", document.getString("event"));
                                    item.put("organizerName", "ID: " + document.getString("sender"));
                                    item.put("notificationText", document.getString("text"));
                                    notificationsList.add(item);
                                    if (notificationsList.size() == docs.size()) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
        }
}
