package com.example.projecteventlotteryapp;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminProfilesListActivity extends AppCompatActivity {
    private ListView profileListView;
    private ProfileArrayAdapter profileAdapter;
    private ArrayList<User> profileDataList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profiles_list);

        db = FirebaseFirestore.getInstance();

        ImageButton backButton = findViewById(R.id.btn_entrant_list_back);
        // TODO: connect back arrow with admin home
        backButton.setOnClickListener(v -> finish());

        profileListView = findViewById(R.id.lv_entrant_list);
        profileDataList = new ArrayList<>();

        profileAdapter = new ProfileArrayAdapter(this, profileDataList, user -> {
            db.collection("entrants").document(user.getUserId()).delete();
            profileDataList.remove(user);
            profileAdapter.notifyDataSetChanged();
        });
        profileListView.setAdapter(profileAdapter);

        loadProfiles();
    }

    private void loadProfiles() {
        db.collection("entrants").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                profileDataList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    User user = new User(Role.ENTRANT, doc.getId(), 
                            doc.getString("name"), 
                            doc.getString("email"), 
                            doc.getString("phone"));
                    profileDataList.add(user);
                }
                profileAdapter.notifyDataSetChanged();
            }
        });
    }
}
