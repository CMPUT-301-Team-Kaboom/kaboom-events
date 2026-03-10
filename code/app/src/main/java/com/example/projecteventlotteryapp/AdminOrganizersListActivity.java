package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AdminOrganizersListActivity extends AppCompatActivity {
    private ListView organizerListView;
    private OrganizerArrayAdapter organizerAdapter;
    private ArrayList<User> organizerDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_organizers_list);

        // initialize ui components
        TextView header = findViewById(R.id.tv_organizer_list_header);
        header.setText("All Organizers");

        ImageButton backButton = findViewById(R.id.btn_organizer_list_back);
        backButton.setOnClickListener(v -> finish());

        organizerListView = findViewById(R.id.lv_organizer_list);
        organizerDataList = new ArrayList<>();

        // initialize adapter
        organizerAdapter = new OrganizerArrayAdapter(this, organizerDataList);
        organizerListView.setAdapter(organizerAdapter);

        // todo: get organizer data from firestore
        // loadOrganizers();
    }
}
