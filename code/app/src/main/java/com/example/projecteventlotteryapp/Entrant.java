package com.example.projecteventlotteryapp;

public class Entrant {
    private String name;
    private String email;
    private String phoneNumber;
    private String deviceId;

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

    public String getDeviceId() {return deviceId;}

    public void setDeviceId(String deviceId) {this.deviceId = deviceId; }
}
