package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class adminNotificationsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications_list);
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        db.collection("notifications")
                // order by date newest first
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<java.util.Map<String, String>> data = new java.util.ArrayList<>();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        java.util.Map<String, String> notification = new java.util.HashMap<>();

                        // Map firestore fields to our list keys
                        // using "event" as the title and "text" as the sub-item
                        notification.put("event", document.getString("title"));
                        notification.put("text", document.getString("text"));

                        com.google.firebase.Timestamp timestamp = document.getTimestamp("date");
                        if (timestamp != null) {
                            notification.put("date", timestamp.toDate().toString());)
                        }

                        data.add(notification);
                    }

                    // Define the mapping from Map keys to XML IDs
                    // AKA put it into the ui
                    String[] from = {"event", "text"};
                    int[] to = {R.id.item_name, R.id.item_subtext};

                    // Set the adapter
                    android.widget.SimpleAdapter adapter = new android.widget.SimpleAdapter(
                            this,
                            data,
                            R.layout.notification_item,
                            from,
                            to
                    );

                    ListView.setAdapter(adapter);

                });

        .addOnFailureListner(e -> {
            android.util.log.e("FIRESTORE", "ERROR FETCHING NOTIFICATIONS", e);
        });


    }
}
