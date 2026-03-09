package com.example.projecteventlotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class OrganizerWaitlistActivity extends AppCompatActivity {
    private Event event;
    private OrganizerWaitlistAdapter adapter;
    private ListView waitlistView;
    private ArrayList<User> waitlist;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_waitlist);

        waitlistView = findViewById(R.id.lv_organizer_waitlist_list);
        Intent intent = getIntent();
        event = (Event) intent.getSerializableExtra("event");

        // TESTING
        event.addToEntrantList(EntrantListType.WAITLIST, new User(Role.ENTRANT, "TEST1", "Entrant1", "entrant1@email.com", ""));
        event.addToEntrantList(EntrantListType.WAITLIST, new User(Role.ENTRANT, "TEST2", "Entrant2", "entrant2@email.com", ""));
        event.addToEntrantList(EntrantListType.WAITLIST, new User(Role.ENTRANT, "TEST3", "Entrant3", "entrant3@email.com", ""));
        event.addToEntrantList(EntrantListType.WAITLIST, new User(Role.ENTRANT, "TEST4", "Entrant4", "entrant4@email.com", ""));

        if (event != null){
            waitlist = event.getList(EntrantListType.WAITLIST).getEntrants();
            adapter = new OrganizerWaitlistAdapter(this, waitlist);
            waitlistView.setAdapter(adapter);

            TextView waitlistSize = findViewById(R.id.tv_organizer_waitlist_size);
            waitlistSize.setText(waitlist.size() + "/" + event.getWaitlistLimit());
        }
    }
}
