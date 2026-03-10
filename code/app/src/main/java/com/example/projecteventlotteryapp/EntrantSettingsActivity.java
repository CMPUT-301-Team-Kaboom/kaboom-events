package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


//this is our edit profile page it:
// finds user using the device ID and loads their saved profile from Firestore into the
// text hints. They are allowed to edit these fields and when they hit save it will update the
// firestore document
public class EntrantSettingsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceID;

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button btnSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Get the device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // connects the variables to their UI elements in the xml
        nameEditText = findViewById(R.id.et_name);
        emailEditText = findViewById(R.id.et_edit_email);
        phoneEditText = findViewById(R.id.et_edit_phone);
        btnSave = findViewById(R.id.btn_save_profile);

        //puts the saved data into the text hints
        loadProfileFromFirestore();

        //save when save button is clicked
        btnSave.setOnClickListener(v -> updateProfileInFirestore());
    }

    // this function reads from the firestore and loads the data into the text hints
    private void loadProfileFromFirestore() {
        //get profile from firestore
        DocumentReference userRef = db.collection("entrants").document(deviceID);

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
                Toast.makeText(this, "No profile found. Please create a profile", Toast.LENGTH_SHORT).show();
            }
            //if profile doesn't exist, show toast message
        }).addOnFailureListener(e -> {
            Log.e("PROFILE", "Failed to load profile", e);
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
        });
    }
    //this function updates the firestore with new values from the text hints
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
       DocumentReference ref = db.collection("entrants").document(deviceID);


       //create a map of the fields to update
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phoneNumber", phone);
        updates.put("deviceId", deviceID);

        //update the profile
        ref.set(updates).addOnSuccessListener(unused ->
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->{
            Log.e("PROFILE", "Failed to update profile", e);
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        });

    }


}
