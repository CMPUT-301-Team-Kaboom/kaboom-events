package com.example.projecteventlotteryapp.Activities;

import static com.example.projecteventlotteryapp.dbUtils.FirestoreUtils.storeNotificationInFirestore;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.projecteventlotteryapp.Models.CreateNotificationDialogFragment;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.OrganizerEntrantListAdapter;
import com.example.projecteventlotteryapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class OrganizerEnrolledActivity extends BaseActivity implements CreateNotificationDialogFragment.NotificationListener {
    private String eventId;
    private String eventName;
    private OrganizerEntrantListAdapter adapter;
    private ListView enrolledListView;
    private ImageButton backBtn;
    private Button selectBtn;
    private Button doneBtn;
    private Button sendNotifBtn;
    private Button exportBtn;
    private ConstraintLayout floatingActionsContainer;
    private ArrayList<String> enrolledList;
    private FirebaseFirestore db;
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_enrolled);

        // finding ui elements
        enrolledListView = findViewById(R.id.lv_organizer_enrolled_list);
        Intent intent  = getIntent();
        eventId = intent.getStringExtra("eventID");
        eventName = intent.getStringExtra("eventName");

        exportBtn = findViewById(R.id.btn_organizer_enrolled_export);
        selectBtn = findViewById(R.id.btn_organizer_enrolled_select);
        doneBtn = findViewById(R.id.btn_done);
        floatingActionsContainer = findViewById(R.id.cl_floating_actions);
        backBtn = findViewById(R.id.btn_organizer_enrolled_back);
        backBtn.setOnClickListener(v -> finish());
        sendNotifBtn = findViewById(R.id.btn_send_notification);


        // hide notification buttons container initially
        floatingActionsContainer.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        DocumentReference eventDoc = db.collection("events").document(eventId);

        eventDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                if(doc.exists()){
                    if (eventName == null) {
                        eventName = doc.getString("name");
                    }
                    enrolledList = (ArrayList<String>) doc.get("enrolled");
                    if (enrolledList == null) enrolledList = new ArrayList<>();

                    adapter = new OrganizerEntrantListAdapter(this, enrolledList);
                    enrolledListView.setAdapter(adapter);

                    TextView enrolledSize = findViewById(R.id.tv_organizer_enrolled_size);
                    if (doc.contains("entrantsLimit")) {
                        enrolledSize.setText(enrolledList.size() + "/" + doc.get("entrantsLimit"));
                    } else {
                        enrolledSize.setText(String.valueOf(enrolledList.size()));
                    }
                }
            } else {
                Log.d("OrganizerEnrolled", "Document retrieval failed", task.getException());
            }
        });

        selectBtn.setOnClickListener(v -> {
            if (!isSelectionMode) {
                // Enter selection mode
                isSelectionMode = true;
                selectBtn.setText("Select All");
                floatingActionsContainer.setVisibility(View.VISIBLE);
                adapter.setSelectionMode(true);
            } else {
                // select all otherwise
                adapter.selectAll();
            }
        });

        doneBtn.setOnClickListener(v -> {
            // Exit Selection Mode
            isSelectionMode = false;
            selectBtn.setText("Select");
            floatingActionsContainer.setVisibility(View.GONE);
            adapter.setSelectionMode(false);
            adapter.clearSelection();
        });

        sendNotifBtn.setOnClickListener(v -> {
            // Only show if users are selected
            if (!adapter.getSelectedPositions().isEmpty()) {
                CreateNotificationDialogFragment.newInstance()
                        .show(getSupportFragmentManager(), "create_notification");
            } else {
                Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show();
            }
        });

        exportBtn.setOnClickListener(v -> {
            Set<Integer> selected = adapter.getSelectedPositions();
            if (!selected.isEmpty()){
                ArrayList<String[]> userData = new ArrayList<>();

                userData.add(new String[] {"User ID", "Name", "Email", "Phone", "Location"});

                AtomicInteger counter = new AtomicInteger(selected.size());
                // formats and adds user data to array list
                for (Integer pos : selected){
                    db.collection("entrants").document(enrolledList.get(pos)).get().addOnSuccessListener(doc -> {
                        if (doc.exists()){
                            String userID = doc.getString("deviceID");
                            String name = doc.getString("name");
                            String email = doc.getString("email");
                            String phone = (doc.getString("phone").isEmpty()) ? "N/A" : doc.getString("phone");
                            String location = doc.getGeoPoint("location").getLatitude() + " " + doc.getGeoPoint("location").getLongitude();

                            String[] line = {userID, name, email, phone, location};
                            userData.add(line);
                            Log.d("ExportCSV", userID + "," + name + "," + email + "," + phone + "," + location);
                        }

                        // write to CSV when all async calls are done
                        if(counter.decrementAndGet() == 0){
                            writeToCSV(userData);
                        }
                    });
                }

                // Exit Selection Mode
                isSelectionMode = false;
                selectBtn.setText("Select");
                floatingActionsContainer.setVisibility(View.GONE);
                adapter.setSelectionMode(false);
                adapter.clearSelection();
            } else {
                Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSendNotification(String message) {
        // handle the sending logic

        // fetch userID from global app state
        String userId = ((MyApp) getApplication()).getCurrentUser().getUserId();

        Set<Integer> selected = adapter.getSelectedPositions();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Integer pos : selected) {
            String recipientId = enrolledList.get(pos);
            storeNotificationInFirestore(userId, recipientId, message, eventName, eventId, db);
        }

        Toast.makeText(this, "Notifications sent", Toast.LENGTH_SHORT).show();

        // Clear selection after sending
        isSelectionMode = false;
        selectBtn.setText("Select");
        floatingActionsContainer.setVisibility(View.GONE);
        adapter.setSelectionMode(false);
        adapter.clearSelection();
    }

    /**
     * Exports a list of data to a CSV file in the user's downloads folder
     *
     * <p>This method uses OpenCSV to write a provided list of data to a CSV file and exports
     * the CSV file to the user's downloads folder. <b><i>Note: User must be using API 29+ </i></b></p>
     * @param enrolledList
     */
    private void writeToCSV(ArrayList<String[]> enrolledList){
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, eventName + "_enrolled.csv");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        if (uri == null) return;

        try (OutputStream os = resolver.openOutputStream(uri);
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(os))) {

            writer.writeAll(enrolledList);

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Selected entrants exported to CSV", Toast.LENGTH_SHORT).show();
    }
}
