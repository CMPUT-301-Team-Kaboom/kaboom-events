package com.example.projecteventlotteryapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AdminRegistrationFragment extends DialogFragment {

    public interface AdminRegistrationDialogListener{
        public void OnConfirmedClick(String passkey, DialogFragment dialog);
        public void OnCancelledClick(DialogFragment dialog);
    }

    AdminRegistrationDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (AdminRegistrationDialogListener) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.fragment_admin_registration, null);

        builder.setView(view);

        EditText passkeyTextbox = view.findViewById(R.id.et_admin_guard_textbox);
        Button confirmBtn = view.findViewById(R.id.btn_admin_guard_confirm);
        Button cancelBtn = view.findViewById(R.id.btn_admin_guard_cancel);

        confirmBtn.setOnClickListener(v -> {
            String passkey = String.valueOf(passkeyTextbox.getText());
            listener.OnConfirmedClick(passkey, this);
        });

        cancelBtn.setOnClickListener(v -> listener.OnCancelledClick(AdminRegistrationFragment.this));

        return builder.create();
    }
}
