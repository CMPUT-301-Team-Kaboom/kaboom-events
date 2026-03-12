package com.example.projecteventlotteryapp;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

/*
 Unit test class that functions to test for all four entrant list types: waitlist, invited, accepted, and declined
 */
@ExtendWith(MockitoExtension.class)
public class EventEntrantListTest {
    @Mock FirebaseFirestore db;
    @Mock CollectionReference eventCollection;
    @Mock DocumentReference eventDoc;
    @Mock DocumentSnapshot eventSnapshot;
    EventUtils eventUtils;
    EntrantListType type;
    User user;
    String eventId;

    @Before
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        eventUtils = new EventUtils(db);
        type = EntrantListType.WAITLIST;
        user = new User(Role.ENTRANT, "user123");
        eventId = "event123";

        when(db.collection("events")).thenReturn(eventCollection);
        when(eventCollection.document(anyString())).thenReturn(eventDoc);

        when(eventDoc.update(anyString(), any())).thenReturn(Tasks.forResult(null));
    }

    @Test
    public void entrantListContainsTest(){
        ArrayList<String> entrantList = new ArrayList<>();
        entrantList.add(user.getUserId());

        when(eventDoc.get()).thenReturn(Tasks.forResult(eventSnapshot));
        when(eventSnapshot.exists()).thenReturn(true);
        when(eventSnapshot.get(anyString())).thenReturn(entrantList);

        Task<Boolean> result = eventUtils.entrantListContains(type, user, eventId);

        assertTrue(result.getResult());

        verify(eventDoc).get();
    }

    @Test
    public void joinEntrantList(){
        eventUtils.addToEntrantList(type, user, eventId);

        verify(eventDoc).update(eq("waitlist"), argThat(arg ->
            arg.getClass().getSimpleName().contains("ArrayUnion")));
    }

    @Test
    public void leaveEntrantList(){
        eventUtils.removeFromEntrantList(type, user, eventId);

        verify(eventDoc).update(eq("waitlist"), argThat(arg->
                arg.getClass().getSimpleName().contains("ArrayRemove")));
    }
}
