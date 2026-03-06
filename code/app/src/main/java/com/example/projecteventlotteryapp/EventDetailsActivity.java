package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.time.format.DateTimeFormatter;

public class EventDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String eventId;
    private Event event;

    //=============================
    // UI Elements
    //=============================

    // organizer Buttons
    private Button waitlistButton;
    private Button invitedButton;
    private Button enrolledButton;
    private Button declinedButton;
    private Button editButton;
    private Button mapButton;

    // entrant buttons
    private Button entrantPrimaryButton;
    private Button entrantSecondaryButton;

    // global UI elements
    private ImageButton backButton;
    private LinearLayout organizerController;
    private ConstraintLayout entrantController;

    private TextView nameHeaderTextView;
    private TextView organizerHeaderTextview;
    private TextView drawDateTV;
    private TextView registrationPeriodTV;
    private TextView attendeesTV;
    private TextView waitListTV;

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
        setupUi();

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


    }

    private void setupUi() {
        waitlistButton  = findViewById(R.id.btn_eventDetails_organizer_waitlist);
        invitedButton   = findViewById(R.id.btn_eventDetails_organizer_invited);
        enrolledButton  = findViewById(R.id.btn_eventDetails_organizer_enrolled);
        declinedButton  = findViewById(R.id.btn_eventDetails_organizer_declined);
        mapButton       = findViewById(R.id.btn_eventDetails_map);

        entrantPrimaryButton    = findViewById(R.id.btn_eventDetails_entrant_primary);
        entrantSecondaryButton  = findViewById(R.id.btn_eventDetails_entrant_secondary);


        nameHeaderTextView      = findViewById(R.id.tv_eventDetails_event_name_header);
        organizerHeaderTextview = findViewById(R.id.tv_eventDetails_org_header);
        drawDateTV              = findViewById(R.id.tv_eventDetails_draw_date);
        registrationPeriodTV    = findViewById(R.id.tv_eventDetails_registration_period);
        attendeesTV             = findViewById(R.id.tv_eventDetails_attendees);
        waitListTV              = findViewById(R.id.tv_eventDetails_waitlist_count);

        organizerController = findViewById(R.id.ll_eventDetails_organizer_button_controls);
        entrantController   = findViewById(R.id.cl_eventDetails_entrant_button_controls);

        editButton = findViewById(R.id.btn_eventDetails_edit);
        backButton = findViewById(R.id.btn_eventDetails_back);
        backButton.setOnClickListener(v -> finish());
    }

    private void configureUIForRole(User user) {
        if (user.getRole() == Role.ORGANIZER) {
            organizerController.setVisibility(View.VISIBLE);
            entrantController.setVisibility(View.GONE);

//            waitlistButton.setOnClickListener(v -> openUserList("waitlist"));

//            invitedButton.setOnClickListener(v -> openUserList("invited"));

//            enrolledButton.setOnClickListener(v -> openUserList("enrolled"));

//            declinedButton.setOnClickListener(v -> openUserList("declined"));
        } else if (user.getRole() == Role.ENTRANT) {
            organizerController.setVisibility(View.GONE);
            entrantController.setVisibility(View.VISIBLE);
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
        nameHeaderTextView.setText(event.getName());
        attendeesTV.setText(String.valueOf(event.getAttendeesLimit()));
        waitListTV.setText(String.valueOf(event.getWaitlistLimit()));

        DateTimeFormatter drawDatePattern = DateTimeFormatter.ofPattern("MMM d      h:mm a");
        String formattedDate = event.getDrawDate().format(drawDatePattern).toUpperCase();
        drawDateTV.setText(formattedDate);

        DateTimeFormatter registrationPeriodPattern = DateTimeFormatter.ofPattern("MMM d");
        String registrationPeriodText = String.format("%s - %s",
                event.getRegistrationStartDate().format(registrationPeriodPattern).toUpperCase(),
                event.getRegistrationEndDate().format(registrationPeriodPattern).toUpperCase()
        );
        registrationPeriodTV.setText(registrationPeriodText);


        // temp user
        User user = new Entrant("Tester", "tester", "100", Role.ENTRANT);
        // show only specific buttons for role
        configureUIForRole(user);
    }
}

