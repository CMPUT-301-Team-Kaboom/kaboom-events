package com.example.projecteventlotteryapp.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecteventlotteryapp.Models.Image;
import com.example.projecteventlotteryapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for managing the notifications sent in the app
 *
 */
public class adminNotificationsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notifications_list);

        // set up elements
        ListView listView = findViewById(R.id.lv_notificationsListView);
        ImageButton btnBack = findViewById(R.id.btn_admin_notifications_back);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get sender ID from intent
        String senderID = getIntent().getStringExtra("sender_id");

        // Set up back button
        btnBack.setOnClickListener(v -> finish());

        // Fetch notifications
        Query notificationQuery = db.collection("notifications");

        if (senderID != null) {
            notificationQuery = notificationQuery.whereEqualTo("sender", senderID);
        }

        // Use .get() to fetch notifications once
        notificationQuery
                //.orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // if the notifications exist
                    if (queryDocumentSnapshots != null) {
                        // start an array and initialize the count
                        List<Map<String, String>> data = new ArrayList<>();
                        int count = 1;
                        // iterate through each notification and add it to the array
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Map<String, String> notification = new HashMap<>();

                            // Store document ID for deletion functionality
                            notification.put("id", document.getId());
                            notification.put("number", String.valueOf(count));

                            // retrieve elements from the document
                            String recipient = document.getString("recipient");
                            String eventName = document.getString("eventName");
                            String message = document.getString("text");

                            String displaySubtext = "To: " + recipient + "\n" + message;

                            // place items into hashmap
                            notification.put("event", eventName);
                            notification.put("text", displaySubtext);

                            // set up date formatting
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

                            Timestamp timestamp = document.getTimestamp("date");
                            String formattedDate = dateFormat.format(timestamp.toDate());
                            notification.put("date", formattedDate);

                            // add the notification to the array and increase the count
                            data.add(notification);
                            count++;
                        }
                        // Define the keys from the data map that we want to display
                        String[] from = {"number", "event", "text", "date"};
                        // Define the corresponding TextView IDs in the layout where the data should be placed
                        int[] to = {R.id.tv_item_number, R.id.tv_item_name, R.id.tv_item_subtext, R.id.tv_date};

                        // Initialize the adapter to bind the data list to the ListView using the specified layout and mapping
                        SimpleAdapter adapter = new SimpleAdapter(
                                this,
                                data,
                                R.layout.notification_item,
                                from,
                                to
                        ) {
                            @Override
                            /**
                             * Method that's run every time a row is drawn in the notifications list
                             * @param position
                             * @param convertView
                             * @param parent
                             *
                             */
                            public View getView(int position, View convertView, ViewGroup parent) {
                                // find the views
                                View view = super.getView(position, convertView, parent);
                                Button deleteBtn = view.findViewById(R.id.btn_delete);
                                View divider = view.findViewById(R.id.v_divider);

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
                                // set up delete button
                                deleteBtn.setOnClickListener(v -> {
                                    new AlertDialog.Builder(parent.getContext(), R.style.DeleteGuard)
                                            .setTitle("Delete Notification")
                                            .setMessage("Are you sure you want to delete this notification?")
                                            .setPositiveButton("Delete", ((dialog, which) -> {
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
                                            }))
                                            .setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()))
                                            .show();

                                });
                                return view;
                            }

                            /**
                             * Method to renumber the notifications after deletion
                             * @param list
                             */
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
