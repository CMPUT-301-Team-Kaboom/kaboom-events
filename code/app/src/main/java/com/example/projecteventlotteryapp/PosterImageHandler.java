package com.example.projecteventlotteryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
import java.util.Collection;
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
    private FirebaseFirestore db;
    private CollectionReference posterCollectionRef;
    private FirebaseStorage storage;
    private  final String STORAGE_DIR = "posters/";

    public PosterImageHandler(){
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        posterCollectionRef = db.collection("posters");
    }

    public PosterImageHandler(FirebaseFirestore db, FirebaseStorage storage, CollectionReference posterCollectionRef){
        this.db = db;
        this.storage = storage;
        this.posterCollectionRef = posterCollectionRef;
    }

    /**
     * Uploads a poster from the user's phone and adds it to both the Firebase photo storage and to the Firebase database
     * under posters and as a document reference in the event that it's a poster for.
     *
     * @param eventId ID of the event that the poster is for, as stored in the database
     * @param uri URI of the image as uploaded from the user's phone
     * @see EditEventActivity calls the function after the activity requests an image from the user's image gallery
     */
    public Task<String> uploadPoster(String eventId, Uri uri) {
        // TODO: make this fix better
        if (uri == null) {return Tasks.forResult("empty poster"); }

        StorageReference posterRef = storage.getReference().child(STORAGE_DIR + eventId + "_poster.jpg");

        return posterRef.putFile(uri)
                .continueWithTask(uploadTask -> handleUploadResult(eventId, posterRef));
    }

    protected Task<String> handleUploadResult(String eventId, StorageReference posterRef) throws Exception {
        return posterRef.getDownloadUrl().continueWithTask(downloadTask -> {
            Uri downloadUrl = downloadTask.getResult();
            return updatePosterDocs(eventId, posterRef, downloadUrl);
        });
    }

    private Task<String> updatePosterDocs(String eventId, StorageReference posterRef, Uri downloadUrl) {
        String posterId = eventId + "_poster";

        Map<String, Object> poster = new HashMap<>();
        poster.put("url", downloadUrl.toString());
        poster.put("path", posterRef.getPath());

        DocumentReference eventDoc = db.collection("events").document(eventId);
        DocumentReference posterDoc = posterCollectionRef.document(posterId);

        return eventDoc.update("poster", posterCollectionRef.document(posterId))
                .continueWithTask(updateTask -> posterDoc.set(poster))
                .continueWith(urlTask -> downloadUrl.toString());
    }

    /**
     * Retrieves all poster documents from the posters collection in the database
     *
     * @param callback  callback to make sure that getting all posters happens in order rather than asynchronously
     */
    public void getAllPosters(Consumer<ArrayList<Image>> callback){
        posterCollectionRef.get().addOnSuccessListener(snapshot -> {

            ArrayList<Image> posters = new ArrayList<>();

            for (DocumentSnapshot doc : snapshot.getDocuments()){
                if (doc.getId().equals("default_poster")) { continue; }
                String url = doc.getString("url");

                posters.add(new Image(doc.getId(), url));
            }

            callback.accept(posters);
        });
    }

    /**
     * Deletes a poster from the database and storage. Sets the document reference of the event that the
     * poster was deleted from to the default poster document reference
     *
     * @param image The image being deleted
     */
    public void deletePoster(Image image){
        DocumentReference posterRef = posterCollectionRef.document(image.getImageId());
        DocumentReference defaultPosterRef = posterCollectionRef.document("default_poster");

        db.collection("events")
                .whereEqualTo("poster", posterRef).get().addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()){
                        doc.getReference().update("poster", defaultPosterRef);
                    }
                });

        posterRef.delete();
        storage.getReferenceFromUrl(image.getImageUrl()).delete();
    }
}
