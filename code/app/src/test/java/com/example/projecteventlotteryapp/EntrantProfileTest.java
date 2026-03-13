package com.example.projecteventlotteryapp;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class EntrantProfileTest {
    @Mock
    FirebaseFirestore db;
    @Mock
    CollectionReference collectionReference;
    @Mock
    DocumentReference documentReference;
    @InjectMocks FirebaseDB service;

    String deviceID;
    Role userRole;

    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);

        service = new FirebaseDB(db);

        deviceID = "device123";
        userRole = Role.ENTRANT;

        when(db.collection(service.getRoleString(userRole))).thenReturn(collectionReference);
        when(collectionReference.document(deviceID)).thenReturn(documentReference);
    }

    @Test
    public void getRoleStringTest(){
        String collection = service.getRoleString(userRole);

        assertEquals("entrants", collection);
    }

    @Test
    public void loadProfileFromDeviceIDTest(){
        service.loadUserProfile(deviceID, userRole);

        verify(documentReference).get();
    }

    @Test
    public void updateProfileTest(){
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", "Test");
        updates.put("email", "test@email.com");
        updates.put("phone", "555-555-5555");

        service.updateUserProfile(deviceID, updates, userRole);

        verify(documentReference).update(updates);
    }

    @Test
    public void deleteProfileTest(){
        service.deleteUserProfile(deviceID, userRole);

        verify(documentReference).delete();
    }
}
