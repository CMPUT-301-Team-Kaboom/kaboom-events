package com.example.projecteventlotteryapp.Models;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.projecteventlotteryapp.R;

public class CreateNotificationDialogFragment extends DialogFragment {

    public interface NotificationListener {
        void onSendNotification(String message);
    }

    private NotificationListener listener;

    public static CreateNotificationDialogFragment newInstance() {
        return new CreateNotificationDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NotificationListener) {
            listener = (NotificationListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_notification, container, false);

        EditText etMessage = view.findViewById(R.id.et_notification_message);
        Button btnBack = view.findViewById(R.id.btn_notif_back);
        Button btnSend = view.findViewById(R.id.btn_notif_send);

        btnBack.setOnClickListener(v -> dismiss());

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString();
            if (!message.isEmpty() && listener != null) {
                listener.onSendNotification(message);
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Set the window to take up the full available width
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // Keep the background transparent for rounded corners
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
