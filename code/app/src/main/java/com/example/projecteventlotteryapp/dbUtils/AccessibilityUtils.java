package com.example.projecteventlotteryapp.dbUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

/**
 * Utility class for handling accessibility mode.
 *
 * <p>This class provides methods to check and set the accessibility mode, allowing users to change small details
 * within accessibility mode like a text color etc.</p>
 *
 * example usage: enabled = AccessibilityUtils.isAccessibilityEnabled(this);
 */
public class AccessibilityUtils {


    /**
     * Checks if accessibility mode is enabled
     * @param context
     * @return boolean value of weather it is enabled or not
     */
    public static boolean isAccessibilityEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("accessibility_mode", false);
    }

    /**
     * Sets the accessibility mode
     * @param context
     * @param enabled Boolean
     */
    public static void setAccessibilityEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("accessibility_mode", enabled).apply();
    }


    public static void applyTextViewColor(TextView textView, Context context, @ColorRes int defaultColor, @ColorRes int accessibilityColor) {
        if (isAccessibilityEnabled(context)) {
            textView.setTextColor(ContextCompat.getColor(context, accessibilityColor));
        } else {
            textView.setTextColor(ContextCompat.getColor(context, defaultColor));
        }
    }

    public static void applyHintColor(EditText editText, Context context, @ColorRes int defaultColor, @ColorRes int accessibilityColor) {
        if (isAccessibilityEnabled(context)) {
            editText.setHintTextColor(ContextCompat.getColor(context, accessibilityColor));
        } else {
            editText.setHintTextColor(ContextCompat.getColor(context, defaultColor));
        }
    }
}