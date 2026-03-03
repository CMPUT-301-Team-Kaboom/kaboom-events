package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OrganizerEventDetailsActivity extends AppCompatActivity {

    private LinearLayout organizerController;
    private ConstraintLayout entrantController;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_event_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        organizerController = findViewById(R.id.ll_organizer_button_controls);
        entrantController = findViewById(R.id.cl_entrant_button_controls);

        // show only specific buttons for role
        if (user.getRole() == Role.ORGANIZER) {
            organizerController.setVisibility(View.VISIBLE);
            entrantController.setVisibility(View.GONE);
        } else {
            organizerController.setVisibility(View.GONE);
            entrantController.setVisibility(View.VISIBLE);
        }
    }

    private void configureUIForRole(User user) {
        if (user.getRole() == Role.ORGANIZER) {
            organizerController.setVisibility(View.VISIBLE);
            entrantController.setVisibility(View.GONE);

            Button waitlistButton = findViewById(R.id.btn_organizer_waitlist);
            waitlistButton.setOnClickListener(v -> openUserList("waitlist"));

            Button invitedButton = findViewById(R.id.btn_organizer_invited);
            invitedButton.setOnClickListener(v -> openUserList("invited"));

            Button enrolledButton = findViewById(R.id.btn_organizer_enrolled);
            enrolledButton.setOnClickListener(v -> openUserList("enrolled"));

            Button declinedButton = findViewById(R.id.btn_organizer_declined);
            declinedButton.setOnClickListener(v -> openUserList("declined"));
        } else if (user.getRole() == Role.ENTRANT) {
            Button entrantPrimaryButton = findViewById(R.id.btn_entrant_primary);
            Button entrantSecondaryButton = findViewById(R.id.btn_entrant_secondary);

            if (event.invitedListContains((Entrant) user)) {
                entrantPrimaryButton.setText("Enroll");
                entrantPrimaryButton.setOnClickListener(v -> event.addToEnrolledList(user));

                entrantSecondaryButton.setText("Decline");
                entrantSecondaryButton.setOnClickListener(v -> event.addToDeclineList(user));
            } else if (event.waitlistContains((Entrant) user)) {
                entrantPrimaryButton.setText("Remove Waitlist");
                entrantPrimaryButton.setOnClickListener(v -> event.removeFromWaitlist(user));
            } else {
                entrantPrimaryButton.setText("Join Waitlist");
                entrantPrimaryButton.setOnClickListener(v -> event.addToWaitlist(user));
            }
        }
    }

    private void openUserList(String tempVar) {
        // todo
    }
}

