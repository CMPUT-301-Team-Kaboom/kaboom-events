package com.example.projecteventlotteryapp;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

public class AdminEntrantDeletionTest {
    @Mock
    FirebaseFirestore db;
    @Mock
    CollectionReference entrantRef;
    @Mock
    DocumentReference entrantDoc;
    @Mock
    Context context;
    ArrayList<User> entrants;
    User entrant;
    String entrantId;
    private ProfileArrayAdapter adapter;

    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);

        entrantId = "user123";

        when(db.collection("entrants")).thenReturn(entrantRef);
        when(entrantRef.document(entrantId)).thenReturn(entrantDoc);

        entrants = new ArrayList<>();

        entrant = new User(Role.ORGANIZER, entrantId);

        entrants.add(entrant);

        adapter = new ProfileArrayAdapter(context, entrants, user -> {
            db.collection("entrants").document(user.getUserId()).delete();
            entrants.remove(user);
            adapter.notifyDataSetChanged();
        });
    }

    @Test
    public void adminDeleteEntrantTest(){
        adapter.deleteListener.onDeleteClick(entrant);

        verify(db).collection("entrants");
        verify(entrantRef).document(entrantId);
        verify(entrantDoc).delete();

        assertFalse(entrants.contains(entrant));
    }
}
