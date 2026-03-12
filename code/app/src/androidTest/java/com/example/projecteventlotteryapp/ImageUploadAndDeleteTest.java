package com.example.projecteventlotteryapp;

import org.junit.*;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import android.content.Context;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class ImageUploadAndDeleteTest {
    /*
    upload image
    update image
    delete image with admin
     */
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private String testEventId;

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        testEventId = "test_event";

        ZoneId zoneId = ZoneId.systemDefault();

        // create test event
        Map<String, Object> event = new HashMap<>();

        LocalDate regStart = LocalDate.parse("2027-03-20");
        LocalDate regEnd = LocalDate.parse("2027-03-21");
        LocalDateTime drawDate = LocalDateTime.parse("2027-03-22T12:00:00");

        DocumentReference poster =
                db.collection("posters").document("default_poster");
        event.put("poster", poster);
        event.put("name", "testEvent");
        event.put("waitlistLimit", 20);
        event.put("entrantsLimit", 30);

        event.put("registrationStartDate",
                FirestoreUtils.localDateToTimestamp(regStart, zoneId));
        event.put("registrationEndDate",
                FirestoreUtils.localDateToTimestamp(regEnd, zoneId));
        event.put("drawDate",
                FirestoreUtils.localDateTimeToTimestamp(drawDate, zoneId));

        event.put("organizer", null);
        event.put("description", null);
        event.put("geoLocationEnabled", false);
        event.put("location", null);
        event.put("qrCodePath", null);
        event.put("tags", null);
        event.put("waitlist", new ArrayList<>());
        event.put("enrolled", new ArrayList<>());
        event.put("invited", new ArrayList<>());
        event.put("declined", new ArrayList<>());

        DocumentReference eventDoc = db.collection("events").document(testEventId);
        Tasks.await(eventDoc.set(event));
    }

    @Test
    public void uploadPosterTest() throws ExecutionException, InterruptedException, IOException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        File file = new File(context.getCacheDir(), "test_poster.jpg");

        InputStream inputStream = context.getResources().openRawResource(R.drawable.test_poster);
        FileOutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        Uri fileUri = Uri.fromFile(file);

        Tasks.await(PosterImageHandler.uploadPoster(testEventId, fileUri));

        StorageReference posterStorageRef = storage.getReference("posters/" + testEventId + "_poster.jpg");

        Uri uploadedUri = Tasks.await(posterStorageRef.getDownloadUrl());

        DocumentSnapshot eventRef = Tasks.await(db.collection("events").document(testEventId).get());
        DocumentSnapshot posterRef = Tasks.await(eventRef.getDocumentReference("poster").get());

        assertEquals(posterRef.getString("url"), uploadedUri.toString());
    }

    @After
    public void takeDown() throws ExecutionException, InterruptedException {
        Tasks.await(db.collection("events").document(testEventId).delete());
        Tasks.await(db.collection("posters").document(testEventId + "_poster").delete());
        Tasks.await(storage.getReference("posters/" + testEventId + "_poster.jpg").delete());
    }
}
