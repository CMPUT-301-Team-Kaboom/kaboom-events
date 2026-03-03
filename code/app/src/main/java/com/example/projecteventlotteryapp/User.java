package com.example.projecteventlotteryapp;

public abstract class User {
    String role;

    public User(String role) {
        this.role = role;
    }
    public String getRole() {
        return role;
    }
}
