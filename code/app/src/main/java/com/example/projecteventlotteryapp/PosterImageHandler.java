package com.example.projecteventlotteryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PosterImageHandler {
    private FirebaseFirestore db;
    private static CollectionReference postersRef;
    private static FirebaseStorage storage;
    private static final String POSTER_DIR = "posters";
    private static final String STORAGE_DIR = "posters";
    public PosterImageHandler(){
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        postersRef = db.collection("posters");
    }

    public static void uploadPoster(String eventFile, Context context, Uri uri){
        /*
        TODO:
        open an image picker, save locally and upload filepath to db, and update
        the document filepath on the event document
         */
        try {
            File directory = new File(context.getFilesDir(), POSTER_DIR);
            if (!directory.exists()){
                directory.mkdirs();
            }
            String filepath = "poster_" + UUID.randomUUID().toString();
            File file = new File(directory, filepath);

            Bitmap bm = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 80, out);

            out.close();

            // database connection for when i can connect it
//            String storagePath = STORAGE_DIR + "/" + filepath;
//            StorageReference ref = storage.getReference().child(storagePath);
//
//            ref.putFile(uri).continueWith(task -> ref.getDownloadUrl())
//                    .addOnSuccessListener(downloadUri -> {
//                        Map<String, Object> image = new HashMap<>();
//
//                        image.put("path", storagePath);
//                        image.put("url", downloadUri.toString());
//
//                        postersRef.add(image);
//                    });

        } catch (Exception e){
            e.printStackTrace();
            Log.e("Error", "Error in uploading image.");
        }
    }

    public static ArrayList<Uri> getAllImages(Context context){
        // TODO: database connection when i can connect it
        ArrayList<Uri> uris = new ArrayList<>();
        File dir = new File(context.getFilesDir(), POSTER_DIR);
        if(!dir.exists()) { return uris; }

        File[] files = dir.listFiles();

        if (files != null){
            for (File file: files){
                uris.add(Uri.fromFile(file));
            }
        }

        return uris;
    }

    public static void deleteImage(Uri uri){
        // TODO: database connection for when i can connect it
        File file = new File(uri.getPath());
        if (file.exists()){
            file.delete();
        }
    }
}
