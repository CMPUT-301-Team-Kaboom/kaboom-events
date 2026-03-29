// references: https://www.geeksforgeeks.org/android/how-to-generate-qr-code-in-android/
// https://stackoverflow.com/questions/46065310/how-to-create-a-qr-code-generator-for-android-using-fragments

package com.example.projecteventlotteryapp.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.PosterImageHandler;
import com.example.projecteventlotteryapp.R;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.example.projecteventlotteryapp.dbUtils.FirestoreUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity for organizers to edit event details.
 * Provides fields for name, registration dates, draw date/time, entrant limits, location, and description.
 */
public class EditEventActivity extends AppCompatActivity {
    private String eventId;
    private Event event;
    private EventUtils eventUtils;
    private PosterImageHandler posterImageHandler;
    private EditText editName, editRegStart, editRegEnd, editDrawDate, editDrawTime;
    private EditText editEntrantLimit, editWaitlistLimit, editLocation, editDescription;
    private TextView editTag1, editTag2, editTag3;
    private SwitchCompat switchGeolocation;
    private Button saveButton, addCoorganizerButton;
    private ImageButton backButton;
    private ImageView editQRCode;
    private Bitmap qrCodeBitmap;
    /////////////////////////////////////////////////////
    /// IMAGE UPLOAD VARIABLES
    ////////////////////////////////////////////////////
    private ImageButton bannerEditButton;
    private ImageView editBanner;
    private ActivityResultLauncher<String> posterImageLauncher;
    private Uri eventPosterFilepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        // getting eventId from intent
        eventId = getIntent().getStringExtra("eventId");
        eventUtils = new EventUtils(FirebaseFirestore.getInstance());
        posterImageHandler = new PosterImageHandler();

        // initialize ui components
        editName = findViewById(R.id.et_event_edit_name);
        editRegStart = findViewById(R.id.et_event_edit_registration_start);
        editRegEnd = findViewById(R.id.et_event_edit_registration_end);
        editDrawDate = findViewById(R.id.et_event_edit_draw_date);
        editDrawTime = findViewById(R.id.et_event_edit_draw_time);
        editEntrantLimit = findViewById(R.id.et_event_edit_entrant_limit);
        editWaitlistLimit = findViewById(R.id.et_event_edit_waitlist_limit);
        editLocation = findViewById(R.id.et_edit_location);
        editDescription = findViewById(R.id.et_edit_description);
        switchGeolocation = findViewById(R.id.switch_geolocation);
        saveButton = findViewById(R.id.btn_event_edit_save);
        addCoorganizerButton = findViewById(R.id.btn_add_coorganizer);
        bannerEditButton = findViewById(R.id.btn_edit_event_edit_banner);
        editBanner = findViewById(R.id.iv_edit_banner);
        backButton = findViewById(R.id.btn_edit_event_back);
        editQRCode = findViewById(R.id.iv_edit_qr_code);

        editTag1 = findViewById(R.id.tv_edit_tag1);
        editTag2 = findViewById(R.id.tv_edit_tag2);
        editTag3 = findViewById(R.id.tv_edit_tag3);

        // initialize tags to "none" until loaded
        editTag1.setText("None");
        editTag2.setText("None");
        editTag3.setText("None");

        // fill in event info
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            event = eventUtils.fetchEventFromSnapshot(doc);
            setUI(event);
        });

        // set up back button listener
        backButton.setOnClickListener(v -> finish());

        // attach date and time pickers to their respective fields
        attachDatePicker(editRegStart);
        attachDatePicker(editRegEnd);
        attachDatePicker(editDrawDate);
        attachTimePicker(editDrawTime);

        // set up tag click listeners
        setupTagEditing(editTag1);
        setupTagEditing(editTag2);
        setupTagEditing(editTag3);

        posterImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null){
                        eventPosterFilepath = uri;
                        try {
                            editBanner.setImageURI(uri);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to upload image!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // set up save button listener
        saveButton.setOnClickListener(v -> saveEventDetails());
        bannerEditButton.setOnClickListener(v -> {
            posterImageLauncher.launch("image/*");
        });

        // set up add co-organizer button listener
        addCoorganizerButton.setOnClickListener(v -> showAddCoorganizerDialog());

        // set up qr code click listener
        editQRCode.setOnClickListener(v -> generateQRCode());
    }

    private void generateQRCode() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Event ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(eventId, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            qrCodeBitmap = barcodeEncoder.createBitmap(bitMatrix);
            editQRCode.setImageBitmap(qrCodeBitmap);
            Toast.makeText(this, "QR Code Generated!", Toast.LENGTH_SHORT).show();
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate Code!", Toast.LENGTH_SHORT).show();
        }
    }

    // reference https://developer.android.com/develop/ui/views/components/dialogs
    private void setupTagEditing(TextView tagView) {
        tagView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Edit Tag");

            View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_edit_tag, null);
            final EditText input = viewInflated.findViewById(R.id.et_dialog_edit_tag);
            
            String currentTag = tagView.getText().toString();
            if (!currentTag.equalsIgnoreCase("None")) {
                input.setText(currentTag);
            }
            
            builder.setView(viewInflated);

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                dialog.dismiss();
                String newTag = input.getText().toString().trim().toUpperCase();
                if (newTag.isEmpty()) {
                    tagView.setText("None");
                } else {
                    tagView.setText(newTag);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

            builder.show();
        });
    }

    private void showAddCoorganizerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Co-organizer");

        final EditText input = new EditText(this);
        input.setHint("Enter User ID");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String userId = input.getText().toString().trim();
            if (!userId.isEmpty()) {
                eventUtils.addCoorganizer(eventId, userId);
            } else {
                Toast.makeText(this, "User ID cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveEventDetails() {
        boolean isValid = true;

        // non-empty validation
        isValid &= fieldNotEmpty(editName);
        isValid &= fieldNotEmpty(editRegStart);
        isValid &= fieldNotEmpty(editRegEnd);
        isValid &= fieldNotEmpty(editDrawDate);
        isValid &= fieldNotEmpty(editDrawTime);
        isValid &= fieldNotEmpty(editEntrantLimit);

        // if any required field is empty, show error messages
        if (!isValid) {
            Toast.makeText(this, "Please fix the errors before saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        // take inputs
        String name = editName.getText().toString().trim();
        LocalDate regStart = LocalDate.parse(editRegStart.getText().toString().trim());
        LocalDate regEnd = LocalDate.parse(editRegEnd.getText().toString().trim());
        LocalDate drawDate = LocalDate.parse(editDrawDate.getText().toString().trim());
        LocalTime drawTime = LocalTime.parse(editDrawTime.getText().toString().trim());
        LocalDateTime drawDateTime = LocalDateTime.of(drawDate, drawTime);
        int entrantLimit = Integer.parseInt(editEntrantLimit.getText().toString().trim());
        int waitlistLimit = Integer.parseInt(editWaitlistLimit.getText().toString().trim().isEmpty() ? "0" : editWaitlistLimit.getText().toString().trim());
        String location = editLocation.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        boolean isGeolocationEnabled = switchGeolocation.isChecked();

        ArrayList<String> tags = new ArrayList<>();
        addTagToList(tags, editTag1);
        addTagToList(tags, editTag2);
        addTagToList(tags, editTag3);

        // logic validation
        if (!regEnd.isAfter(regStart)) {
            editRegEnd.setError("Must be after start date");
            isValid = false;
        }

        if (!drawDateTime.isAfter(regEnd.atStartOfDay())) {
            editDrawDate.setError("Must be after registration end date");
            isValid = false;
        }

        if (entrantLimit < 1) {
            editEntrantLimit.setError("Must be at least 1");
            isValid = false;
        }

        if (!isValid) return;

        // TODO: update the event object and database
        // TODO: figure out image and QR code, as well as location
        posterImageHandler.uploadPoster(eventId, eventPosterFilepath);
        if (qrCodeBitmap != null) {
            posterImageHandler.uploadQRCode(eventId, qrCodeBitmap);
        }

        ZoneId zoneId = ZoneId.systemDefault();

        Map<String, Object> event = new HashMap<>();
        event.put("name", name);
        event.put("registrationStartDate", FirestoreUtils.localDateToTimestamp(regStart, zoneId));
        event.put("registrationEndDate", FirestoreUtils.localDateToTimestamp(regEnd, zoneId));
        event.put("drawDate", FirestoreUtils.localDateTimeToTimestamp(drawDateTime, zoneId));
        event.put("entrantsLimit", entrantLimit);
        event.put("waitlistLimit", waitlistLimit);
        //event.put("location", location);
        event.put("description", description);
        event.put("geoLocationEnabled", isGeolocationEnabled);
        event.put("tags", tags);

        eventUtils.updateEventInDB(event, eventId);

        finish(); // close activity
    }

    private void addTagToList(ArrayList<String> list, TextView tagView) {
        String tag = tagView.getText().toString().trim();
        if (!tag.isEmpty() && !tag.equalsIgnoreCase("None") && !tag.equalsIgnoreCase("Tag 1") && !tag.equalsIgnoreCase("Tag 2") && !tag.equalsIgnoreCase("Tag 3")) {
            list.add(tag.toUpperCase());
        }
    }

    private boolean fieldNotEmpty(EditText field) {
        String value = field.getText().toString().trim();
        if (value.isEmpty()) {
            field.setError("Required");
            return false;
        }
        return true;
    }

    private void attachDatePicker(EditText editText) {
        editText.setFocusable(false);
        editText.setOnClickListener(v -> {
            LocalDate today = LocalDate.now();
            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                        editText.setText(selectedDate.toString());
                    },
                    today.getYear(),
                    today.getMonthValue() - 1,
                    today.getDayOfMonth()
            );
            picker.show();
        });
    }

    private void attachTimePicker(EditText editText) {
        editText.setFocusable(false);
        editText.setOnClickListener(v -> {
            TimePickerDialog picker = new TimePickerDialog(
                    this,
                    (view, hour, minute) -> {
                        LocalTime time = LocalTime.of(hour, minute);
                        editText.setText(time.toString());
                    },
                    12,
                    0,
                    true
            );
            picker.show();
        });
    }

    private void setUI(Event event){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        editName.setText(event.getName());
        editRegStart.setText(event.getRegistrationStartDate().format(dateFormatter));
        editRegEnd.setText(event.getRegistrationEndDate().format(dateFormatter));
        editDrawDate.setText(event.getDrawDate().format(dateFormatter));
        editDrawTime.setText(event.getDrawDate().format(timeFormatter));
        editEntrantLimit.setText(String.valueOf(event.getAttendeesLimit()));
        editWaitlistLimit.setText(String.valueOf(event.getWaitlistLimit()));
        // editLocation.setText(); // need to get event location
        editDescription.setText(event.getDescription());
        switchGeolocation.setChecked(event.isGeolocationEnabled());

        ArrayList<String> tags = event.getTagsList();
        if (tags != null) {
            if (tags.size() > 0) editTag1.setText(tags.get(0));
            else editTag1.setText("None");
            if (tags.size() > 1) editTag2.setText(tags.get(1));
            else editTag2.setText("None");
            if (tags.size() > 2) editTag3.setText(tags.get(2));
            else editTag3.setText("None");
        } else {
            editTag1.setText("None");
            editTag2.setText("None");
            editTag3.setText("None");
        }
    }
}
