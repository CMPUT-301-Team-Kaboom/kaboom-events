package com.example.projecteventlotteryapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.projecteventlotteryapp.Enums.EntrantListType;
import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.MyApp;
import com.example.projecteventlotteryapp.R;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This class handles the logic and UI support for displaying Event details for both Entrants and Organizers
 *
 *
 */
public class EventDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String eventId;
    private Event event;
    private EventUtils eventUtils;
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
    private ImageView posterIV;

    /**
     * Entry point of the activity
     *
     * <p>This function is the entry point of the Activity. It sets up the db instance and UI for
     * the event.</p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
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
        eventUtils = new EventUtils(db);

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
                        event = eventUtils.fetchEventFromSnapshot(document);
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

    /**
     * Helps setup the UI of the activity by defining local variables.
     */
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
        posterIV                = findViewById(R.id.img_eventDetails_poster);

        organizerController = findViewById(R.id.ll_eventDetails_organizer_button_controls);
        entrantController   = findViewById(R.id.cl_eventDetails_entrant_button_controls);

        editButton = findViewById(R.id.btn_eventDetails_edit);
        backButton = findViewById(R.id.btn_eventDetails_back);
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Configures the UI for a given user depending on their role as well as firestore actions
     *
     * <p>If the user is an organizer, organizer-specific controls are displayed such as
     *  editing the event, viewing entrant lists, and accessing the map. Entrant controls
     *  are hidden.</p>
     *
     * <p>If the user is an entrant, entrant-specific controls are displayed and organizer
     *  controls are hidden. The method queries Firestore to determine the entrant's
     *  current status in the event (invited or waitlisted) and updates the available
     *  actions accordingly.</p>
     *
     *  TODO: simplify this function by creating sub functions and separating firestore actions
     *
     * @param user the User interacting with the app
     */
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
            invitedButton.setOnClickListener(v -> {
                Log.d("EventDetails", "[TEMP] Open invited list");
                Intent intent = new Intent(EventDetailsActivity.this, OrganizerInvitedActivity.class);
                intent.putExtra("eventID", eventId);
                startActivity(intent);
            });
            enrolledButton.setOnClickListener(v -> {
                Log.d("EventDetails", "[TEMP] Open enrolled List");
                Intent intent = new Intent(EventDetailsActivity.this, OrganizerEnrolledActivity.class);
                intent.putExtra("eventID", eventId);
                startActivity(intent);
            });
            declinedButton.setOnClickListener(v -> {
                Log.d("EventDetails", "[TEMP] Open declined list");
                Intent intent = new Intent(EventDetailsActivity.this, OrganizerDeclinedActivity.class);
                intent.putExtra("eventID", eventId);
                startActivity(intent);
            });

            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditEventActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            });
            mapButton.setOnClickListener(v -> Log.d("EventDetails", "Clicked Map Button"));
        } else if (user.getRole() == Role.ENTRANT) {
            entrantController.setVisibility(View.VISIBLE);
            organizerController.setVisibility(View.GONE);
            entrantSecondaryButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
            mapButton.setVisibility(View.GONE);

            eventUtils.entrantListContains(EntrantListType.INVITED, user, event.getEventId()).addOnSuccessListener(invited -> {
                if (invited) {
                    entrantPrimaryButton.setText("Enroll");
                    // TODO: Setup onclick listener for adding to Enrolled list
                    entrantPrimaryButton.setOnClickListener(v -> Log.d("EventDetails", "[TEMP] Enroll"));


                    entrantSecondaryButton.setVisibility(View.VISIBLE);
                    entrantSecondaryButton.setText("Decline");
                    // TODO: Setup onclick listener for adding to Declined list
                    entrantSecondaryButton.setOnClickListener(v -> Log.d("EventDetails", "[TEMP] Decline"));
                } else {
                    eventUtils.entrantListContains(EntrantListType.WAITLIST, user, eventId).addOnSuccessListener(waitlisted -> {
                        if (waitlisted){
                            entrantPrimaryButton.setText("Remove Waitlist");
                            entrantPrimaryButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
                            entrantPrimaryButton.setOnClickListener(v -> {
                                eventUtils.removeFromEntrantList(EntrantListType.WAITLIST, user, eventId)
                                        .addOnSuccessListener(aVoid -> {Log.d("EventDetails", "Successfully Left Waitlist");
                                            entrantPrimaryButton.setText("Join Waitlist");
                                            //TODO make this non recursive
                                            configureUIForRole(user);
                                        })
                                        .addOnFailureListener(e -> {Log.d("EventDetails", "Failed to join Waitlist");
                                        });
                            });
                        } else {
                            entrantPrimaryButton.setText("Join Waitlist");
                            entrantPrimaryButton.setBackgroundColor(ContextCompat.getColor(this, R.color.secondaryAccent));
                            entrantPrimaryButton.setOnClickListener(v -> {
                                eventUtils.addToEntrantList(EntrantListType.WAITLIST, user, eventId)
                                        .addOnSuccessListener(aVoid -> {Log.d("EventDetails", "Successfully joined Waitlist");
                                            entrantPrimaryButton.setText("Remove Waitlist");
                                            // TODO: make this non recursive
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

    /**
     * Helper function that updates the UI elements using the local event variable and fetches
     * the poster image associated with the event.
     */
    private void updateUi() {
        nameHeaderTextView.setText(event.getName());
        attendeesTV.setText(String.valueOf(event.getAttendeesLimit()));
        waitListTV.setText(String.valueOf(event.getWaitlistSize()));
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

        db.collection("events").document(eventId).get().addOnSuccessListener(doc->{
            if (doc.exists()){
                DocumentReference posterRef = doc.getDocumentReference("poster");

                if (posterRef != null) {
                    posterRef.get().addOnSuccessListener(posterDoc -> {
                        if (posterDoc.exists()){
                            Glide.with(this).load(posterDoc.getString("url")).into(posterIV);
                        } else {
                            Glide.with(this).load(R.drawable.default_poster).into(posterIV);
                        }
                    });
                } else {
                    Glide.with(this).load(R.drawable.default_poster).into(posterIV);
                }
            }
        });

        configureUIForRole(globalUser);
    }

    /**
     * Helper function that sets up the tags
     */
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

