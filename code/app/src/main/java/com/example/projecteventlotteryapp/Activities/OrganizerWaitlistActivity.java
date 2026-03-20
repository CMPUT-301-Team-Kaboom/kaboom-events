package com.example.projecteventlotteryapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.projecteventlotteryapp.OrganizerEntrantListAdapter;
import com.example.projecteventlotteryapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Provides an organizer with the waitlist of entrants that have entered to join their event
 */
public class OrganizerWaitlistActivity extends AppCompatActivity {
    private String eventId;
    private OrganizerEntrantListAdapter adapter;
    private ListView waitlistView;
    private ImageButton backBtn;
    private Button selectBtn;
    private Button notifcationBtn;
    private Button doneBtn;
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

        selectBtn = findViewById(R.id.btn_organizer_waitlist_select);
        notifcationBtn = findViewById(R.id.btn_send_notification);
        doneBtn = findViewById(R.id.btn_done);
        floatingActionsContainer = findViewById(R.id.cl_floating_actions);
        backBtn = findViewById(R.id.btn_organizer_waitlist_back);
        backBtn.setOnClickListener(v -> finish());

        // hide notification buttons container initially
        floatingActionsContainer.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        DocumentReference eventDoc = db.collection("events").document(eventId);

        eventDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if(doc.exists()){
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

    }
}
