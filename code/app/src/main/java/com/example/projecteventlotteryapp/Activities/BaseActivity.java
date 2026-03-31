package com.example.projecteventlotteryapp.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projecteventlotteryapp.R;
import com.example.projecteventlotteryapp.dbUtils.AccessibilityUtils;


/**
 * Base activity for all activities in the application.
 *
 * Automatically applies the correct theme before the activity is created,
 * depending on whether accessibility mode is enabled. All activities should
 * extend this class instead of AppCompatActivity to ensure consistent
 * theming across the app.
 *
 * @see AccessibilityUtils#isAccessibilityEnabled(android.content.Context)
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AccessibilityUtils.isAccessibilityEnabled(this)) {
            setTheme(R.style.Theme_ProjectEventLotteryApp_Accessibility);
        } else {
            setTheme(R.style.Base_Theme_ProjectEventLotteryApp);
        }
        super.onCreate(savedInstanceState);
    }
}
