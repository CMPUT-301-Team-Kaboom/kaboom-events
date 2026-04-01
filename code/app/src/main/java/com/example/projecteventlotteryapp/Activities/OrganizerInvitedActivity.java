package com.example.projecteventlotteryapp.Activities;

import static com.example.projecteventlotteryapp.dbUtils.FirestoreUtils.storeNotificationInFirestore;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.projecteventlotteryapp.Enums.EntrantListType;
import com.example.projecteventlotteryapp.Models.CreateNotificationDialogFragment;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.OrganizerEntrantListAdapter;
import com.example.projecteventlotteryapp.R;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Set;


/**
 * Activity responsible for displaying a list of invited users for the current event.
 * Organizers can select users to send out notifications
 *
 */
public class OrganizerInvitedActivity extends AppCompatActivity implements CreateNotificationDialogFragment.NotificationListener {
    private String eventId;
    private String eventName;
    private EventUtils eventUtils;
    private OrganizerEntrantListAdapter adapter;
    private TextView invitedSize;
    private ListView invitedListView;
    private ImageButton backBtn;
    private Button selectBtn;
    private Button doneBtn;
    private Button sendNotifBtn;
    private Button uninviteBtn;
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

        invitedSize = findViewById(R.id.tv_organizer_invited_size);
        selectBtn = findViewById(R.id.btn_organizer_invited_select);
        doneBtn = findViewById(R.id.btn_done);
        floatingActionsContainer = findViewById(R.id.cl_floating_actions);
        backBtn = findViewById(R.id.btn_organizer_invited_back);
        backBtn.setOnClickListener(v -> finish());
        sendNotifBtn = findViewById(R.id.btn_send_notification);
        uninviteBtn = findViewById(R.id.btn_organizer_invited_uninvite);

        // hide notification buttons container initially
        floatingActionsContainer.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        DocumentReference eventDoc = db.collection("events").document(eventId);
        eventUtils = new EventUtils(db);

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

        uninviteBtn.setOnClickListener(v -> {
            Set<Integer> selected = adapter.getSelectedPositions();
            if (!selected.isEmpty()){
                new AlertDialog.Builder(this, R.style.DeleteGuard)
                        .setTitle("Uninvite Users")
                        .setMessage("Are you sure you want to uninvite the selected users?")
                        .setPositiveButton("Uninvite", ((dialog, which) -> {
                            ArrayList<String> selectedList = new ArrayList<>();
                            for (int i = 0; i < invitedList.size(); i++){
                                if (selected.contains(i)){
                                    selectedList.add(invitedList.get(i));
                                }
                            }

                            for (String userID : selectedList){
                                eventUtils.moveEntrantAcrossLists(eventId, userID, EntrantListType.DECLINED, EntrantListType.INVITED);
                                invitedList.remove(userID);
                            }

                            adapter.notifyDataSetChanged();
                            invitedSize.setText(String.valueOf(invitedList.size()));

                            // Clear selection after uninviting
                            isSelectionMode = false;
                            selectBtn.setText("Select");
                            floatingActionsContainer.setVisibility(View.GONE);
                            adapter.setSelectionMode(false);
                            adapter.clearSelection();
                        }))
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
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
            storeNotificationInFirestore(userId, recipientId, message, eventName, eventId, db);
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
