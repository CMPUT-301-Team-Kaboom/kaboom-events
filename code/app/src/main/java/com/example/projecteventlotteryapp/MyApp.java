package com.example.projecteventlotteryapp;

import android.app.Application;
import android.util.Log;

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
