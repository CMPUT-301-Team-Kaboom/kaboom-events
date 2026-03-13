package com.example.projecteventlotteryapp;

import android.app.Application;
import android.util.Log;

/**
 * Application level class used to store global application state.
 *
 * <p>This class extends {@link Application} and provides a centralized location
 * for storing data that should persist across activities during the lifetime
 * of the app process. Currently, it stores the authenticated User
 * object so it can be accessed from any activity.</p>
 *
 */
public class MyApp extends Application {
    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        Log.d("MyApp", String.format("Set currentUser - UserID: %s", currentUser.getUserId()));
    }
}
