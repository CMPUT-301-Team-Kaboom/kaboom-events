package com.example.projecteventlotteryapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.projecteventlotteryapp.Models.CreateNotificationDialogFragment;
import com.example.projecteventlotteryapp.OrganizerEntrantListAdapter;
import com.example.projecteventlotteryapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides an organizer with the waitlist of entrants that have entered to join their event
 */
public class OrganizerWaitlistActivity extends AppCompatActivity implements CreateNotificationDialogFragment.NotificationListener {
    private String eventId;
    private String eventName;
    private OrganizerEntrantListAdapter adapter;
    private ListView waitlistView;
    private ImageButton backBtn;
    private Button selectBtn;
    private Button notificationBtn;
    private Button doneBtn;
    private Button sendNotifBtn;
    private ConstraintLayout floatingActionsContainer;
    private ArrayList<String> waitlist;
    private FirebaseFirestore db;
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_waitlist);

        // finding ui elements
        waitlistView = findViewById(R.id.lv_organizer_waitlist_list);
        Intent intent  = getIntent();
        eventId = intent.getStringExtra("eventID");
        eventName = intent.getStringExtra("eventName");

        selectBtn = findViewById(R.id.btn_organizer_waitlist_select);
        notificationBtn = findViewById(R.id.btn_send_notification);
        doneBtn = findViewById(R.id.btn_done);
        floatingActionsContainer = findViewById(R.id.cl_floating_actions);
        backBtn = findViewById(R.id.btn_organizer_waitlist_back);
        backBtn.setOnClickListener(v -> finish());
        sendNotifBtn = findViewById(R.id.btn_send_notification);


        // hide notification buttons container initially
        floatingActionsContainer.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        DocumentReference eventDoc = db.collection("events").document(eventId);

        eventDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if(doc.exists()){
                    if (eventName == null) {
                        eventName = doc.getString("name");
                    }
                    waitlist = (ArrayList<String>) doc.get("waitlist");

                    adapter = new OrganizerEntrantListAdapter(this, waitlist);
                    waitlistView.setAdapter(adapter);

                    TextView waitlistSize = findViewById(R.id.tv_organizer_waitlist_size);
                    waitlistSize.setText(waitlist.size() + "/" + doc.get("waitlistLimit"));
                }
            } else {
                Log.d("OrganizerWaitlist", "Document retrieval failed", task.getException());
            }
        });

        selectBtn.setOnClickListener(v -> {
            if (!isSelectionMode) {
                // Enter selection mode
                isSelectionMode = true;
                selectBtn.setText("Select All");
                floatingActionsContainer.setVisibility(View.VISIBLE);
                adapter.setSelectionMode(true);
            } else {
                // select all otherwise
                adapter.selectAll();
            }
            });

        doneBtn.setOnClickListener(v -> {
            // Exit Selection Mode
            isSelectionMode = false;
            selectBtn.setText("Select");
            floatingActionsContainer.setVisibility(View.GONE);
            adapter.setSelectionMode(false);
            adapter.clearSelection();
        });

        sendNotifBtn.setOnClickListener(v -> {
            // Only show if users are selected
            if (!adapter.getSelectedPositions().isEmpty()) {
                CreateNotificationDialogFragment.newInstance()
                        .show(getSupportFragmentManager(), "create_notification");
            }
        });

    }

    @Override
    public void onSendNotification(String message) {
        // Here you handle the actual Firestore saving logic
        Set<Integer> selected = adapter.getSelectedPositions();
        if (selected.isEmpty()) {
            android.widget.Toast.makeText(this, "Please select at least one user", android.widget.Toast.LENGTH_SHORT).show();;
            return;
        }

        for (Integer pos : selected) {
            String userId = waitlist.get(pos);
            sendToFirestore(userId, message);
        }
    }

    private void sendToFirestore(String userId, String message) {
        // Get the sender's device ID
        String senderID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Map<String, Object> notif = new HashMap<>();
        notif.put("sender", senderID);
        notif.put("recipient", userId);
        notif.put("text", message);
        notif.put("date", com.google.firebase.Timestamp.now());
        notif.put("event", eventName != null ? eventName : "Waitlist Update");

        db.collection("notifications").add(notif)
        .addOnSuccessListener(documentReference -> {
            Log.d("Notification", "Notification stored with ID: " + documentReference.getId());
        })
        .addOnFailureListener(e -> {
            Log.w("Firestore", "Error adding notification", e);
        });

    }
}
