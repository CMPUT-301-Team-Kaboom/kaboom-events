package com.example.projecteventlotteryapp.Activities;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import static com.example.projecteventlotteryapp.dbUtils.FirestoreUtils.storeNotificationInFirestore;


/**
 * Activity responsible for displaying a list of waitlisted users for the current event.
 * Organizers can select users to send out notifications
 *
 */
public class OrganizerWaitlistActivity extends BaseActivity implements CreateNotificationDialogFragment.NotificationListener {
    private String eventId;
    private String eventName;
    private boolean isPrivate;
    private OrganizerEntrantListAdapter adapter;
    private ListView waitlistView;
    private ImageButton backBtn, shareBtn;
    private Button selectBtn;
    private Button doneBtn;
    private Button sendNotifBtn;
    private ConstraintLayout floatingActionsContainer;
    private ArrayList<String> waitlist;
    private FirebaseFirestore db;
    private boolean isSelectionMode = false;
    private String limit;

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
        doneBtn = findViewById(R.id.btn_done);
        floatingActionsContainer = findViewById(R.id.cl_floating_actions);
        backBtn = findViewById(R.id.btn_organizer_waitlist_back);
        backBtn.setOnClickListener(v -> finish());
        sendNotifBtn = findViewById(R.id.btn_send_notification);
        shareBtn = findViewById(R.id.btn_organizer_waitlist_share);

        // hide notification buttons container initially
        floatingActionsContainer.setVisibility(GONE);

        db = FirebaseFirestore.getInstance();
        DocumentReference eventDoc = db.collection("events").document(eventId);

        eventDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if(doc.exists()){
                    isPrivate = doc.getBoolean("isPrivate");
                    if (isPrivate) { // show share button if private event
                        shareBtn.setVisibility(VISIBLE);
                        shareBtn.setOnClickListener(v -> {
                            Intent inviteActivityIntent = new Intent(OrganizerWaitlistActivity.this, OrganizerPrivateInvitationsActivity.class);
                            inviteActivityIntent.putExtra("eventId", eventId);
                            inviteActivityIntent.putExtra("eventName", eventName);
                            startActivity(inviteActivityIntent);
                        });
                    } else { // hide share button if public event
                        shareBtn.setVisibility(GONE);
                    }

                    if (eventName == null) {
                        eventName = doc.getString("name");
                    }
                    waitlist = (ArrayList<String>) doc.get("waitlist");

                    adapter = new OrganizerEntrantListAdapter(this, waitlist);
                    waitlistView.setAdapter(adapter);

                    TextView waitlistSize = findViewById(R.id.tv_organizer_waitlist_size);
                    limit = doc.get("waitlistLimit").toString();
                    if (limit.equals("-1")) {
                        waitlistSize.setText(String.valueOf(waitlist.size()));
                    } else {
                        // Concatenation naturally converts the whole expression to a String
                        waitlistSize.setText(waitlist.size() + "/" + doc.get("waitlistLimit"));
                    }
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
                floatingActionsContainer.setVisibility(VISIBLE);
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
            floatingActionsContainer.setVisibility(GONE);
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
            String recipientId = waitlist.get(pos);
            storeNotificationInFirestore(userId, recipientId, message, eventName, eventId, db, this);
        }

        // Clear selection after sending
        isSelectionMode = false;
        selectBtn.setText("Select");
        floatingActionsContainer.setVisibility(GONE);
        adapter.setSelectionMode(false);
        adapter.clearSelection();
    }

}
