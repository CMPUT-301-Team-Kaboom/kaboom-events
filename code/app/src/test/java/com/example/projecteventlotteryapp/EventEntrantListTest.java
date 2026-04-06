package com.example.projecteventlotteryapp;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.projecteventlotteryapp.Enums.EntrantListType;
import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Map;

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
        when(eventDoc.update(any(Map.class))).thenReturn(Tasks.forResult(null));
    }

    @Test
    public void entrantListContainsTest() throws Exception {
        ArrayList<String> entrantList = new ArrayList<>();
        entrantList.add(user.getUserId());

        Task<DocumentSnapshot> getTask = mock(Task.class);

        when(eventDoc.get()).thenReturn(getTask);
        when(eventSnapshot.exists()).thenReturn(true);
        when(eventSnapshot.get("waitlist")).thenReturn(entrantList);
        when(getTask.isSuccessful()).thenReturn(true);
        when(getTask.getResult()).thenReturn(eventSnapshot);

        when(getTask.continueWith(any())).thenAnswer(invocation -> {
            Continuation<DocumentSnapshot, Boolean> continuation = invocation.getArgument(0);
            Boolean result = continuation.then(getTask);
            return Tasks.forResult(result);
        });

        Task<Boolean> result = eventUtils.entrantListContains(type, user, eventId);

        assertTrue(result.getResult());

        verify(eventDoc).get();
    }

    @Test
    public void joinEntrantList(){
        eventUtils.addToEntrantList(type, user.getUserId(), eventId);

        verify(eventDoc).update(argThat((Map<String, Object> map) ->
            map.containsKey("waitlist") &&
            map.get("waitlist").getClass().getSimpleName().contains("ArrayUnion")
        ));
    }

    @Test
    public void leaveEntrantList(){
        eventUtils.removeFromEntrantList(type, user.getUserId(), eventId);

        verify(eventDoc).update(argThat((Map<String, Object> map) ->
                map.containsKey("waitlist") &&
                        map.get("waitlist").getClass().getSimpleName().contains("ArrayRemove")
        ));
    }

    @Test
    public void getEntrantList_success_returnsList() throws Exception {
        ArrayList<String> entrantList = new ArrayList<>();
        entrantList.add("user1");
        entrantList.add("user2");

        Task<DocumentSnapshot> getTask = mock(Task.class);

        when(eventDoc.get()).thenReturn(getTask);
        when(eventSnapshot.exists()).thenReturn(true);
        when(eventSnapshot.get("waitlist")).thenReturn(entrantList);
        when(getTask.isSuccessful()).thenReturn(true);
        when(getTask.getResult()).thenReturn(eventSnapshot);

        when(getTask.continueWith(any())).thenAnswer(invocation -> {
            Continuation<DocumentSnapshot, ArrayList<String>> continuation = invocation.getArgument(0);
            ArrayList<String> result = continuation.then(getTask);
            return Tasks.forResult(result);
        });

        Task<ArrayList<String>> result = eventUtils.getEntrantList(eventId, type);

        assertEquals(2, result.getResult().size());
        assertTrue(result.getResult().contains("user1"));

        verify(eventDoc).get();
    }

    @Test
    public void getEntrantList_nullField_returnsEmptyList() throws Exception {
        Task<DocumentSnapshot> getTask = mock(Task.class);

        when(eventDoc.get()).thenReturn(getTask);
        when(eventSnapshot.exists()).thenReturn(true);
        when(eventSnapshot.get("waitlist")).thenReturn(null);
        when(getTask.isSuccessful()).thenReturn(true);
        when(getTask.getResult()).thenReturn(eventSnapshot);

        when(getTask.continueWith(any())).thenAnswer(invocation -> {
            Continuation<DocumentSnapshot, ArrayList<String>> continuation = invocation.getArgument(0);
            ArrayList<String> result = continuation.then(getTask);
            return Tasks.forResult(result);
        });

        Task<ArrayList<String>> result = eventUtils.getEntrantList(eventId, type);

        assertNotNull(result.getResult());
        assertTrue(result.getResult().isEmpty());
    }

    @Test
    public void getEntrantList_documentMissing_throwsException() throws Exception {
        Task<DocumentSnapshot> getTask = mock(Task.class);

        when(eventDoc.get()).thenReturn(getTask);
        when(eventSnapshot.exists()).thenReturn(false);
        when(getTask.isSuccessful()).thenReturn(true);
        when(getTask.getResult()).thenReturn(eventSnapshot);

        when(getTask.continueWith(any())).thenAnswer(invocation -> {
            Continuation<DocumentSnapshot, ArrayList<String>> continuation = invocation.getArgument(0);
            return Tasks.forException(new Exception("Event document missing"));
        });

        Task<ArrayList<String>> result = eventUtils.getEntrantList(eventId, type);

        assertTrue(result.isComplete());
        assertFalse(result.isSuccessful());
    }

    @Test
    public void getEntrantList_taskFailure_propagatesException() throws Exception {
        Task<DocumentSnapshot> getTask = mock(Task.class);
        Exception exception = new Exception("Firestore error");

        when(eventDoc.get()).thenReturn(getTask);
        when(getTask.isSuccessful()).thenReturn(false);
        when(getTask.getException()).thenReturn(exception);

        when(getTask.continueWith(any())).thenAnswer(invocation -> {
            return Tasks.forException(exception);
        });

        Task<ArrayList<String>> result = eventUtils.getEntrantList(eventId, type);

        assertTrue(result.isComplete());
        assertFalse(result.isSuccessful());
    }
}
