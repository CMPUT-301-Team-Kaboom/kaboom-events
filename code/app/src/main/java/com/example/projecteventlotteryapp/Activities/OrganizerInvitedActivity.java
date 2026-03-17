package com.example.projecteventlotteryapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class OrganizerInvitedActivity extends AppCompatActivity {
    private String eventId;
    private OrganizerEntrantListAdapter adapter;
    private ListView invitedListView;
    private ImageButton backBtn;
    private ArrayList<String> invited;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_invited);

        invitedListView = findViewById(R.id.lv_organizer_invited_list);
        Intent intent  = getIntent();
        eventId = intent.getStringExtra("eventID");

        db = FirebaseFirestore.getInstance();
        DocumentReference eventDoc = db.collection("events").document(eventId);

        eventDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if(doc.exists()){
                    invited = (ArrayList<String>) doc.get("invited");

                    adapter = new OrganizerEntrantListAdapter(this, invited);
                    invitedListView.setAdapter(adapter);

                    TextView invitedSize = findViewById(R.id.tv_organizer_invited_size);
                    invitedSize.setText(String.valueOf(invited.size()));
                }
            } else {
                Log.d("OrganizerInvited", "Document retrieval failed", task.getException());
            }
        });

        backBtn = findViewById(R.id.btn_organizer_invited_back);
        backBtn.setOnClickListener(v -> finish());
    }
}
