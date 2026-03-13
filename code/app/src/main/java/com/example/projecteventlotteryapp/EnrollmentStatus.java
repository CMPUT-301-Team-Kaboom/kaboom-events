package com.example.projecteventlotteryapp;

/**
 * Enum to be used exclusively for filtering.
 * <p>This is NOT the same as the EntrantListType enum. The reason it exists is because I needed
 * another way of defining enrollment status that did not touch entrantListType.
 * I hate this and am open to other ways to achieve this</p>
 *
 * If you find another use for this ask Ben before using cause I think this truly unstable
 */
public enum EnrollmentStatus {
    ENROLLED,
    DECLINED,
    INVITED,
    ON_WAITLIST,
    NOT_ON_WAITLIST
}
