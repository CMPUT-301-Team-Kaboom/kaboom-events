package com.example.projecteventlotteryapp.dbUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AccessibilityUtils {

    public static boolean isAccessibilityEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("accessibility_mode", false);
    }
    public static void setAccessibilityEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("accessibility_mode", enabled).apply();
    }

    public static void applyTextViewStyle(TextView textView, Context context) {
        if (isAccessibilityEnabled(context)) {
            textView.setTextSize(28);
        } else {
            textView.setTextSize(24);
        }
    }

    public static void applyBodyTextStyle(TextView textView, Context context) {
        if (isAccessibilityEnabled(context)) {
            textView.setTextSize(20);
        } else {
            textView.setTextSize(16);
        }
    }

    public static void applyButtonStyle(Button button, Context context) {
        if (isAccessibilityEnabled(context)) {
            button.setTextSize(18);
        } else {
            button.setTextSize(14);
        }
    }

    public static void applyEditTextStyle(EditText editText, Context context) {
        if (isAccessibilityEnabled(context)) {
            editText.setTextSize(20);
            editText.setMinHeight(140);
        } else {
            editText.setTextSize(16);
            editText.setMinHeight(100);
        }
    }
}