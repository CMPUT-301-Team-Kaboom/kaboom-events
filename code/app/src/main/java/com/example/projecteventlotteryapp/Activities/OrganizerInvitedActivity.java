package com.example.projecteventlotteryapp.Activities;

import static com.example.projecteventlotteryapp.dbUtils.FirestoreUtils.storeNotificationInFirestore;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.projecteventlotteryapp.Models.CreateNotificationDialogFragment;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.OrganizerEntrantListAdapter;
import com.example.projecteventlotteryapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Set;

public class OrganizerInvitedActivity extends AppCompatActivity implements CreateNotificationDialogFragment.NotificationListener {
    private String eventId;
    private String eventName;
    private OrganizerEntrantListAdapter adapter;
    private ListView invitedListView;
    private ImageButton backBtn;
    private Button selectBtn;
    private Button doneBtn;
    private Button sendNotifBtn;
    private ConstraintLayout floatingActionsContainer;
    private ArrayList<String> invitedList;
    private FirebaseFirestore db;
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_invited);

        // finding ui elements
        invitedListView = findViewById(R.id.lv_organizer_invited_list);
        Intent intent  = getIntent();
        eventId = intent.getStringExtra("eventID");
        eventName = intent.getStringExtra("eventName");

        selectBtn = findViewById(R.id.btn_organizer_invited_select);
        doneBtn = findViewById(R.id.btn_done);
        floatingActionsContainer = findViewById(R.id.cl_floating_actions);
        backBtn = findViewById(R.id.btn_organizer_invited_back);
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
                    invitedList = (ArrayList<String>) doc.get("invited");
                    if (invitedList == null) invitedList = new ArrayList<>();

                    adapter = new OrganizerEntrantListAdapter(this, invitedList);
                    invitedListView.setAdapter(adapter);

                    TextView invitedSize = findViewById(R.id.tv_organizer_invited_size);
                    invitedSize.setText(String.valueOf(invitedList.size()));
                }
            } else {
                Log.d("OrganizerInvited", "Document retrieval failed", task.getException());
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
            } else {
                Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onSendNotification(String message) {
        // handle the sending logic

        // fetch userID from global app state
        String userId = ((MyApp) getApplication()).getCurrentUser().getUserId();

        Set<Integer> selected = adapter.getSelectedPositions();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Integer pos : selected) {
            String recipientId = invitedList.get(pos);
            storeNotificationInFirestore(userId, recipientId, message, eventName, db);
        }

        Toast.makeText(this, "Notifications sent", Toast.LENGTH_SHORT).show();

        // Clear selection after sending
        isSelectionMode = false;
        selectBtn.setText("Select");
        floatingActionsContainer.setVisibility(View.GONE);
        adapter.setSelectionMode(false);
        adapter.clearSelection();
    }
}
