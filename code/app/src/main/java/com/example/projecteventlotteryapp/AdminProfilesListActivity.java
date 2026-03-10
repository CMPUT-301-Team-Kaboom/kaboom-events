package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AdminProfilesListActivity extends AppCompatActivity {
    private ListView profileListView;
    private ProfileArrayAdapter profileAdapter;
    private ArrayList<User> profileDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profiles_list);

        // initialize ui components
        TextView header = findViewById(R.id.tv_entrant_list_header);
        header.setText("All Profiles");

        ImageButton backButton = findViewById(R.id.btn_entrant_list_back);
        backButton.setOnClickListener(v -> finish());

        profileListView = findViewById(R.id.lv_entrant_list);
        profileDataList = new ArrayList<>();

        // initialize adapter
        profileAdapter = new ProfileArrayAdapter(this, profileDataList);
        profileListView.setAdapter(profileAdapter);

        // todo: get profile data from firestore
        // loadProfiles();
    }
}
