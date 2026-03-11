package com.example.projecteventlotteryapp;

import android.content.Intent;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String eventId;
    private Event event;
    private User globalUser;
    private DocumentReference eventDoc;

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
    private TextView descriptionTV;

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
        eventDoc = db.collection("events").document(this.eventId);
        setupUi();

        MyApp app = (MyApp) getApplication();
        globalUser = app.getCurrentUser();

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

    private String getListField(EntrantListType type) {
        switch (type) {
            case WAITLIST:
                return "waitlist";
            case INVITED:
                return "invited";
            case DECLINED:
                return "declined";
            case ENROLLED:
                return "enrolled";
            default:
                throw new IllegalArgumentException("Unknown list type: " + type);
        }
    }

    private Task<Void> addToEntrantList(EntrantListType listType, User entrant) {
        Log.d("AddToEntrantList", String.format("Type: %s | userId: %s",
                listType.toString(),
                entrant.getUserId())
        );

        return eventDoc.update(getListField(listType), FieldValue.arrayUnion(entrant.getUserId()));
    }

    public Task<Void> removeFromEntrantList(EntrantListType listType, User entrant) {
        return eventDoc.get().continueWithTask(task -> {
            //if we cannot find the event
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            
            DocumentSnapshot doc = task.getResult();
            if (doc == null || !doc.exists()) {
                throw new Exception("Document does not exist");
            }

            ArrayList<String> entrantList =
                    (ArrayList<String>) doc.get(getListField(listType));
            if (entrantList == null) {
                entrantList = new ArrayList<>();
            }

            entrantList.remove(entrant.getUserId());
            return eventDoc.update(getListField(listType), entrantList);
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
        descriptionTV           = findViewById(R.id.tv_eventDetails_description);

        organizerController = findViewById(R.id.ll_eventDetails_organizer_button_controls);
        entrantController   = findViewById(R.id.cl_eventDetails_entrant_button_controls);

        editButton = findViewById(R.id.btn_eventDetails_edit);
        backButton = findViewById(R.id.btn_eventDetails_back);
        backButton.setOnClickListener(v -> finish());
    }

    private void configureUIForRole(User user) {
        if (user.getRole() == Role.ORGANIZER) {
            entrantController.setVisibility(View.GONE);
            organizerController.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
            mapButton.setVisibility(View.VISIBLE);

            // TODO: set onClickListeners for Organizer specific buttons
            waitlistButton.setOnClickListener(v -> {
                Log.d("EventDetails", "[TEMP] Open waitlist");
                Intent intent = new Intent(EventDetailsActivity.this, OrganizerWaitlistActivity.class);
                intent.putExtra("eventID", eventId);
                startActivity(intent);
            });
            invitedButton.setOnClickListener(v -> Log.d("EventDetails", "[TEMP] Open invited list"));
            enrolledButton.setOnClickListener(v -> Log.d("EventDetails", "[TEMP] Open enrolled List"));
            declinedButton.setOnClickListener(v -> Log.d("EventDetails", "[TEMP] Open declined list"));

            editButton.setOnClickListener(v -> Log.d("EventDetails", "Clicked Edit Button"));
            mapButton.setOnClickListener(v -> Log.d("EventDetails", "Clicked Map Button"));
        } else if (user.getRole() == Role.ENTRANT) {
            entrantController.setVisibility(View.VISIBLE);
            organizerController.setVisibility(View.GONE);
            entrantSecondaryButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
            mapButton.setVisibility(View.GONE);

            event.entrantListContains(EntrantListType.INVITED, user).addOnSuccessListener(invited -> {
                if (invited) {
                    entrantPrimaryButton.setText("Enroll");
                    // TODO: Setup onclick listener for adding to Enrolled list
                    entrantPrimaryButton.setOnClickListener(v -> Log.d("EventDetails", "[TEMP] Enroll"));


                    entrantSecondaryButton.setVisibility(View.VISIBLE);
                    entrantSecondaryButton.setText("Decline");
                    // TODO: Setup onclick listener for adding to Declined list
                    entrantSecondaryButton.setOnClickListener(v -> Log.d("EventDetails", "[TEMP] Decline"));
                } else {
                    event.entrantListContains(EntrantListType.WAITLIST, user).addOnSuccessListener(waitlisted -> {
                        if (waitlisted){
                            entrantPrimaryButton.setText("Remove Waitlist");
                            // TODO: Setup onclick listener for removing from wait list
                            entrantPrimaryButton.setOnClickListener(v -> {
                                removeFromEntrantList(EntrantListType.WAITLIST, user)
                                        .addOnSuccessListener(aVoid -> {Log.d("EventDetails", "Successfully Left Waitlist");
                                            entrantPrimaryButton.setText("Join Waitlist");
                                            configureUIForRole(user);
                                        })
                                        .addOnFailureListener(e -> {Log.d("EventDetails", "Failed to join Waitlist");
                                        });
                            });
                        } else {
                            entrantPrimaryButton.setText("Join Waitlist");
                            // TODO: Setup onclick listener for adding to Waitlist list
                            entrantPrimaryButton.setOnClickListener(v -> {
                                addToEntrantList(EntrantListType.WAITLIST, user)
                                        .addOnSuccessListener(aVoid -> {Log.d("EventDetails", "Successfully joined Waitlist");
                                        entrantPrimaryButton.setText("Remove Waitlist");
                                        configureUIForRole(user);
                                        })
                                        .addOnFailureListener(e -> {Log.d("EventDetails", "Failed to join Waitlist");
                                        });
                            });
                        }
                    });
                }
            });
        }
    }

    private void updateUi() {
        nameHeaderTextView.setText(event.getName());
        attendeesTV.setText(String.valueOf(event.getAttendeesLimit()));
        waitListTV.setText(String.valueOf(event.getWaitlistLimit()));
        descriptionTV.setText(event.getDescription());
        setupTags();

        DateTimeFormatter drawDatePattern = DateTimeFormatter.ofPattern("MMM d      h:mm a");
        String formattedDate = event.getDrawDate().format(drawDatePattern).toUpperCase();
        drawDateTV.setText(formattedDate);

        DateTimeFormatter registrationPeriodPattern = DateTimeFormatter.ofPattern("MMM d");
        String registrationPeriodText = String.format("%s - %s",
                event.getRegistrationStartDate().format(registrationPeriodPattern).toUpperCase(),
                event.getRegistrationEndDate().format(registrationPeriodPattern).toUpperCase()
        );
        registrationPeriodTV.setText(registrationPeriodText);

        configureUIForRole(globalUser);
    }

    private void setupTags() {
        ArrayList<String> tags = event.getTagsList();
        int numTags = tags.size();

        TextView tag1 = findViewById(R.id.tv_eventDetails_tag1);
        TextView tag2 = findViewById(R.id.tv_eventDetails_tag2);
        TextView tag3 = findViewById(R.id.tv_eventDetails_tag3);
        List<TextView> tagsTVs = Arrays.asList(tag1, tag2, tag3);

        for (TextView tv:tagsTVs) {
            tv.setVisibility(View.GONE);
        }

        if (numTags > 3) {
            Log.d("EventDetailsActivity", "Number of tags exceeds limit (3). Truncating to first 3 tags.");
            numTags = 3;
        }

        for (int i=0; i < numTags; i++) {
            TextView tagTv = tagsTVs.get(i);
            tagTv.setText(tags.get(i));
            tagTv.setVisibility(View.VISIBLE);
        }
    }
}

