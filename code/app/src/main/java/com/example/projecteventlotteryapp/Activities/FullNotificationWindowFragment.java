package com.example.projecteventlotteryapp.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.projecteventlotteryapp.EventsListFragment;
import com.example.projecteventlotteryapp.R;

import javax.annotation.Nullable;

/**
 * Dialog fragment that displays the full notification window.
 */
public class FullNotificationWindowFragment extends DialogFragment {
    // Keys for the bundle
    private static final String ARG_TITLE = "title";
    private static final String ARG_SENDER = "sender";
    private static final String ARG_BODY = "body";

    /**
     * Creates a new instance of the dialog fragment.
     * @param title
     * @param sender
     * @param body
     * @return
     */
    public static FullNotificationWindowFragment newInstance(String title, String sender, String body) {
        FullNotificationWindowFragment fragment = new FullNotificationWindowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_SENDER, sender);
        args.putString(ARG_BODY, body);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_full_notifcation_window, container, false);

        // initialize UI elements
        TextView titleTextView = view.findViewById(R.id.tv_full_notif_title);
        TextView senderTextView = view.findViewById(R.id.tv_full_notif_sender);
        TextView bodyTextView = view.findViewById(R.id.tv_full_notif_body);
        ImageButton backButton = view.findViewById(R.id.btn_full_notif_back);

        // set the text for the UI elements
        if (getArguments() != null) {
            titleTextView.setText(getArguments().getString(ARG_TITLE));
            senderTextView.setText(getArguments().getString(ARG_SENDER));
            bodyTextView.setText(getArguments().getString(ARG_BODY));
        }

        // close the dialog when back button is pressed
        backButton.setOnClickListener(v -> dismiss());

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        // This ensures the dialog is wide enough and background is transparent for rounded corners
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}