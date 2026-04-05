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

/**
 * Activity responsible for displaying a list of declined users for the current event.
 * Organizers can select users to send out notifications
 *
 */
public class OrganizerDeclinedActivity extends AppCompatActivity implements CreateNotificationDialogFragment.NotificationListener {    private String eventId;
    private String eventName;
    private OrganizerEntrantListAdapter adapter;
    private ListView declinedView;
    private ImageButton backBtn;
    private Button selectBtn;
    private Button doneBtn;
    private Button sendNotifBtn;
    private TextView declineSize;
    private ConstraintLayout floatingActionsContainer;
    private ArrayList<String> declined;
    private FirebaseFirestore db;
    private String limit;
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_declined);

        // finding ui elements
        declinedView = findViewById(R.id.lv_organizer_declined_list);
        Intent intent  = getIntent();
        eventId = intent.getStringExtra("eventID");
        eventName = intent.getStringExtra("eventName");

        selectBtn = findViewById(R.id.btn_organizer_declined_select);
        doneBtn = findViewById(R.id.btn_done);
        floatingActionsContainer = findViewById(R.id.cl_floating_actions);
        backBtn = findViewById(R.id.btn_organizer_declined_back);
        backBtn.setOnClickListener(v -> finish());
        sendNotifBtn = findViewById(R.id.btn_send_notification);
        declineSize = findViewById(R.id.tv_organizer_declined_size);



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
                    declined = (ArrayList<String>) doc.get("declined");

                    adapter = new OrganizerEntrantListAdapter(this, declined);
                    declinedView.setAdapter(adapter);

                    if (declined != null) {
                        declineSize.setText(String.valueOf(declined.size()));
                    } else {
                        declineSize.setText("0");
                    }

                }
            } else {
                Log.d("OrganizerDeclined", "Document retrieval failed", task.getException());
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
        // handle the sending logic

        // fetch userID from global app state
        String userId = ((MyApp) getApplication()).getCurrentUser().getUserId();

        Set<Integer> selected = adapter.getSelectedPositions();
        if (selected.isEmpty()) {
            android.widget.Toast.makeText(this, "Please select at least one user", android.widget.Toast.LENGTH_SHORT).show();;
            return;
        }

        for (Integer pos : selected) {
            String recipientId = declined.get(pos);
            storeNotificationInFirestore(userId, recipientId, message, eventName, eventId, db, this);
        }

        // Clear selection after sending
        isSelectionMode = false;
        selectBtn.setText("Select");
        floatingActionsContainer.setVisibility(View.GONE);
        adapter.setSelectionMode(false);
        adapter.clearSelection();
    }

}
