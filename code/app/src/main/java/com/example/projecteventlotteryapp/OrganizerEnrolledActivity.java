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

public class OrganizerEnrolledActivity extends AppCompatActivity {
    private String eventId;
    private OrganizerEntrantListAdapter adapter;
    private ListView enrolledListView;
    private ImageButton backBtn;
    private ArrayList<String> enrolled;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_enrolled);

        enrolledListView = findViewById(R.id.lv_organizer_enrolled_list);
        Intent intent  = getIntent();
        eventId = intent.getStringExtra("eventID");

        db = FirebaseFirestore.getInstance();
        DocumentReference eventDoc = db.collection("events").document(eventId);

        eventDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if(doc.exists()){
                    enrolled = (ArrayList<String>) doc.get("enrolled");

                    adapter = new OrganizerEntrantListAdapter(this, enrolled);
                    enrolledListView.setAdapter(adapter);

                    TextView enrolledSize = findViewById(R.id.tv_organizer_enrolled_size);
                    enrolledSize.setText(enrolled.size() + "/" + doc.get("entrantsLimit"));
                }
            } else {
                Log.d("OrganizerEnrolled", "Document retrieval failed", task.getException());
            }
        });

        backBtn = findViewById(R.id.btn_organizer_enrolled_back);
        backBtn.setOnClickListener(v -> finish());
    }
}
