package com.example.projecteventlotteryapp.dbUtils;

import com.example.projecteventlotteryapp.Enums.Role;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Database utility class to handle all database operations on User classes
 *
 * <p>This class is designed to encapsulated all user database operates. An instance of this
 *  class should be declared with an injection of the database instance that the operations are
 *  performed on.</p>
 *
 * @author Ashley Kang (akang2)
 */
public class UserUtils {
    private FirebaseFirestore db;
    public UserUtils(FirebaseFirestore db){
        this.db = db;
    }

    /**
     * Helper function to retrieve the role string from a Role enum
     *
     * <p>Used for fetching collections from the database</p>
     *
     * @param userRole Role of the user
     * @return String representing the appropriate collection in the database
     * @see Role
     */
    public String getRoleString(Role userRole){
        switch (userRole){
            case ADMIN:
                return "admins";
            case ENTRANT:
                return "entrants";
            case ORGANIZER:
                return "organizers";
            default:
                throw new IllegalArgumentException("Unknown user role: " + userRole);
        }
    }

    /**
     * Retrieves a document snapshot from the database of a specified user
     *
     * @param deviceId document ID of the profile to retrieve
     * @param userRole Role of the user
     * @return a document snapshot wrapped in an async task
     */
    public Task<DocumentSnapshot> loadUserProfile(String deviceId, Role userRole){
        return db.collection(getRoleString(userRole)).document(deviceId).get();
    }

    /**
     * Updates the information of a specified user
     *
     * @param deviceId document ID of the user to update info of
     * @param updates a set of updates to put into the document
     * @param userRole Role of the user
     * @return an async task for the completion of the update
     */
    public Task<Void> updateUserProfile(String deviceId, Map<String, Object> updates, Role userRole){
        return db.collection(getRoleString(userRole)).document(deviceId).update(updates);
    }

    /**
     * Deletes a specified user from the database
     *
     * @param deviceId document ID of the user to delete
     * @param userRole Role of the user
     * @return an async task for the completion of the delete
     */
    public Task<Void> deleteUserProfile(String deviceId, Role userRole){
        return db.collection(getRoleString(userRole)).document(deviceId).delete();
    }

    /**
     * Updates the notification preference of a specified user
     *
     * @param deviceId document ID of the user to update the preference for
     * @param userRole Role of the user
     * @param isEnabled boolean value of the notification preference
     * @return an async task for the completion of the update
     */
    public Task<Void> updateNotificationPreference(String deviceId, Role userRole, boolean isEnabled){
        return db.collection(getRoleString(userRole))
                    .document(deviceId)
                        .update("notificationEnabled", isEnabled);

    }
}
