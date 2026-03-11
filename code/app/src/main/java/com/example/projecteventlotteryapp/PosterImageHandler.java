package com.example.projecteventlotteryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Static poster handler class that handles poster uploading and, retrieving all posters, and poster deletion
 *
 * @author Ashley Kang (akang2)
 */
public class PosterImageHandler {
    private static FirebaseFirestore db;
    private static CollectionReference posterCollectionRef;
    private static FirebaseStorage storage;
    private static final String STORAGE_DIR = "posters/";
    static {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        posterCollectionRef = db.collection("posters");
    }

    /**
     * Uploads a poster from the user's phone and adds it to both the Firebase photo storage and to the Firebase database
     * under posters and as a document reference in the event that it's a poster for.
     *
     * @param eventId ID of the event that the poster is for, as stored in the database
     * @param uri URI of the image as uploaded from the user's phone
     * @see EditEventActivity calls the function after the activity requests an image from the user's image gallery
     */
    public static void uploadPoster(String eventId, Uri uri){
        /*
        the following code is referenced from https://firebase.google.com/docs/storage/android/upload-files
         */
        String posterFilepath = STORAGE_DIR + eventId + "_poster.jpg";
        StorageReference posterRef = storage.getReference().child(posterFilepath);

        posterRef.putFile(uri).continueWithTask(task -> {
            if(!task.isSuccessful()){
                throw task.getException();
            }
            return posterRef.getDownloadUrl();
        }).addOnSuccessListener(downloadUri -> {
            String downloadUrl = downloadUri.toString();

            DocumentReference eventDoc = db.collection("events").document(eventId);
            eventDoc.update("poster", downloadUrl);
        });
    }

    /**
     * Retrieves all poster documents from the posters collection in the database
     *
     * @param callback  callback to make sure that getting all posters happens in order rather than asynchronously
     * @return          An ArrayList of Image objects that represents all the posters in the database
     */
    public static void getAllPosters(Consumer<ArrayList<String>> callback){
        db.collection("events").get().addOnSuccessListener(snapshot -> {

            ArrayList<String> posters = new ArrayList<>();

            for (DocumentSnapshot doc : snapshot){
                String url = doc.getString("poster");
                if (url.isEmpty()) { continue; }

                posters.add(url);
            }

            callback.accept(posters);
        });
    }

    /**
     * Deletes a poster from the database and storage. Sets the document reference of the event that the
     * poster was deleted from to the default poster document reference
     *
     * @param imageUrl The url of the image being deleted
     */
    public static void deletePoster(String imageUrl){
        DocumentReference defaultPosterRef = posterCollectionRef.document("default_poster");

        db.collection("events")
                        .whereEqualTo("poster", imageUrl).get().addOnSuccessListener(querySnapshot -> {
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()){
                                doc.getReference().update("poster", null);
                            }
                });

        storage.getReferenceFromUrl(imageUrl).delete();
    }
}
