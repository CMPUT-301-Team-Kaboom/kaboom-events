package com.example.projecteventlotteryapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    private Button selectbtn;
    private ArrayList<String> waitlist;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_waitlist);

        waitlistView = findViewById(R.id.lv_organizer_waitlist_list);
        Intent intent  = getIntent();
        eventId = intent.getStringExtra("eventID");

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

        backBtn = findViewById(R.id.btn_organizer_waitlist_back);
        backBtn.setOnClickListener(v -> finish());
    }
}
