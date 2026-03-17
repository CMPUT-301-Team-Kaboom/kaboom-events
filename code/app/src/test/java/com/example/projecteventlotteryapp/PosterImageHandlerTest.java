package com.example.projecteventlotteryapp;

import org.junit.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import com.example.projecteventlotteryapp.Models.Image;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@ExtendWith(MockitoExtension.class)
public class PosterImageHandlerTest {
    /*
    upload image
    update image
    delete image with admin
     */
    @Mock FirebaseFirestore mockDb;
    @Mock FirebaseStorage mockStorage;
    @Mock StorageReference mockStorageReference;
    @Mock StorageReference mockPosterRef;
    @Mock CollectionReference mockEventCollection;
    @Mock DocumentReference mockEventDocument;
    @Mock CollectionReference mockPosterCollection;
    @Mock DocumentReference mockPosterDocument;
    @Mock DocumentReference mockDefaultPoster;
    @Mock UploadTask mockUploadTask;
    @Mock DocumentSnapshot mockDocumentSnapshot;
    @Mock Query mockQuery;
    @Mock
    Task<QuerySnapshot> mockQueryTask;
    @Mock QuerySnapshot mockQuerySnapshot;

    PosterImageHandler posterImageHandler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        posterImageHandler = new PosterImageHandler(mockDb, mockStorage, mockPosterCollection);

        when(mockStorage.getReference()).thenReturn(mockStorageReference);
        when(mockStorageReference.child(anyString())).thenReturn(mockPosterRef);

        when(mockDb.collection("events")).thenReturn(mockEventCollection);
        when(mockEventCollection.document(anyString())).thenReturn(mockEventDocument);

        when(mockPosterCollection.document(anyString())).thenReturn(mockPosterDocument);
        when(mockPosterCollection.document(eq("default_poster"))).thenReturn(mockDefaultPoster);
    }

    @Test
    public void uploadPosterTest() throws Exception {

        String eventId = "event123";
        String downloadUrl = "https://example.com/poster.jpg";
        Uri fileUri = mock(Uri.class);

        PosterImageHandler spyHandler =
                spy(new PosterImageHandler(mockDb, mockStorage, mockPosterCollection));

        when(mockStorage.getReference()).thenReturn(mockStorageReference);
        when(mockStorageReference.child(anyString())).thenReturn(mockPosterRef);

        when(mockPosterRef.putFile(fileUri)).thenReturn(mockUploadTask);

        // run the continuation immediately
        when(mockUploadTask.continueWithTask(any())).thenAnswer(invocation -> {
            Continuation<?, Task<String>> continuation = invocation.getArgument(0);
            return continuation.then(null);
        });

        // mock the internal method
        doReturn(Tasks.forResult(downloadUrl))
                .when(spyHandler)
                .handleUploadResult(eq(eventId), any());

        Task<String> resultTask = spyHandler.uploadPoster(eventId, fileUri);

        assertEquals(downloadUrl, resultTask.getResult());

        verify(mockPosterRef).putFile(fileUri);
        verify(spyHandler).handleUploadResult(eq(eventId), any());
    }

    @Test
    public void deletePosterTest(){
        Image image = new Image("", "");

        when(mockEventCollection.whereEqualTo("poster", mockPosterDocument)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQueryTask);
        when(mockQueryTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<QuerySnapshot> listener = invocation.getArgument(0);

            when(mockQuerySnapshot.getDocuments()).thenReturn(Collections.singletonList(mockDocumentSnapshot));

            when(mockDocumentSnapshot.getReference()).thenReturn(mockEventDocument);

            listener.onSuccess(mockQuerySnapshot);
            return mockQueryTask;
        });

        when(mockEventDocument.update("poster", mockDefaultPoster)).thenReturn(Tasks.forResult(null));
        when(mockStorage.getReferenceFromUrl(anyString())).thenReturn(mockPosterRef);

        posterImageHandler.deletePoster(image);

        verify(mockEventDocument).update("poster", mockDefaultPoster);
        verify(mockPosterRef).delete();
        verify(mockPosterDocument).delete();
    }

    @Test
    public void getAllPostersTest(){
        DocumentSnapshot posterSnapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot defaultPosterSnapshot = mock(DocumentSnapshot.class);

        when(mockPosterCollection.get()).thenReturn(mockQueryTask);

        when(mockQueryTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<QuerySnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(mockQuerySnapshot);
            return mockQueryTask;
        });

        when(posterSnapshot.getId()).thenReturn("poster1");
        when(posterSnapshot.getString("url")).thenReturn("url1");

        when(defaultPosterSnapshot.getId()).thenReturn("default_poster");

        List<DocumentSnapshot> docs = Arrays.asList(posterSnapshot, defaultPosterSnapshot);

        when(mockQuerySnapshot.getDocuments()).thenReturn(docs);

        ArrayList<Image> images = new ArrayList<>();

        Consumer<ArrayList<Image>> callback = images::addAll;

        posterImageHandler.getAllPosters(callback);

        assertEquals(1, images.size());
        assertEquals("poster1", images.get(0).getImageId());
        assertEquals("url1", images.get(0).getImageUrl());
    }
}
