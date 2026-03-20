package com.example.projecteventlotteryapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.google.firebase.firestore.FirebaseFirestore;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A simple {@link DialogFragment} subclass organizers use to create a new Event.
 *
 * <p>This DialogFragment class is used by organizers to create a new Event and store it in
 * the database. It is invoked from the Organizers main menu</p>
 */
public class CreateEventDialogFragment extends DialogFragment {
    public interface OnEventCreatedListener {
        void OnEventCreated();
    }
    private OnEventCreatedListener listener;
    private EventUtils eventUtils;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (OnEventCreatedListener) context;
    }

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
        eventUtils = new EventUtils(FirebaseFirestore.getInstance());

        // grab references to editTexts and confirm button
        EditText editName = view.findViewById(R.id.et_event_edit_name);
        EditText editRegStart = view.findViewById(R.id.et_event_edit_registration_start);
        EditText editRegEnd = view.findViewById(R.id.et_event_edit_registration_end);
        EditText editDrawDate = view.findViewById(R.id.et_event_edit_draw_date);
        EditText editDrawTime = view.findViewById(R.id.et_event_edit_draw_time);
        EditText editEntrantLimit = view.findViewById(R.id.et_event_edit_entrant_limit);
        SwitchCompat isPrivateSwitch = view.findViewById(R.id.switch_create_isPrivate);
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
            boolean isPrivate = isPrivateSwitch.isChecked();


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

            Event event = new Event(name, regStart, regEnd, drawDateTime, entrantLimit, isPrivate);
            MyApp app = (MyApp) requireActivity().getApplication();
            eventUtils.createNewEventDbItem(event, app.getCurrentUser().getUserId());
            listener.OnEventCreated();
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
}