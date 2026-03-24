package com.example.projecteventlotteryapp.Models;

import org.checkerframework.common.returnsreceiver.qual.This;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Model for an Event
 *
 * <p>This class models an event entity stored in Firebase Firestore. It stores
 *  metadata such as registration dates, draw date, attendee limits, organizer
 *  information, and lists of entrants. Instances of this class are used to help display
 *  database information.</p>
 *
 *  TODO: reduce scope of the class and move non-model functionality into a controller class
 */
public class Event {
    private String eventId;
    private String name;
    private int attendeesLimit;
    private int waitlistLimit;
    private int waitlistSize;   // number of users on the waitlist
    private boolean geolocationEnabled;
    private boolean isPrivate;
    private LocalDateTime drawDate;
    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
    private ArrayList<String> tagsList;
    private String description;
    private String organizerId;
    private String organizerName;
    private ArrayList<String> coorganizerIds = new ArrayList<>();
    // private QRCode
    // private location
    // private map
    private String poster;


    /**
     * Creates a new Event object with basic event information.
     * <p> This constructor is typically used when creating a new event before
     * it has been stored in the database.</p>
     *
     * This version has been deprecated. Please update your constructors with the isPrivate method
     *
     * @param name name of the event
     * @param registrationStartDate the date when event registration opens
     * @param registrationEndDate the date when event registration closes
     * @param drawDate the date and time when the lottery draw occurs
     * @param attendeesLimit the maximum number of entrants that can attend the event
     */
    public Event(
            String name,
            LocalDate registrationStartDate,
            LocalDate registrationEndDate,
            LocalDateTime drawDate,
            int attendeesLimit
    ) {
        this.name = name;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.drawDate = drawDate;
        this.attendeesLimit = attendeesLimit;
        isPrivate = false;
    }

    /**
     * Creates a new Event object with basic event information as well as Privacy.
     * <p> This constructor is typically used when creating a new event before
     * it has been stored in the database, this also includes privacy.</p>
     * @param isPrivate boolean value indicating if Event is private
     */
    public Event(
            String name,
            LocalDate registrationStartDate,
            LocalDate registrationEndDate,
            LocalDateTime drawDate,
            int attendeesLimit,
            boolean isPrivate
    ) {
        this.name = name;
        this.registrationStartDate = registrationStartDate;
        this.registrationEndDate = registrationEndDate;
        this.drawDate = drawDate;
        this.attendeesLimit = attendeesLimit;
        this.isPrivate = isPrivate;
    }


    /**
     * Creates an Event object that corresponds to an existing Firestore document.
     *
     * <p>This constructor initializes the event with a known event ID and
     * creates a reference to the associated Firestore document.</p>
     *
     * @param eventId the unique identifier of the event document in Firestore
     * @param name the name of the event
     * @param registrationStartDate the date when event registration opens
     * @param registrationEndDate the date when event registration closes
     * @param drawDate the date and time when the lottery draw occurs
     * @param attendeesLimit the maximum number of entrants that can attend the event
     * @param isPrivate boolean value indicating if Event is private
     */
    public Event(
            String eventId,
            String name,
            LocalDate registrationStartDate,
            LocalDate registrationEndDate,
            LocalDateTime drawDate,
            int attendeesLimit,
            boolean isPrivate
    ) {
        this(name, registrationStartDate, registrationEndDate, drawDate, attendeesLimit, isPrivate);
        this.eventId = eventId;
        //eventDoc = this.db.collection("events").document(this.eventId);
    }

    /**
     * Gets the eventId
     * @return name
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * sets the eventId
     * @param eventId the unique identifier of the event document in Firestore
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name
     * @param name the name of the event
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * gets the attendees limit
     * @return attendeesLimit
     */
    public int getAttendeesLimit() {
        return attendeesLimit;
    }

    /**
     * sets the attendees limit
     * @param attendeesLimit maximum amount of entrants allowed in a event
     */
    public void setAttendeesLimit(int attendeesLimit) {
        this.attendeesLimit = attendeesLimit;
    }

    /**
     * gets the waitlist limit
     * @return waitlistLimit
     */
    public int getWaitlistLimit() {
        return waitlistLimit;
    }

    /**
     * sets the waitlist limit
     * @param waitlistLimit maximum amount of entrants allowed on the waitlist for a event
     */
    public void setWaitlistLimit(int waitlistLimit) {
        this.waitlistLimit = waitlistLimit;
    }

    /**
     * gets the geolocationEnabled
     * @return geolocationEnabled
     */
    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    public int getWaitlistSize() {
        return waitlistSize;
    }

    public void setWaitlistSize(int waitlistSize) {
        this.waitlistSize = waitlistSize;
    }

    /**
     * sets the geolocationEnabled
     * @param geolocationEnabled boolean value of whether geolocation is enabled
     */
    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }

    /**
     * gets the drawDate
     * @return drawDate
     */
    public LocalDateTime getDrawDate() {
        return drawDate;
    }

    /**
     * sets the drawDate
     * @param drawDate the date and time when the lottery draw occurs
     */
    public void setDrawDate(LocalDateTime drawDate) {
        this.drawDate = drawDate;
    }

    /**
     * gets the registrationStartDate
     * @return registrationStartDate
     */
    public LocalDate getRegistrationStartDate() {
        return registrationStartDate;
    }

    /**
     * sets the registrationStartDate
     * @param registrationStartDate LocalDate which is the date when event registration opens
     */
    public void setRegistrationStartDate(LocalDate registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    /**
     * gets the registrationEndDate
     * @return registrationEndDate
     */
    public LocalDate getRegistrationEndDate() {
        return registrationEndDate;
    }

    /**
     * sets the registrationEndDate
     * @param registrationEndDate LocalDate which is the date when event registration closes
     */
    public void setRegistrationEndDate(LocalDate registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    /**
     * gets the tagsList
     * @return tagsList
     */
    public ArrayList<String> getTagsList() {
        return tagsList;
    }

    /**
     * sets the tagsList
     * @param tagsList ArrayList of tags
     */
    public void setTagsList(ArrayList<String> tagsList) {
        this.tagsList = tagsList;
    }

    /**
     * gets the description
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * sets the description
     * @param description String description of the event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * gets the organizerId
     * @return organizerId
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * sets the organizerId
     * @param organizerId the unique identifier of the organizer document in Firestore
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * gets the organizerName
     * @return organizerName
     */
    public String getOrganizerName() {
        return organizerName;
    }

    /**
     * sets the organizerName
     * @param organizerName the name of the organizer
     */
    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    /**
     * gets the poster
     * @param poster the poster of the event
     */
    public void setPoster(String poster) { this.poster = poster; }
    public String getPoster() { return poster; }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean privacy) {
        isPrivate = privacy;
    }

    public ArrayList<String> getCoorganizerIds() {
        return coorganizerIds;
    }

    public void setCoorganizerIds(ArrayList<String> coorganizerIds) {
        this.coorganizerIds = coorganizerIds;
    }
}
