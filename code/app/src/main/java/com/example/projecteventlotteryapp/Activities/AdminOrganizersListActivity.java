package com.example.projecteventlotteryapp.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.OrganizerArrayAdapter;
import com.example.projecteventlotteryapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Activity for displaying a list of organizers.
 * This activity allows administrators to view and manage organizers.
 * It fetches organizer data from Firebase and displays it in a ListView.
 * @author Kevin
 */
public class AdminOrganizersListActivity extends BaseActivity {
    private ListView organizerListView;
    private OrganizerArrayAdapter organizerAdapter;
    private ArrayList<User> organizerDataList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_organizers_list);

        db = FirebaseFirestore.getInstance();

        ImageButton backButton = findViewById(R.id.btn_organizer_list_back);
        backButton.setOnClickListener(v -> finish());

        organizerListView = findViewById(R.id.lv_organizer_list);
        organizerDataList = new ArrayList<>();

        organizerAdapter = new OrganizerArrayAdapter(this, organizerDataList,
                user -> { // Delete Listener
                    new AlertDialog.Builder(this, R.style.DeleteGuard)
                            .setTitle("Delete Organizer")
                            .setMessage("Are you sure you want to delete this organizer?")
                            .setPositiveButton("Delete", ((dialog, which) -> {
                                db.collection("organizers").document(user.getUserId()).delete();
                                organizerDataList.remove(user);
                                organizerAdapter.notifyDataSetChanged();
                            }))
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
                },
                user -> { // Notify Listener (New)
                    Intent intent = new Intent(AdminOrganizersListActivity.this, adminNotificationsActivity.class);
                    // Pass the specific User ID to the next activity
                    intent.putExtra("sender_id", user.getUserId());
                    startActivity(intent);
                }
        );
        organizerListView.setAdapter(organizerAdapter);

        loadOrganizers();

    }

    private void loadOrganizers() {
        db.collection("organizers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                organizerDataList.clear();
                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    User user = new User(Role.ORGANIZER, doc.getId(),
                            doc.getString("name"), 
                            doc.getString("email"), 
                            doc.getString("phoneNumber"));
                    organizerDataList.add(user);
                }
                organizerAdapter.notifyDataSetChanged();
            }
        });
    }
}
