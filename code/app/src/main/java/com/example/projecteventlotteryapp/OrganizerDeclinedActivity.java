package com.example.projecteventlotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrganizerDeclinedActivity extends AppCompatActivity {
    private String eventId;
    private OrganizerEntrantListAdapter adapter;
    private ListView declinedListView;
    private ImageButton backBtn;
    private ArrayList<String> declined;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_declined);

        declinedListView = findViewById(R.id.lv_organizer_declined_list);
        Intent intent  = getIntent();
        eventId = intent.getStringExtra("eventID");

        db = FirebaseFirestore.getInstance();
        DocumentReference eventDoc = db.collection("events").document(eventId);

        eventDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if(doc.exists()){
                    declined = (ArrayList<String>) doc.get("declined");

                    adapter = new OrganizerEntrantListAdapter(this, declined);
                    declinedListView.setAdapter(adapter);

                    TextView declinedSize = findViewById(R.id.tv_organizer_declined_size);
                    declinedSize.setText(String.valueOf(declined.size()));
                }
            } else {
                Log.d("OrganizerInvited", "Document retrieval failed", task.getException());
            }
        });

        backBtn = findViewById(R.id.btn_organizer_declined_back);
        backBtn.setOnClickListener(v -> finish());
    }
}
