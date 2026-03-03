package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout organizerController;
    private ConstraintLayout entrantController;
    private String eventId;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        /*  Code adapted from https://firebase.google.com/docs/firestore/query-data/get-data#java */
        DocumentReference docRef = db.collection("events").document(eventId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("EventActivity", "DocumentSnapshot data: " + document.getData());
                        event = Event.fetchEventFromSnapshot(document);
                        updateUi();
                    } else {
                        Log.d("EventActivity", "No such document");
                    }
                } else {
                    Log.d("EventActivity", "get failed with ", task.getException());
                }
            }
        });

        organizerController = findViewById(R.id.ll_organizer_button_controls);
        entrantController = findViewById(R.id.cl_entrant_button_controls);


        // temp user
        User user = new Entrant("Tester", "tester", "100", Role.ENTRANT);
        // show only specific buttons for role
        configureUIForRole(user);
    }

    private void configureUIForRole(User user) {
        if (user.getRole() == Role.ORGANIZER) {
            organizerController.setVisibility(View.VISIBLE);
            entrantController.setVisibility(View.GONE);

            Button waitlistButton = findViewById(R.id.btn_organizer_waitlist);
//            waitlistButton.setOnClickListener(v -> openUserList("waitlist"));

            Button invitedButton = findViewById(R.id.btn_organizer_invited);
//            invitedButton.setOnClickListener(v -> openUserList("invited"));

            Button enrolledButton = findViewById(R.id.btn_organizer_enrolled);
//            enrolledButton.setOnClickListener(v -> openUserList("enrolled"));

            Button declinedButton = findViewById(R.id.btn_organizer_declined);
//            declinedButton.setOnClickListener(v -> openUserList("declined"));
        } else if (user.getRole() == Role.ENTRANT) {
            organizerController.setVisibility(View.GONE);
            entrantController.setVisibility(View.VISIBLE);
            Button entrantPrimaryButton = findViewById(R.id.btn_entrant_primary);
            Button entrantSecondaryButton = findViewById(R.id.btn_entrant_secondary);
            entrantSecondaryButton.setVisibility(View.GONE);

            if (event.invitedListContains((Entrant) user)) {
                entrantPrimaryButton.setText("Enroll");
//                entrantPrimaryButton.setOnClickListener(v -> event.addToEnrolledList(user));

                entrantSecondaryButton.setVisibility(View.VISIBLE);
                entrantSecondaryButton.setText("Decline");
//                entrantSecondaryButton.setOnClickListener(v -> event.addToDeclineList(user));
            } else if (event.waitlistContains((Entrant) user)) {
                entrantPrimaryButton.setText("Remove Waitlist");
                //                entrantPrimaryButton.setOnClickListener(v -> event.removeFromWaitlist(user));
            } else {
                entrantPrimaryButton.setText("Join Waitlist");
//                entrantPrimaryButton.setOnClickListener(v -> event.addToWaitlist(user));
            }
        }
    }

    private void updateUi() {
        TextView nameHeaderTextView = findViewById(R.id.tv_organizer_details_event_name_header);
        TextView organizerHeaderTextview = findViewById(R.id.tv_organizer_details_org_header);
        TextView drawDateTV = findViewById(R.id.tv_organizer_draw_date);
        TextView registrationPeriodTV = findViewById(R.id.tv_organizer_registration_period);
        TextView attendeesTV = findViewById(R.id.tv_organizer_attendees);
        TextView waitListTV = findViewById(R.id.tv_organizer_waitlist_count);

        nameHeaderTextView.setText(event.getName());
    }
//
//    private void openUserList(String tempVar) {
//        // todo
//    }
}

