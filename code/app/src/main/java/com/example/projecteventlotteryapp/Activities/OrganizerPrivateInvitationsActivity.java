package com.example.projecteventlotteryapp.Activities;

import static com.example.projecteventlotteryapp.dbUtils.FirestoreUtils.storeNotificationInFirestore;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.CreateNotificationDialogFragment;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.PrivateInviteEntrantArrayAdapter;
import com.example.projecteventlotteryapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles the logic and UI support for inviting entrants to private events.
 *
 */
public class OrganizerPrivateInvitationsActivity extends BaseActivity implements CreateNotificationDialogFragment.NotificationListener {
    private ToggleButton nameToggleButton, emailToggleButton, phoneToggleButton;

    private FirebaseFirestore db;
    private ListView entrantsListView;
    private ArrayList<User> entrantsArrayList;
    private PrivateInviteEntrantArrayAdapter entrantsArrayAdapter;

    private User selectedEntrant;
    private String eventId, eventName;

    /**
     * Entry point of the activity.
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
        setContentView(R.layout.activity_organizer_private_invitations);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.private_invitations_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // setup db
        db = FirebaseFirestore.getInstance();

        Intent intent  = getIntent();
        eventId = intent.getStringExtra("eventId");
        eventName = intent.getStringExtra("eventName");

        // setup ListView and ArrayAdapter
        entrantsListView = findViewById(R.id.lv_entrants_list);
        entrantsArrayList = new ArrayList<>();
        entrantsArrayAdapter = new PrivateInviteEntrantArrayAdapter(this, entrantsArrayList, entrant -> {
            selectedEntrant = entrant;
            new AlertDialog.Builder(this, R.style.DeleteGuard)
                    .setTitle("Invite Entrant")
                    .setMessage("Are you sure you want to invite this entrant?")
                    .setPositiveButton("Invite", ((dialog, which) -> {
                        CreateNotificationDialogFragment.newInstance().show(getSupportFragmentManager(), "send_private_invite");
                        Log.d("OrganizerPrivateInvitationsActivity", "invited entrant");
                    }))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        entrantsListView.setAdapter(entrantsArrayAdapter);


        // references to EditText and Buttons
        ImageButton backButton = findViewById(R.id.ibtn_private_invitation_back);
        EditText entrantSearch = findViewById(R.id.et_search_entrant);
        Button confirmButton = findViewById(R.id.btn_search_confirm);
        Button clearFiltersButton = findViewById(R.id.btn_search_clear);
        nameToggleButton = findViewById(R.id.tbtn_search_name);
        emailToggleButton = findViewById(R.id.tbtn_search_email);
        phoneToggleButton = findViewById(R.id.tbtn_search_phone);
        List<ToggleButton> toggleButtons = List.of(
                nameToggleButton,
                emailToggleButton,
                phoneToggleButton
        );
        setupToggleButtons(toggleButtons);

        // back button logic
        backButton.setOnClickListener(v -> finish());

        // confirm button logic
        confirmButton.setOnClickListener(v -> {
            String searchType = getToggleStatus();

            if (searchType == null) {
                Toast.makeText(OrganizerPrivateInvitationsActivity.this, "Please choose a search type (Name, Email, Phone).", Toast.LENGTH_SHORT).show();
            } else {
                searchEntrants(searchType, entrantSearch.getText().toString().toLowerCase().trim());
            }
        });

        // clear filters button logic
        clearFiltersButton.setOnClickListener(v -> {
            // reset search
            nameToggleButton.setChecked(false);
            emailToggleButton.setChecked(false);
            phoneToggleButton.setChecked(false);
            entrantSearch.setText("");

            // clear logic (back to all entrants)
            getAllEntrants();
        });

        getAllEntrants();
    }

    /**
     * This method is called when the organizer sends the notification after typing the message.
     *
     * @param message entered by the organizer
     */
    @Override
    public void onSendNotification(String message) {
        if (selectedEntrant == null) {
            Toast.makeText(this, "Cannot send an invite.", Toast.LENGTH_SHORT).show();
            return;
        }

        // get sender ID
        String organizerId = ((MyApp) getApplication()).getCurrentUser().getUserId();

        // get recipient ID
        String recipientId = selectedEntrant.getUserId();

        // store the notification
        storeNotificationInFirestore(organizerId, recipientId, message, eventName, eventId, db);

        // Show a confirmation message
        Toast.makeText(this, "Invite sent to " + selectedEntrant.getName(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Gets all entrants from the database and updates the ListView.
     */
    private void getAllEntrants() {
        db.collection("entrants").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                entrantsArrayList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    User entrant = new User(Role.ENTRANT, doc.getId(),
                            doc.getString("name"),
                            doc.getString("email"),
                            doc.getString("phone"));
                    Log.d("OrganizerPrivateInvitationsActivity", "User: " + entrant.getName());
                    entrantsArrayList.add(entrant);
                }
                entrantsArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Searches for entrants in the database based on the selected search type and input.
     *
     * @param searchType to perform ("name", "email", or "phone")
     * @param search query entered by the user
     */
    private void searchEntrants(String searchType, String search) {
        // get entrants
        db.collection("entrants").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                entrantsArrayList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    User entrant = new User(Role.ENTRANT, doc.getId(),
                            doc.getString("name"),
                            doc.getString("email"),
                            doc.getString("phone"));

                    // filter
                    if (searchType.equals("name") && entrant.getName() != null) {
                        String entrantName = entrant.getName().toLowerCase().trim();
                        Log.d("OrganizerPrivateInvitationsActivity", "Entrant: " + entrantName);

                        // check for exact match or partial match
                        if (entrantName.equalsIgnoreCase(search) || entrantName.contains(search)) {
                            entrantsArrayList.add(entrant);
                        }
                    } else if (searchType.equals("email") && entrant.getEmail() != null) {
                        String entrantEmail = entrant.getEmail().toLowerCase().trim();
                        Log.d("OrganizerPrivateInvitationsActivity", "Entrant: " + entrantEmail);

                        // check for exact match or partial match
                        if (entrantEmail.equalsIgnoreCase(search) || entrantEmail.contains(search)) {
                            entrantsArrayList.add(entrant);
                        }
                    } else if (searchType.equals("phone") && entrant.getPhoneNumber() != null) {
                        String entrantPhone = entrant.getPhoneNumber().toLowerCase().trim();
                        Log.d("OrganizerPrivateInvitationsActivity", "Entrant: " + entrantPhone);

                        // check for exact match or partial match
                        if (entrantPhone.equalsIgnoreCase(search) || entrantPhone.contains(search)) {
                            entrantsArrayList.add(entrant);
                        }
                    }
                }
                entrantsArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Gets the search type chosen from the toggle buttons.
     *
     * @return search type as a String ("name", "email", "phone"), or null if none selected
     */
    private String getToggleStatus() {
        Map<ToggleButton, String> toggleMap = new HashMap<>();
        toggleMap.put(nameToggleButton, "name");
        toggleMap.put(emailToggleButton, "email");
        toggleMap.put(phoneToggleButton, "phone");

        for (Map.Entry<ToggleButton, String> entry : toggleMap.entrySet()) {
            if (entry.getKey().isChecked()) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Sets up the toggle buttons so only one can be checked at a time.
     *
     * @param buttons a list of toggle buttons
     */
    private void setupToggleButtons(List<ToggleButton> buttons) {
        for (ToggleButton tb : buttons) {
            tb.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                if (!isChecked) {
                    return;
                }

                for (ToggleButton other : buttons) {
                    if (other != buttonView) {
                        other.setChecked(false);
                    }
                }
            }));
        }
    }
}