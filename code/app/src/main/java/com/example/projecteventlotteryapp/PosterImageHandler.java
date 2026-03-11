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

public class PosterImageHandler {
    private static FirebaseFirestore db;
    private static CollectionReference posterCollectionRef;
    private static FirebaseStorage storage;
    private static final String POSTER_DIR = "posters";
    private static final String STORAGE_DIR = "posters/";
    public PosterImageHandler(){
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        posterCollectionRef = db.collection("posters");
    }

    public interface PosterUploadListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    private static PosterUploadListener listener;

    public static void uploadPoster(String eventId, Context context, Uri uri){
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

            Map<String, Object> poster = new HashMap<>();

            poster.put("url", downloadUrl);
            poster.put("path", posterFilepath);

            posterCollectionRef.add(poster);
        });
    }

    public static ArrayList<Uri> getAllPosters(Consumer<ArrayList<Image>> callback){
        posterCollectionRef.get().addOnSuccessListener(snapshot -> {

            ArrayList<Image> posters = new ArrayList<>();

            for (DocumentSnapshot doc : snapshot){
                String url = doc.getString("url");

                posters.add(new Image(doc.getId(), url));
            }

            callback.accept(posters);
        });
    }

    public static void deletePoster(Image image){
        posterCollectionRef.document(image.getImageId()).delete();

        storage.getReferenceFromUrl(image.getImageUrl()).delete();
    }
}
