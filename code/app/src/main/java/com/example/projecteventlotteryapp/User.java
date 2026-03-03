package com.example.projecteventlotteryapp;

public abstract class User {
    Role role;

    public User(Role role) {
        this.role = role;
    }
    public Role getRole() {
        return role;
    }
}
