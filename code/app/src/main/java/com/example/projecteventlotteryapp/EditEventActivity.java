// references: https://www.geeksforgeeks.org/android/how-to-generate-qr-code-in-android/

package com.example.projecteventlotteryapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Activity for organizers to edit event details.
 * Provides fields for name, registration dates, draw date/time, entrant limits, location, and description.
 */
public class EditEventActivity extends AppCompatActivity {

    private EditText editName, editRegStart, editRegEnd, editDrawDate, editDrawTime;
    private EditText editEntrantLimit, editWaitlistLimit, editLocation, editDescription;
    private SwitchCompat switchGeolocation;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

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

        // attach date and time pickers to their respective fields
        attachDatePicker(editRegStart);
        attachDatePicker(editRegEnd);
        attachDatePicker(editDrawDate);
        attachTimePicker(editDrawTime);

        // set up save button listener
        saveButton.setOnClickListener(v -> saveEventDetails());
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

        finish(); // close activity
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
}
