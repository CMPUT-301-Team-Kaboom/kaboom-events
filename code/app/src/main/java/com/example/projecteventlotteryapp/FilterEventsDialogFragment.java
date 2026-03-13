package com.example.projecteventlotteryapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.FirestoreGrpc;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link DialogFragment} subclass.
 * Use the {@link FilterEventsDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterEventsDialogFragment extends DialogFragment {

    /*
    The following code and related OnAttach code is adapted from...
    Author: Kalyaganov Alexey https://stackoverflow.com/users/1979290/kalyaganov-alexey
    Title: "Passing Data Between Fragments to Activity"
    Answer: https://stackoverflow.com/a/14440095
    Date: 2013-01-21
    Retrieved: 2026-03-11
    License: CC-BY-SA 3.0
    */
    interface FilterEventsListener {
        void filterEvents(String name, String status, ArrayList<String> tags, LocalDate startDate, LocalDate endDate, LocalDate drawDate);

        void clearFilters();
    }

    private FilterEventsListener listener;
    private LinearLayout tagContainerLinearLayout;
    private ArrayList<String> tags;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        listener.clearFilters();    // band aid fix for subsequent filter button presses leaving dirty values
        View view = getLayoutInflater().inflate(R.layout.fragment_filter_events, null);
        tags = new ArrayList<String>();

        // grab references to EditTexts and Buttons
        EditText filterName = view.findViewById(R.id.et_filter_name);
        EditText filterRegStart = view.findViewById(R.id.et_filter_registration_start);
        EditText filterRegEnd = view.findViewById(R.id.et_filter_registration_end);
        EditText filterDrawDate = view.findViewById(R.id.et_filter_draw_date);
        Button clearFiltersButton = view.findViewById(R.id.btn_clear_filters);
        Button confirmButton = view.findViewById(R.id.btn_filter_confirm);
        Button addTagButton = view.findViewById(R.id.btn_add_tag);
        tagContainerLinearLayout = view.findViewById(R.id.ll_filter_tags);

        // convert EditTexts for dates to be pickers instead of text
        attachDatePicker(filterRegStart);
        attachDatePicker(filterRegEnd);
        attachDatePicker(filterDrawDate);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);;

        // addTag button logic
        addTagButton.setOnClickListener(v -> {
            EditText input = new EditText(requireContext());
            input.setHint("Enter Tag");

            new AlertDialog.Builder(requireContext())
                    .setTitle("Enter value")
                    .setView(input)
                    .setPositiveButton("Add", (nestedDialog, which) -> {

                        String tagText = input.getText().toString().trim().toUpperCase();

                        if (!tagText.isEmpty()) {
                            addNewTagBox(tagText);
                        }

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // clear Filter logic
        clearFiltersButton.setOnClickListener(v -> {
            listener.clearFilters();
            dialog.dismiss();
        });

        confirmButton.setOnClickListener(v -> {
            // validation
            boolean isValid = true;

            // TODO: implement status
            String status = null;

            String name = getTextOrNull(filterName);

            // initialize date filters
            LocalDate regStart = getLocalDateOrNull(filterRegStart);
            LocalDate regEnd = getLocalDateOrNull(filterRegEnd);
            LocalDate drawDate = getLocalDateOrNull(filterDrawDate);

            if (regStart != null && regEnd != null && !regEnd.isAfter(regStart)) {
                filterRegEnd.setError("Must be after start date");
                isValid = false;
            }

            if (drawDate != null && regEnd != null && !drawDate.isAfter(regEnd)) {
                filterDrawDate.setError("Must be after registration end date");
                isValid = false;
            }

            // make sure at least one filter is set before filtering
            if (name == null && status == null && tags == null && regStart == null && regEnd == null && drawDate == null) {
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            // give some filter info to EventListFragment
            if (listener != null) {
                listener.filterEvents(name, status, tags, regStart, regEnd, drawDate);
            }

            dialog.dismiss();
        });

        return dialog;
    }

    // make EventsListActivity implement the interface
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FilterEventsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FilterEventsListener");
        }

    }

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

    private String getTextOrNull(EditText editText) {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return text;
    }

    /**
     * Helper function to extract the LocalDate from an EditText or null
     *
     * @param editText the editText to extract from; MUST be set by a datePicker
     * @return
     */
    private LocalDate getLocalDateOrNull(EditText editText) {
        String dateText = editText.getText().toString();
        if (dateText.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateText);
    }

    /**
     * Adds a new textbox to the fragment_filter_events tags linear layout
     * code adapted from AI prompt:
     * "How can I add a textview to a LinearLayout using Java code instead of XML"
     *
     * @param tagText
     */
    private void addNewTagBox(String tagText) {
        Log.d("FilterEventsDialogFragment", "new Tag filer: " + tagText);

        int currentBoxes = tagContainerLinearLayout.getChildCount() - 1;    // -1 to remove the '+' button
        if (currentBoxes >= 5) return;

        TextView newBox = new TextView(requireContext(), null, 0, R.style.TagBoxTextView);
        newBox.setText(tagText);

        tags.add(tagText);
        int insertIndex = tagContainerLinearLayout.getChildCount() - 1;
        tagContainerLinearLayout.addView(newBox, insertIndex);
    }
}