package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
                            notification.put("number", String.valueOf(count));

                            String recipient = document.getString("recipient");
                            String eventName = document.getString("event");
                            if (eventName == null) eventName = document.getString("title");

                            notification.put("event", eventName != null ? eventName : "General Notification");

                            String message = document.getString("text");
                            String displaySubtext = "To: " + (recipient != null ? recipient : "All") + "\n" + (message != null ? message : "No message content") ;
                            notification.put("text", displaySubtext);

                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

                            Timestamp timestamp = document.getTimestamp("date");
                            if (timestamp != null) {
                                String formattedDate = dateFormat.format(timestamp.toDate());
                                notification.put("date", formattedDate);
                            } else {
                                notification.put("date", "No date");
                            }

                            data.add(notification);
                            count++;
                        }

                        String[] from = {"number", "event", "text", "date"};
                        int[] to = {R.id.item_number, R.id.item_name, R.id.item_subtext, R.id.date};

                        SimpleAdapter adapter = new SimpleAdapter(
                                this,
                                data,
                                R.layout.notification_item,
                                from,
                                to
                        ) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                // find the views
                                View view = super.getView(position, convertView, parent);
                                Button deleteBtn = view.findViewById(R.id.btn_delete);
                                View divider = view.findViewById(R.id.divider);

                                // logic to hide divider for the last item
                                if (divider != null) {
                                    if (position == getCount() - 1) {
                                        // It's the last item, hide the line
                                        divider.setVisibility(View.GONE);
                                    } else {
                                        // It's not the last item, make sure the line is visible
                                        // (Necessary because ListView recycles views)
                                        divider.setVisibility(View.VISIBLE);
                                    }
                                }
                                deleteBtn.setOnClickListener(v -> {
                                    String docId = data.get(position).get("id");
                                    db.collection("notifications").document(docId).delete()
                                            .addOnSuccessListener(aVoid -> {
                                                data.remove(position);
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
                                    list.get(i).put("number", String.valueOf(i + 1));
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
