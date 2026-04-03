package com.example.projecteventlotteryapp.Activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.projecteventlotteryapp.R;

/**
 * DialogFragment for displaying a QR code
 */
public class ViewQRCodeDialogFragment extends DialogFragment {

    private static final String ARG_QR_URL = "qr_url";

    public static ViewQRCodeDialogFragment newInstance(String qrUrl) {
        ViewQRCodeDialogFragment fragment = new ViewQRCodeDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QR_URL, qrUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_view_qr_code, null);

        ImageView qrImageView = view.findViewById(R.id.iv_qr_code_display);
        Button closeButton = view.findViewById(R.id.btn_close_qr);

        String qrUrl = getArguments() != null ? getArguments().getString(ARG_QR_URL) : null;

        if (qrUrl != null && !qrUrl.isEmpty()) {
            Glide.with(this).load(qrUrl).into(qrImageView);
        }

        closeButton.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}
