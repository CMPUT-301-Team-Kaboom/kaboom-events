package com.example.projecteventlotteryapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class LogInActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String deviceID;
    private ToggleButton btnEntrant, btnOrganizer, btnAdmin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        db = FirebaseFirestore.getInstance();

        // FIX: Retrieve deviceID so it is not null
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        ImageButton btnBack = findViewById(R.id.BackButton);
        Button btnLogIn = findViewById(R.id.btn_registration_login);
        btnEntrant = findViewById(R.id.btn_registration_entrant);
        btnOrganizer = findViewById(R.id.btn_registration_organizer);
        btnAdmin = findViewById(R.id.btn_registration_admin);

        setupToggleLogic();

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(LogInActivity.this, RegistrationActivity.class));
            finish();
        });

        btnLogIn.setOnClickListener(v -> checkExistingUser());
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

    private String getSelectedCollection() {
        if (btnEntrant.isChecked()) return "entrants";
        if (btnOrganizer.isChecked()) return "organizers";
        if (btnAdmin.isChecked()) return "admins";
        return null;
    }

    private void checkExistingUser() {
        String collectionName = getSelectedCollection();
        if (collectionName == null) {
            Toast.makeText(this, "Please select your role to Log In", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(collectionName).document(deviceID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Log.d("AUTH", "User found in " + collectionName + ": " + deviceID);
                        startActivity(new Intent(LogInActivity.this, EventsListActivity.class));
                        finish();
                    } else if (task.isSuccessful()) {
                        Log.d("AUTH", "User does not exist in " + collectionName + ": " + deviceID);
                        Toast.makeText(this, "User does not exist for this role. Please sign up.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("AUTH", "Error checking user", task.getException());
                        Toast.makeText(this, "Login failed. Check connection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
