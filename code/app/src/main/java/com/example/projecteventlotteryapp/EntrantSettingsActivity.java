package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
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

    private FirebaseFirestore db;
    private String deviceID;

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button btnSave;
    private ImageButton btnBack;
    private String collectionName; // This will hold "entrants", "organizers", or "admins"

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
        db = FirebaseFirestore.getInstance();

        // Get the device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Get the collection name from the intent. Default to "entrants" if none provided.
        collectionName = getIntent().getStringExtra("collectionName");
        if (collectionName == null) {
            collectionName = "entrants";
        }

        // connects the variables to their UI elements in the xml
        nameEditText = findViewById(R.id.et_name);
        emailEditText = findViewById(R.id.et_edit_email);
        phoneEditText = findViewById(R.id.et_edit_phone);
        btnSave = findViewById(R.id.btn_save_profile);
        btnBack = findViewById(R.id.btn_entrant_settings_back);

        //puts the saved data into the text hints
        loadProfileFromFirestore();

        //save when save button is clicked
        btnSave.setOnClickListener(v -> updateProfileInFirestore());

        //back when back button is clicked
        btnBack.setOnClickListener(v -> finish());
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
        //get profile from firestore
        DocumentReference userRef = db.collection(collectionName).document(deviceID);

        //check if profile exists
        userRef.get().addOnSuccessListener(documentSnapshot -> {
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
                Toast.makeText(this, "No profile found for collection: " + collectionName, Toast.LENGTH_SHORT).show();
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
        //update profile in firestore
       DocumentReference ref = db.collection(collectionName).document(deviceID);


       //create a map of the fields to update
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", phone);

        //update the profile
        ref.update(updates).addOnSuccessListener(unused ->
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->{
            Log.e("PROFILE", "Failed to update profile", e);
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        });

    }


}
