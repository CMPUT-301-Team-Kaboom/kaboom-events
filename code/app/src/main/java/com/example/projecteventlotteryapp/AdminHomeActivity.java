package com.example.projecteventlotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // locate buttons
        Button viewEntrants = findViewById(R.id.btn_view_entrants);
        Button viewEvents = findViewById(R.id.btn_view_events);
        Button viewOrganizers = findViewById(R.id.btn_view_organizers);
        Button viewImages = findViewById(R.id.btn_view_images);

        // set on click listeners

        viewEntrants.setOnClickListener(v -> {
            // navigate to entrants list activity
            startActivity(new Intent(AdminHomeActivity.this, AdminProfilesListActivity.class));
        });

        viewEvents.setOnClickListener(v -> {
            // navigate to events list activity
            startActivity(new Intent(AdminHomeActivity.this, AdminEventsActivity.class));
        });

        viewOrganizers.setOnClickListener(v -> {
            // navigate to organizers list activity
            startActivity(new Intent(AdminHomeActivity.this, AdminOrganizersListActivity.class));
        });

        viewImages.setOnClickListener(v -> {
            // navigate to view images activity
            startActivity(new Intent(AdminHomeActivity.this, AdminImagesActivity.class));
        });

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bn_events_list_menu);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(AdminHomeActivity.this, AdminActivity.class));
                return true;
            }
            return false;
        });
    }
}
