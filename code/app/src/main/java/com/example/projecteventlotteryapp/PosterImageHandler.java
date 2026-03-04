package com.example.projecteventlotteryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class PosterImageHandler {
    private final FirebaseFirestore db;

    public PosterImageHandler(){
        db = FirebaseFirestore.getInstance();
    }

    public void uploadPoster(String eventFile, Context context, Uri uri){
        /*
        TODO:
        open an image picker, save locally and upload filepath to db, and update
        the document filepath on the event document
         */
        try {
            Bitmap bm = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            String filepath = "poster_" + UUID.randomUUID().toString();
            File file = new File(context.getFilesDir(), filepath);

            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();

            // adding poster filepath to the poster document
            CollectionReference posterRef = db.collection("posters");
            Log.d("Poster", "../" + filepath);
            //posterRef.document(eventFile + "_poster").set("../" + filepath);

            // updating the database filepath in the event page
            CollectionReference eventRef = db.collection("events");
            Log.d("Poster", "/posters/" + eventFile + "_poster");
            //eventRef.document(eventFile).update("poster", "/posters/" + eventFile + "_poster");

        } catch (Exception e){
            e.printStackTrace();
            Log.e("Error", "Error in uploading image.");
        }
    }
}
