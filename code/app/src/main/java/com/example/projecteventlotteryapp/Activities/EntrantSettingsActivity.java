package com.example.projecteventlotteryapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.R;
import com.example.projecteventlotteryapp.dbUtils.UserUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


/**
 * EntrantSettingsActivity
 *
 * allows an entrant to view and edit their profile information.
 *
 * How it works:
 * Retrieves the device's unique ANDROID_ID.
 * Uses that ID as the document key in the "entrants" Firestore collection.
 * Loads saved profile data (name, email, phone) into editable text fields.
 * Allows the user to modify their information.
 * Updates the corresponding Firestore document when the Save button is pressed.
 *
 * If no profile exists for the device, a message is displayed prompting
 * the user to create one.
 * @author anna
 */
public class EntrantSettingsActivity extends AppCompatActivity {

    private UserUtils db;
    private String deviceID;

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button btnSave;
    private Button btnDelete;
    private Switch swtchNotification;
    private User globalUser;

    /**
     * Entry point of the activity
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant);

        // Initialize Firebase Firestore
        db = new UserUtils(FirebaseFirestore.getInstance());

        // Get the device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        // connects the variables to their UI elements in the xml
        nameEditText = findViewById(R.id.et_name);
        emailEditText = findViewById(R.id.et_edit_email);
        phoneEditText = findViewById(R.id.et_edit_phone);
        btnSave = findViewById(R.id.btn_save_profile);
        btnDelete = findViewById(R.id.btn_delete_profile);
        swtchNotification = findViewById((R.id.s_switch));
        ImageButton btnBack = findViewById(R.id.btn_entrant_back);

        btnBack.setOnClickListener(v -> finish());

        MyApp app = (MyApp) getApplication();
        globalUser = app.getCurrentUser();

        //puts the saved data into the text hints
        loadProfileFromFirestore();

        //save when save button is clicked
        btnSave.setOnClickListener(v -> updateProfileInFirestore());
        btnDelete.setOnClickListener(v -> deleteProfileFromFirestore());

        // Load the current notification status from Firestore
        db.loadUserProfile(deviceID, globalUser.getRole()).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean notificationEnabled = documentSnapshot.getBoolean("notificationEnabled");

                // If the field doesn't exist yet
                if (notificationEnabled == null) {
                    Map<String, Object> initialUpdate = new HashMap<>();
                    initialUpdate.put("notificationEnabled", true); // set the field to true by default
                    db.updateUserProfile(deviceID, initialUpdate, globalUser.getRole()).addOnSuccessListener(unused -> {
                        // If default setting was successfully made, set the switch to true
                        swtchNotification.setChecked(true);
                    }).addOnFailureListener(e -> {
                        Log.e("PROFILE", "Failed to update notification status", e);
                        Toast.makeText(this, "Failed to update notification status", Toast.LENGTH_SHORT).show();
                        // Revert the switch visual state if the database update failed
                        swtchNotification.setChecked(false);
                    });
                } else {
                    swtchNotification.setChecked(notificationEnabled);
                }
            }

        });

        // Save the status immediately when the user toggles the switch
        swtchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Map<String, Object> update = new HashMap<>();
            update.put("notificationEnabled", isChecked);

            db.updateUserProfile(deviceID, update, globalUser.getRole())
                    .addOnSuccessListener(unused -> {
                        String status = isChecked ? "Notification enabled" : "Notification disabled";
                        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("PROFILE", "Failed to update notification status", e);
                        Toast.makeText(this, "Failed to update notification status", Toast.LENGTH_SHORT).show();
                        // Revert the switch visual state if the database update failed
                        swtchNotification.setChecked(!isChecked);
                    });


        });

    }




    /**
     * Loads the users profile from firestore.
     *retrieves the device's unique ANDROID_ID.
     * Uses that ID as the document key in the "entrants" Firestore collection.
     * fills in the text hints with the saved data.
     * If no profile exists for the device, a message is displayed prompting
     * the user to create one.
     */
    private void loadProfileFromFirestore() {
        //check if profile exists
        db.loadUserProfile(deviceID, globalUser.getRole()).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String phone = documentSnapshot.getString("phone");

                //if any of the fields are null, show toast message
                if (name != null) nameEditText.setText(name);
                if (email != null) emailEditText.setText(email);
                if (phone != null) phoneEditText.setText(phone);
            } else {
                //if profile doesn't exist, show toast message
                Toast.makeText(this, "No profile found. Please create a profile", Toast.LENGTH_SHORT).show();
            }
            //if profile doesn't exist, show toast message
        }).addOnFailureListener(e -> {
            Log.e("PROFILE", "Failed to load profile", e);
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Updates the users profile in firestore.
     * Gets the values from the edit text fields.
     * Checks if any of the required fields are empty (phone is not a required field).
     * Updates the corresponding Firestore document when the Save button is pressed.
     */
    private void updateProfileInFirestore() {
        //get values from edit text
       String name = nameEditText.getText().toString().trim();
       String email = emailEditText.getText().toString().trim();
       String phone = phoneEditText.getText().toString().trim();

       //check if any of the required fields are empty (phone is not a required field)
       if (name.isEmpty() || email.isEmpty()) {
           Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
           return;
       }

       //create a map of the fields to update
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", phone);

        //update the profile
        db.updateUserProfile(deviceID, updates, globalUser.getRole()).addOnSuccessListener(unused ->
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->{
            Log.e("PROFILE", "Failed to update profile", e);
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        });

    }

    private void deleteProfileFromFirestore() {
        // TODO: Delete profile from everywhere in database (inside waitlists etc.)

        db.deleteUserProfile(deviceID, globalUser.getRole()).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();

            //clear text fields
            nameEditText.setText("");
            emailEditText.setText("");
            phoneEditText.setText("");

            //close activity and return to the registration screen
            Intent intent = new Intent(this, RegistrationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Log.e("PROFILE", "Failed to delete profile", e);
            Toast.makeText(this, "Failed to delete profile", Toast.LENGTH_SHORT).show();
        });
    }

}
