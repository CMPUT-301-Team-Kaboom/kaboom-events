package com.example.projecteventlotteryapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.EventsListActivity;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceID;
    private EditText etName, etEmail, etPhone;
    private ToggleButton btnEntrant, btnOrganizer, btnAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Retrieve deviceID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Buttons
        Button btnSignUp = findViewById(R.id.btn_registration_signup);
        Button btnLogIn = findViewById(R.id.btn_registration_login);
        btnEntrant = findViewById(R.id.btn_registration_entrant);
        btnOrganizer = findViewById(R.id.btn_registration_organizer);
        btnAdmin = findViewById(R.id.btn_registration_admin);

        setupToggleLogic();

        // Initialize EditTexts
        etName = findViewById(R.id.et_registration_name);
        etEmail = findViewById(R.id.et_registration_email);
        etPhone = findViewById(R.id.et_registration_phone);


        // Sign Up Logic
        btnSignUp.setOnClickListener(v -> createNewUser());

        // Log In Logic
        btnLogIn.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LogInActivity.class));
        });
    }

    private void setupToggleLogic() {
        btnEntrant.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                btnOrganizer.setChecked(false);
                btnAdmin.setChecked(false);
            }
        });


        btnOrganizer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                btnEntrant.setChecked(false);
                btnAdmin.setChecked(false);
            }
        });


        btnAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                btnOrganizer.setChecked(false);
                btnEntrant.setChecked(false);
            }

        });
    }

    private Role getSelectedRole() {
        // Function to determine the selected role
        if (btnEntrant.isChecked()) return Role.ENTRANT;
        if (btnOrganizer.isChecked()) return Role.ORGANIZER;
        if (btnAdmin.isChecked()) return Role.ADMIN;
        return null;
    }

    private String getSelectedCollection(){
        if (btnEntrant.isChecked()){
            return "entrants";
        } else if (btnOrganizer.isChecked()){
            return "organizers";
        } else if (btnAdmin.isChecked()){
            return "admins";
        } else {
            return null;
        }
    }

    // Creates new user profile in Firestore
    private void createNewUser() {
        String collectionName = getSelectedCollection();
        if (collectionName == null) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user already exists before allowing Sign Up
        db.collection(collectionName).document(deviceID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    Toast.makeText(RegistrationActivity.this, "Account already exists. Please Log In instead.", Toast.LENGTH_LONG).show();
                } else {
                    // User doesn't exist in this role, proceed with creation
                    processUserCreation(collectionName);
                }
            } else {
                Log.e("AUTH", "Error checking existing user", task.getException());
                Toast.makeText(RegistrationActivity.this, "Connection failed. Check internet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processUserCreation(String collectionName) {
        DocumentReference userRef = db.collection(collectionName).document(deviceID);

        // Get text from EditTexts
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Check if fields are empty
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Data to store
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("deviceID", deviceID);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone", phone);

        // Add data to Firestore
        userRef.set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AUTH", "New profile Created in " + collectionName + ": " + deviceID);
                    
                    Role role = getSelectedRole();
                    // extract userId field from Firestore
                    User user = new User(role, deviceID, name, email, phone);

                    // set global MyApp user
                    MyApp app = (MyApp) getApplication();
                    app.setCurrentUser(user);

                    // Temporary handling of page routing, will add more cases for organizer and entrant for now
                    Intent intent;
                    if (role == Role.ADMIN) {
                        intent = new Intent(RegistrationActivity.this, AdminHomeActivity.class);
                    } else {
                        intent = new Intent(RegistrationActivity.this, EventsListActivity.class);
                    }
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("AUTH", "Error creating user", e);
                    Toast.makeText(this, "Failed to create profile", Toast.LENGTH_SHORT).show();
                });
    }
}
