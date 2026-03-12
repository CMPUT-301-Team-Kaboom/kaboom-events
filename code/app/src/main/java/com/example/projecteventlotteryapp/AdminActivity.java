package com.example.projecteventlotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * AdminActivity
 * Serves as the "profile" page for the Admin, users can
 * edit their personal information such as Name, Email, and Phone number.
 * Users can also enable/disable notifications, sign out, and delete their profile
 * @author Kevin
 */
public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        ImageButton btnBack = findViewById(R.id.BackButton);
        Button btnSave = findViewById(R.id.btn_save_profile);
        Button btnSignOut = findViewById(R.id.btn_sign_out);
        Button btnDelete = findViewById(R.id.btn_delete_profile);

        btnBack.setOnClickListener(v -> finish());

    }
}
