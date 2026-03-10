package com.example.projecteventlotteryapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventsListItemActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String eventId;
    private String deviceID;
    private Button btn_waitlist;
    private boolean isOnWaitlist;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_list_item);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        btn_waitlist = findViewById(R.id.btn_waitlist);
        eventId = getIntent().getStringExtra("eventId");

        // Get the device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        checkWaitlistStatus();

        btn_waitlist.setOnClickListener(v -> {
            if (isOnWaitlist) {
                leaveWaitlistDb();
            } else {
                joinWaitlistDb();
            }
        });
    }

    private void checkWaitlistStatus() {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> waitlist = (List<String>) documentSnapshot.get("waitlist");

                        if (waitlist != null && waitlist.contains(deviceID)) {
                            isOnWaitlist = true;
                            btn_waitlist.setText("Leave Waitlist");
                        } else {
                            isOnWaitlist = false;
                            btn_waitlist.setText("Join Waitlist");
                        }
                    }
                });
    }

    private void joinWaitlistDb() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (deviceID == null || deviceID.isEmpty()) {
            Toast.makeText(this, "Device ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .update("waitlist", FieldValue.arrayUnion(deviceID))
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Added to waitlist", Toast.LENGTH_SHORT).show();
                    isOnWaitlist = true;
                    btn_waitlist.setText("Leave Waitlist");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to join waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void leaveWaitlistDb() {
        db.collection("events")
                .document(eventId)
                .update("waitlist", FieldValue.arrayRemove(deviceID))
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Removed from waitlist", Toast.LENGTH_SHORT).show();
                    isOnWaitlist = false;
                    btn_waitlist.setText("Join Waitlist");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to leave waitlist", Toast.LENGTH_SHORT).show()
                );
    }
}

