package com.example.projecteventlotteryapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.os.Trace;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A simple {@link DialogFragment} subclass.
 * Use the {@link CreateEventDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateEventDialogFragment extends DialogFragment {
    interface CreateEventDialogListener {
        void addEvent(Event event);
    }
    private CreateEventDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof  CreateEventDialogListener) {
            listener = (CreateEventDialogListener) listener;
        } else {
            throw new RuntimeException("Implement Listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedINstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_create_event, null);

        // grab references to editTexts and confirm button
        EditText editName = view.findViewById(R.id.et_event_creation_name);
        EditText editRegStart = view.findViewById(R.id.et_event_creation_registration_start);
        EditText editRegEnd = view.findViewById(R.id.et_event_creation_registration_end);
        EditText editDrawDate = view.findViewById(R.id.et_event_creation_draw_date);
        EditText editDrawTime = view.findViewById(R.id.et_event_creation_draw_time);
        EditText editEntrantLimit = view.findViewById(R.id.et_event_creation_entrant_limit);
        Button confirmButton = view.findViewById(R.id.btn_event_creation_confirm);

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

            if (!drawDateTime.isAfter(regEnd)) {
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
            listener.addEvent(event);
            dialog.dismiss();
        });

        return dialog;
    }

    private boolean fieldNotEmpty(EditText field) {
        String value = field.getText().toString().trim();
        if (value.isEmpty()) {
            field.setError("Required");
            return false;
        }
        return true;
    }
}