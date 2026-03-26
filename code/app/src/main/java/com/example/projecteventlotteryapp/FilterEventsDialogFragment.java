package com.example.projecteventlotteryapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.EventsFilter;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.Models.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link DialogFragment} that allows the user to filter events.
 *
 * Citations:
 *      [1] Author: Kalyaganov Alexey https://stackoverflow.com/users/1979290/kalyaganov-alexey
 *          Title: "Passing Data Between Fragments to Activity"
 *          Answer: https://stackoverflow.com/a/14440095
 *           Date: 2013-01-21
 *          Retrieved: 2026-03-11
 *          License: CC-BY-SA 3.0
 */
public class FilterEventsDialogFragment extends DialogFragment {

    //  interface for the EventsActivity to implement to get the filter changes (see citation [1])
    interface FilterEventsListener {
        /**
         * Filters events.
         *
         * @param filter to apply
         */
        void filterEvents(EventsFilter filter);

        /**
         * Clears all filters.
         */
        void clearFilters();
    }

    private FilterEventsListener listener;
    private User globalUser;
    private LinearLayout tagContainerLinearLayout;
    private ArrayList<String> tags;
    private ToggleButton enrolledToggleButton, declinedToggleButton, invitedToggleButton, onWaitListToggleButton;

    /**
     * Creates the dialog used to filter events.
     *
     * @param savedInstanceState saved instance state
     * @return an AlertDialog with filter options
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_filter_events, null);
        tags = new ArrayList<String>();

        // get user role
        MyApp app = (MyApp) getActivity().getApplication();
        globalUser = app.getCurrentUser();

        // grab references to EditTexts and Buttons
        EditText filterName = view.findViewById(R.id.et_filter_name);
        EditText filterRegStart = view.findViewById(R.id.et_filter_registration_start);
        EditText filterRegEnd = view.findViewById(R.id.et_filter_registration_end);
        EditText filterDrawDate = view.findViewById(R.id.et_filter_draw_date);
        Button clearFiltersButton = view.findViewById(R.id.btn_clear_filters);
        Button confirmButton = view.findViewById(R.id.btn_filter_confirm);
        Button addTagButton = view.findViewById(R.id.btn_add_tag);
        tagContainerLinearLayout = view.findViewById(R.id.ll_filter_tags);

        enrolledToggleButton = view.findViewById(R.id.mbtn_filter_enrolled);
        declinedToggleButton = view.findViewById(R.id.mbtn_filter_declined);
        invitedToggleButton = view.findViewById(R.id.mbtn_filter_invited);
        onWaitListToggleButton = view.findViewById(R.id.mbtn_filter_on_wait);
        List<ToggleButton> toggleButtons = List.of(
                enrolledToggleButton,
                declinedToggleButton,
                invitedToggleButton,
                onWaitListToggleButton
        );

        setupToggleButtons(toggleButtons);

        // hide status filters based on role
        if (globalUser.getRole() == Role.ORGANIZER) {
            ConstraintLayout statusesConstraintLayout = view.findViewById(R.id.cl_enrollment_status);
            if (statusesConstraintLayout != null) {
                statusesConstraintLayout.setVisibility(View.GONE);
            }
        }


        // convert EditTexts for dates to be pickers instead of text
        attachDatePicker(filterRegStart);
        attachDatePicker(filterRegEnd);
        attachDatePicker(filterDrawDate);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // addTag button logic
        addTagButton.setOnClickListener(v -> {
            EditText input = new EditText(requireContext());
            input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(16) });
            input.setHint("Tag");

            new AlertDialog.Builder(requireContext())
                    .setTitle("Enter Tag")
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

        // clear filters logic
        clearFiltersButton.setOnClickListener(v -> {
            listener.clearFilters();
            dialog.dismiss();
        });

        // confirm button logic
        confirmButton.setOnClickListener(v -> {
            EventsFilter filter = new EventsFilter();

            // name
            filter.name = getTextOrNull(filterName);

            // status
            filter.status = getToggleStatus();

            // tags
            filter.tags = tags;

            // date
            filter.regStart = getLocalDateOrNull(filterRegStart);
            filter.regEnd = getLocalDateOrNull(filterRegEnd);
            filter.drawDate = getLocalDateOrNull(filterDrawDate);

            // validation checks
            boolean isValid = true;

            if (filter.regStart != null && filter.regEnd != null && !filter.regEnd.isAfter(filter.regStart)) {
                filterRegEnd.setError("Must be after registration start date");
                isValid = false;
            }

            if (filter.drawDate != null && filter.regEnd != null && !filter.drawDate.isAfter(filter.regEnd)) {
                filterDrawDate.setError("Must be after registration end date");
                isValid = false;
            }

            // make sure at least one filter is set before filtering
            if (filter.isEmpty()) {
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            // give some filter info to EventListFragment
            if (listener != null) {
                listener.filterEvents(filter);
            }

            dialog.dismiss();
        });

        return dialog;
    }

    /**
     * Ensures that the EventsActivity implements the {@link FilterEventsListener} interface.
     *
     * @param context to attach to
     * @throws ClassCastException if the activity doesn't implement the listener
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FilterEventsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FilterEventsListener");
        }

    }

    /**
     * Attaches a date picker to an EditText field.
     *
     * @param editText to attach the DatePicker to
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
     * Gets the status chosen from the toggle buttons.
     *
     * @return status as a String ("enrolled", "declined", "invited", "waitlist"), or null if none selected
     */
    private String getToggleStatus() {
        Map<ToggleButton, String> toggleMap = new HashMap<>();
        toggleMap.put(enrolledToggleButton, "enrolled");
        toggleMap.put(declinedToggleButton, "declined");
        toggleMap.put(invitedToggleButton, "invited");
        toggleMap.put(onWaitListToggleButton, "waitlist");

        for (Map.Entry<ToggleButton, String> entry : toggleMap.entrySet()) {
            if (entry.getKey().isChecked()) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Sets up the toggle buttons so only one can be checked at a time.
     *
     * @param buttons a list of toggle buttons
     */
    private void setupToggleButtons(List<ToggleButton> buttons) {
        for (ToggleButton tb : buttons) {
            tb.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                if (!isChecked) {
                    return;
                }

                for (ToggleButton other : buttons) {
                    if (other != buttonView) {
                        other.setChecked(false);
                    }
                }
            }));
        }
    }

    /**
     * Helper function to get the text from an EditText or null
     *
     * @param editText to extract from
     * @return either a String or null
     */
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
     * @param editText the EditText to extract from; MUST be set by a datePicker
     * @return either a LocalDate or null
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
     * @param tagText to add
     */
    private void addNewTagBox(String tagText) {
        Log.d("FilterEventsDialogFragment", "new Tag fitler: " + tagText);

        int currentBoxes = tagContainerLinearLayout.getChildCount() - 1;    // -1 to remove the '+' button
        if (currentBoxes >= 3) return;

        TextView newBox = new TextView(requireContext(), null, 0, R.style.TagBoxTextView);
        newBox.setText(tagText);

        tags.add(tagText);
        int insertIndex = tagContainerLinearLayout.getChildCount() - 1;
        tagContainerLinearLayout.addView(newBox, insertIndex);
    }
}