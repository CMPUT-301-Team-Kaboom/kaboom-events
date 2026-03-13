package com.example.projecteventlotteryapp;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import android.content.Context;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

public class AdminOrganizerDeletionTest {
    @Mock
    FirebaseFirestore db;
    @Mock
    CollectionReference organizersRef;
    @Mock
    DocumentReference organizerDoc;
    @Mock
    Context context;
    ArrayList<User> organizers;
    User organizer;
    String userId;
    private OrganizerArrayAdapter adapter;

    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);

        userId = "user123";

        when(db.collection("organizers")).thenReturn(organizersRef);
        when(organizersRef.document(userId)).thenReturn(organizerDoc);

        organizers = new ArrayList<>();

        organizer = new User(Role.ORGANIZER, userId);

        organizers.add(organizer);

        adapter = new OrganizerArrayAdapter(context, organizers, user -> {
            // delete listener
            db.collection("organizers").document(user.getUserId()).delete();
            organizers.remove(user);
            adapter.notifyDataSetChanged();
        }, user -> {});
    }

    @Test
    public void adminDeleteOrganizerTest(){
        adapter.deleteListener.onDeleteClick(organizer);

        verify(db).collection("organizers");
        verify(organizersRef).document(userId);
        verify(organizerDoc).delete();

        assertFalse(organizers.contains(organizer));
    }
}
