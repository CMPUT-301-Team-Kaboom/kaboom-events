package com.example.projecteventlotteryapp;

import android.app.Application;

public class MyApp extends Application {
    private User curentUser;

    public User getCurentUser() {
        return curentUser;
    }

    public void setCurentUser(User curentUser) {
        this.curentUser = curentUser;
    }
}
