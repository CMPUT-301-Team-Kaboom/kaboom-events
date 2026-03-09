package com.example.projecteventlotteryapp;

import java.io.Serializable;

public class User implements Serializable {
    private Role role;
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;

    public User(Role role, String userId) {
        this.role = role;
        this.userId = userId;
    }

    public User(Role role, String userId, String name, String email, String phoneNumber) {
        this(role, userId);
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    public Role getRole() {
        return role;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
