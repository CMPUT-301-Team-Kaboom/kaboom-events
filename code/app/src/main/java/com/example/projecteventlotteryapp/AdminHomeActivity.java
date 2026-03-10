package com.example.projecteventlotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_home_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // locate buttons
        Button viewEntrants = findViewById(R.id.btn_view_entrants);
        Button viewEvents = findViewById(R.id.btn_view_events);
        Button viewOrganizers = findViewById(R.id.btn_view_organizers);
        Button viewImages = findViewById(R.id.btn_view_images);

        // set on click listeners
        viewImages.setOnClickListener(v -> {
            // navigate to view images activity
            startActivity(new Intent(AdminHomeActivity.this, AdminImagesActivity.class));
        });

        viewEvents.setOnClickListener(v -> {
            // navigate to view events activity
            startActivity(new Intent(AdminHomeActivity.this, AdminEventsActivity.class));
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
