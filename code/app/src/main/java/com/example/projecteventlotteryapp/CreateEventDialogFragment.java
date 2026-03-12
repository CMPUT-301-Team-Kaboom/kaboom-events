package com.example.projecteventlotteryapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.FirestoreGrpc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link DialogFragment} subclass organizers use to create a new Event.
 *
 * <p>This DialogFragment class is used by organizers to create a new Event and store it in
 * the database. It is invoked from the Organizers main menu</p>
 */
public class CreateEventDialogFragment extends DialogFragment {

    /**
     * Entry point of the DialogFragment
     *
     * <p>Sets up UI, handles input validation and creates a new Event db object on success </p>
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return Dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_create_event, null);

        // grab references to editTexts and confirm button
        EditText editName = view.findViewById(R.id.et_event_edit_name);
        EditText editRegStart = view.findViewById(R.id.et_event_edit_registration_start);
        EditText editRegEnd = view.findViewById(R.id.et_event_edit_registration_end);
        EditText editDrawDate = view.findViewById(R.id.et_event_edit_draw_date);
        EditText editDrawTime = view.findViewById(R.id.et_event_edit_draw_time);
        EditText editEntrantLimit = view.findViewById(R.id.et_event_edit_entrant_limit);
        Button confirmButton = view.findViewById(R.id.btn_event_edit_confirm);

        // convert editTexts for dates and times to be pickers instead of text
        attachDatePicker(editRegStart);
        attachDatePicker(editRegEnd);
        attachDatePicker(editDrawDate);
        attachTimePicker(editDrawTime);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();

        confirmButton.setOnClickListener(v -> {
            // Validation
            boolean isValid = true;
            isValid &= fieldNotEmpty(editName);
            isValid &= fieldNotEmpty(editRegStart);
            isValid &= fieldNotEmpty(editRegEnd);
            isValid &= fieldNotEmpty(editDrawDate);
            isValid &= fieldNotEmpty(editDrawTime);
            isValid &= fieldNotEmpty(editEntrantLimit);

            // return after field checks
            if (!isValid) {
                return;
            }

            String name = editName.getText().toString().trim();
            LocalDate regStart = LocalDate.parse(editRegStart.getText().toString().trim());
            LocalDate regEnd = LocalDate.parse(editRegEnd.getText().toString().trim());
            LocalDate drawDate = LocalDate.parse(editDrawDate.getText().toString().trim());
            LocalTime drawTime = LocalTime.parse(editDrawTime.getText().toString().trim());
            LocalDateTime drawDateTime = LocalDateTime.of(drawDate, drawTime);
            int entrantLimit = Integer.parseInt(editEntrantLimit.getText().toString().trim());

            if (!regEnd.isAfter(regStart)) {
                editRegEnd.setError("Must be after start date");
                isValid = false;
            }

            if (!drawDateTime.isAfter(regEnd.atStartOfDay())) {
                editDrawDate.setError("Must be after registration end date");
                isValid = false;
            }

            if (entrantLimit < 1) {
                editEntrantLimit.setError("Must be greater than 1");
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            Event event = new Event(name, regStart, regEnd, drawDateTime, entrantLimit);
            createNewEventDbItem(event);
            dialog.dismiss();
        });

        return dialog;
    }

    /**
     * Helper function to validate if an EditText field is not empty
     * @param field the EditText
     * @return boolean
     */
    private boolean fieldNotEmpty(EditText field) {
        String value = field.getText().toString().trim();
        if (value.isEmpty()) {
            field.setError("Required");
            return false;
        }
        return true;
    }

    /**
     * Attaches a date picker to an EditText to ease date selection
     * @param editText the editText
     */
    private void attachDatePicker(EditText editText) {
        editText.setFocusable(false);

        editText.setOnClickListener(v -> {
            LocalDate today = LocalDate.now();
            DatePickerDialog picker = new DatePickerDialog(
                    requireContext(),
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

    /**
     * Attaches a time picker to an EditText to ease date selection
     * @param editText the editText
     */
    private void attachTimePicker(EditText editText) {
        editText.setFocusable(false);

        editText.setOnClickListener(v-> {
            TimePickerDialog picker = new TimePickerDialog(
                    requireContext(),
                    (view1, hour, minute) -> {
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

    /**
     * Creates and uploads a new Event database object
     *
     * <p>Uses an Event instance and uploads it to the database. Only populates the following attributes:
     * organizer
     * name
     * drawDate
     * registrationEndDate
     * registrationStartDate
     * drawDate
     * entrantsLimit
     *
     * All other values are initialized to null or the equivalent default value
     * Further event refinement is handled by event editing</p>
     *
     * @param event the Event that is to be uploaded
     */
    private void createNewEventDbItem(Event event) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Inputted values
        HashMap<String, Object> eventData = new HashMap<>();

        MyApp app = (MyApp) requireActivity().getApplication();
        DocumentReference organizerRef =
                db.collection("organizers").document(app.getCurrentUser().getUserId());
        DocumentReference defaultPoster =
                db.collection("posters").document("default_poster");
        eventData.put("organizer", organizerRef);
        eventData.put("name", event.getName());
        eventData.put("poster", defaultPoster);

        ZoneId zoneId = ZoneId.systemDefault();
        // TODO: update to grab system zoneid
        eventData.put(
                "drawDate",
                FirestoreUtils.localDateTimeToTimestamp(
                        event.getDrawDate(),
                        zoneId
                )
        );
        eventData.put(
                "registrationEndDate",
                FirestoreUtils.localDateToTimestamp(
                        event.getRegistrationEndDate(),
                        zoneId
                )
        );
        eventData.put(
                "registrationStartDate",
                FirestoreUtils.localDateToTimestamp(
                        event.getRegistrationStartDate(),
                        zoneId
                )
        );
        eventData.put("entrantsLimit", event.getAttendeesLimit());

        // setting null values
        eventData.put("description", null);
        eventData.put("geoLocationEnabled", false);
        eventData.put("location", null);
        eventData.put("qrCodePath", null);
        eventData.put("tags", null);
        eventData.put("waitlistLimit", -1);  // -1 indicates no limit
        eventData.put("waitlist", new ArrayList<>());
        eventData.put("enrolled", new ArrayList<>());
        eventData.put("invited", new ArrayList<>());
        eventData.put("declined", new ArrayList<>());

        db.collection("events")
            .add(eventData)
            .addOnSuccessListener(documentReference -> {
                Log.d("Firestore", "Document added to events with id: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.w("Firestore", "Error adding document", e);
            });
    }
}