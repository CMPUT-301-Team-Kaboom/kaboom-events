package com.example.projecteventlotteryapp.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projecteventlotteryapp.R;
import com.example.projecteventlotteryapp.dbUtils.AccessibilityUtils;

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
