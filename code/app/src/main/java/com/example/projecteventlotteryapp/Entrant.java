package com.example.projecteventlotteryapp;

public class Entrant extends User {
    private String name;
    private String email;
    private String phoneNumber;

    public Entrant(String name, String email, String phoneNumber, String role) {
        super(role);
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
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
