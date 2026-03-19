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
import android.widget.Toast;

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
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.R;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
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
    private Button drawButton;
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
        drawButton = findViewById(R.id.btn_eventDetails_Draw);
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

            if (event.getRegistrationEndDate().isBefore(LocalDate.now())) {
                drawButton.setVisibility(View.VISIBLE);
            } else {
                drawButton.setVisibility(View.GONE);
            }

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

            drawButton.setOnClickListener(v -> {
                eventUtils.generateInvitationList(event.getEventId(), event.getAttendeesLimit())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Draw Complete", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("EventDetailsActivity", "Failed to generate invitationList. Error: " + e);
                            Toast.makeText(this, "Could not complete Draw", Toast.LENGTH_SHORT).show();
                        });
            });

            mapButton.setOnClickListener(v -> Log.d("EventDetails", "Clicked Map Button"));
        } else if (user.getRole() == Role.ENTRANT) {
            entrantController.setVisibility(View.VISIBLE);
            organizerController.setVisibility(View.GONE);
            entrantSecondaryButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
            drawButton.setVisibility(View.GONE);
            mapButton.setVisibility(View.GONE);

            setupEntrantButtonsByEnrollmentStatus(user);
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

    /**
     * Sets the entrantPrimaryButton to Remove Waitlist functionality
     * @param user the current user
     */
    private void showRemoveWaitlistButtonState(User user) {
        entrantPrimaryButton.setText("Remove Waitlist");
        entrantPrimaryButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        entrantPrimaryButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        entrantPrimaryButton.setOnClickListener(v -> {
            eventUtils.removeFromEntrantList(EntrantListType.WAITLIST, user, eventId)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EventDetails", "Successfully Left Waitlist");
                        showJoinWaitlistButtonState(user);
                    })
                    .addOnFailureListener(e -> {
                        Log.d("EventDetails", "Failed to join Waitlist - Error: " + e);
                        Toast.makeText(this, "Failed to join Waitlist", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    /**
     * Sets the entrantPrimaryButton to Join Waitlist functionality
     * @param user the current user
     */
    private void showJoinWaitlistButtonState(User user) {
        entrantPrimaryButton.setText("Join Waitlist");
        entrantPrimaryButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        entrantPrimaryButton.setTextColor(ContextCompat.getColor(this, R.color.secondaryBackground));
        entrantPrimaryButton.setOnClickListener(v -> {
            eventUtils.addToEntrantList(EntrantListType.WAITLIST, user, eventId)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EventDetails", "Successfully joined Waitlist");
                        showRemoveWaitlistButtonState(user);
                    })
                    .addOnFailureListener(e -> {
                        Log.d("EventDetails", "Failed to join Waitlist - Error: " + e);
                        Toast.makeText(this, "Failed to join Waitlist", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    /**
     * Sets the entrantPrimaryButton to Enroll in event functionality
     * @param user the current user
     */
    private void showEnrollButton(User user) {
        entrantPrimaryButton.setText("Enroll");
        entrantPrimaryButton.setBackgroundColor(ContextCompat.getColor(this, R.color.secondaryAccent));
        entrantPrimaryButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        entrantPrimaryButton.setShadowLayer(10,0,0, R.color.black);
        entrantPrimaryButton.setOnClickListener(v -> {
            Log.d("EventDetails", "Attempting to enroll user in event. EventId: " + event.getEventId());

            // move user from INVITED to ENROLLED
            eventUtils.moveEntrantAcrossLists(event.getEventId(), user.getUserId(), EntrantListType.ENROLLED, EntrantListType.INVITED)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EventDetails", "Successfully enrolled user in event");
                        showEnrolledDeclinedStatus(EntrantListType.ENROLLED);
                    })
                    .addOnFailureListener(e -> {
                        Log.d("EventDetails", "Failed to enrolled user in event. Error: " + e);
                        Toast.makeText(this, "Failed to enroll in event", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    /**
     * Sets the entrantSecondaryButton to Decline event functionality.
     * @param user the current user
     */
    private void showDeclineButton(User user) {
        entrantSecondaryButton.setText("Decline");
        entrantSecondaryButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        entrantSecondaryButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        entrantSecondaryButton.setVisibility(View.VISIBLE);
        entrantSecondaryButton.setOnClickListener(v -> {
            Log.d("EventDetails", "Attempting to decline event. EventId: " + event.getEventId());

            // move user from INVITED to DECLINED
            eventUtils.moveEntrantAcrossLists(event.getEventId(), user.getUserId(), EntrantListType.DECLINED, EntrantListType.INVITED)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EventDetails", "Successfully declined user in event");
                        showEnrolledDeclinedStatus(EntrantListType.DECLINED);
                    })
                    .addOnFailureListener(e -> {
                        Log.d("EventDetails", "Failed to decline user invitation for event. Error: " + e);
                        Toast.makeText(this, "Failed to decline event", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    /**
     * Deactivates the entrantPrimary/SecondaryButton and sets the text to the entrant's enrollment
     * status.
     *
     * <p>This function takes in an EntrantListType and based on its value, updates the
     * entrantPrimaryButton to display their enrollment status.</p>
     * @param status the EntrantListType that represents the entrants enrollment status. Must be DECLINED or ENROLLED
     */
    private void showEnrolledDeclinedStatus(EntrantListType status) {
        if (status != EntrantListType.DECLINED && status != EntrantListType.ENROLLED) {
            Log.d("showEnrolledDeclinedStatus", "Not supplied valid EntrantListType status. Expected: ENROLLED, DECLINED");
            return;
        }

        entrantSecondaryButton.setVisibility(View.GONE);
        entrantPrimaryButton.setOnClickListener(v -> {
            // do nothing
        });
        if (status == EntrantListType.ENROLLED) {
            entrantPrimaryButton.setText("Enrolled");
            entrantPrimaryButton.setBackgroundColor(ContextCompat.getColor(this, R.color.secondaryAccent));
            entrantPrimaryButton.setShadowLayer(10,0,0, R.color.black);
            entrantPrimaryButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            entrantPrimaryButton.setText("Declined");
            entrantPrimaryButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
            entrantPrimaryButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    /**
     * Configures entrant related buttons based on user's enrollment status for the current event.
     *
     * <p>THis method checks all of the entrant lists (invited, enrolled, declined, waitlist) to
     * determine which buttons to show. The checks are executed in parallel and once all results
     * are available, the correct UI is applied.
     * If any of the fetching fails, an error is logged and nothing happens.</p>
     * @param user the current user
     */
    private void setupEntrantButtonsByEnrollmentStatus(User user) {
        String eventId = event.getEventId();

        Tasks.whenAllSuccess(
                eventUtils.entrantListContains(EntrantListType.INVITED, user, eventId),
                eventUtils.entrantListContains(EntrantListType.ENROLLED, user, eventId),
                eventUtils.entrantListContains(EntrantListType.DECLINED, user, eventId),
                eventUtils.entrantListContains(EntrantListType.WAITLIST, user, eventId)
        ).addOnSuccessListener(results -> {
            boolean invited     = (Boolean) results.get(0);
            boolean enrolled    = (Boolean) results.get(1);
            boolean declined    = (Boolean) results.get(2);
            boolean waitlisted  = (Boolean) results.get(3);

            if (invited) {
                showEnrollButton(user);
                showDeclineButton(user);
            } else if (enrolled) {
                showEnrolledDeclinedStatus(EntrantListType.ENROLLED);
            } else if (declined) {
                showEnrolledDeclinedStatus(EntrantListType.DECLINED);
            } else if (waitlisted) {
                showRemoveWaitlistButtonState(user);
            } else {
                showJoinWaitlistButtonState(user);
            }
        }).addOnFailureListener(e -> {
            Log.e("setupEntrantButtonsByEnrollmentStatus", "Failed to determine entrant status", e);
        });
    }
}

