package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class adminNotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications_list);
        
        ListView listView = findViewById(R.id.notificationsListView);
        ImageButton btnBack = findViewById(R.id.BackButton);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());

        // Use .get() to fetch notifications once
        db.collection("notifications")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        List<Map<String, String>> data = new ArrayList<>();
                        int count = 1;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Map<String, String> notification = new HashMap<>();

                            // Store document ID for deletion functionality
                            notification.put("id", document.getId());

                            String sender = document.getString("sender");
                            String eventName = document.getString("event");
                            if (eventName == null) eventName = document.getString("title");

                            String displayTitle = count + ". " + (eventName != null ? eventName : "General Notification");
                            notification.put("event", displayTitle);

                            String message = document.getString("text");
                            String displaySubtext = "From: " + (sender != null ? sender : "System") + "\n" + (message != null ? message : "");
                            notification.put("text", displaySubtext);

                            Timestamp timestamp = document.getTimestamp("date");
                            if (timestamp != null) {
                                notification.put("date", timestamp.toDate().toString());
                            }

                            data.add(notification);
                            count++;
                        }

                        String[] from = {"event", "text"};
                        int[] to = {R.id.item_name, R.id.item_subtext};

                        // Custom SimpleAdapter to handle button clicks within items
                        SimpleAdapter adapter = new SimpleAdapter(
                                this,
                                data,
                                R.layout.notification_item,
                                from,
                                to
                        ) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                ImageButton deleteBtn = view.findViewById(R.id.btn_delete_notification);
                                
                                deleteBtn.setOnClickListener(v -> {
                                    String docId = data.get(position).get("id");
                                    db.collection("notifications").document(docId).delete()
                                            .addOnSuccessListener(aVoid -> {
                                                data.remove(position);
                                                // Re-number remaining items to keep the list consistent
                                                reNumber(data);
                                                notifyDataSetChanged();
                                                Toast.makeText(adminNotificationsActivity.this, "Notification deleted", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("FIRESTORE", "Error deleting notification", e);
                                                Toast.makeText(adminNotificationsActivity.this, "Failed to delete notification", Toast.LENGTH_SHORT).show();
                                            });
                                });
                                return view;
                            }

                            private void reNumber(List<Map<String, String>> list) {
                                for (int i = 0; i < list.size(); i++) {
                                    Map<String, String> item = list.get(i);
                                    String currentTitle = item.get("event");
                                    if (currentTitle != null && currentTitle.contains(". ")) {
                                        // Extract original event name and update number
                                        String nameOnly = currentTitle.substring(currentTitle.indexOf(". ") + 2);
                                        item.put("event", (i + 1) + ". " + nameOnly);
                                    }
                                }
                            }
                        };

                        listView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Error fetching notifications", e);
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                });
    }
}
