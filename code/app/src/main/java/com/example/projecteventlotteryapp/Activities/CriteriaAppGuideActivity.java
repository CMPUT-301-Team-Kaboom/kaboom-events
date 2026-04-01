package com.example.projecteventlotteryapp.Activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projecteventlotteryapp.R;

/**
 * Lottery criteria guide for the user.
 * Displays rules and criteria for the lottery.
 * @author Kevin
 */
public class CriteriaAppGuideActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criteria);


        // Find back button
        ImageButton backButton = findViewById(R.id.BackButton);

        backButton.setOnClickListener( v-> finish());
    }
}
