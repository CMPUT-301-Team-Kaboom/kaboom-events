package com.example.projecteventlotteryapp;

import java.io.Serializable;

/**
 * Model for a User
 *
 * <p>This class models a User entity stored in Firebase Firestore. It provides a representation
 * of metadata such as the Role, userId (unique Key in db), name, email and PhoneNumber</p>
 */
public class User implements Serializable {
    private Role role;
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;

    /**
     * Creates a new User object with basic information.
     *
     * @param role the role of the user
     * @param userId the userId of the user
     */
    public User(Role role, String userId) {
        this.role = role;
        this.userId = userId;
    }

    /**
     * Creates a new User object with full information
     *
     * <p>This constructor is typically used when creating a User from a db fetch.</p>
     * @param role the role of the user
     * @param userId the userId of the user
     * @param name the name of the user
     * @param email the email of the user
     * @param phoneNumber the phoneNumber of the user
     */
    public User(Role role, String userId, String name, String email, String phoneNumber) {
        this(role, userId);
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the role of the user
     * @return role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Gets the userId
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * gets the users name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * sets the users name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * gets the users email
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * sets the users email
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * gets the users phone number
     * @return phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    /**
     * sets the users phone number
     * @param phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
