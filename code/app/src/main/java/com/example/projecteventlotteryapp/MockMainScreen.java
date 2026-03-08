package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Mock screen just to test Event Creation Fragment functionality
 *
 * TODO: remove
 */
public class MockMainScreen extends AppCompatActivity implements CreateEventDialogFragment.CreateEventDialogListener {
    private Button addEventButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mock_main_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addEventButton = findViewById(R.id.mock_button);

        addEventButton.setOnClickListener(view -> {
            CreateEventDialogFragment createEventDialogFragment = new CreateEventDialogFragment();
            createEventDialogFragment.show(getSupportFragmentManager(), "Create Event");
        });
    }

    @Override
    public void addEvent(Event event) {
        Log.d("Mock Activity", "Event Details: " +
                "\nName: " + event.getName() +
                "\nDraw date: " + event.getDrawDate().toString() +
                "\nReg Start: " + event.getRegistrationStartDate().toString() +
                "\nReg End: " + event.getRegistrationEndDate().toString() +
                "\nEntrants Limit: " + event.getAttendeesLimit()
        );
    }
}