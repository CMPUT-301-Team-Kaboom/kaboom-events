package com.example.projecteventlotteryapp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

import android.content.Context;

import com.example.projecteventlotteryapp.Models.Event;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class AdminEventDeletionTest {
    @Mock
    FirebaseFirestore db;
    @Mock
    CollectionReference eventCollection;
    @Mock
    DocumentReference eventDoc;
    @Mock
    Context context;
    @Mock
    Task<Void> deleteTask;
    private ArrayList<Event> eventList;
    private String eventId;
    private Event event;
    private AdminEventArrayAdapter adapter;
    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);

        eventId = "event123";

        when(db.collection("events")).thenReturn(eventCollection);
        when(eventCollection.document(eventId)).thenReturn(eventDoc);

        eventList = new ArrayList<>();

        LocalDate regStart = LocalDate.parse("2026-01-01");
        LocalDate regEnd = LocalDate.parse("2026-01-02");
        LocalDateTime drawDate = LocalDateTime.parse("2026-01-03T12:00:00");

        event = new Event(eventId, "event", regStart, regEnd, drawDate, 1, false);
        eventList.add(event);

        adapter = new AdminEventArrayAdapter(context, eventList, eventDelete ->{
            db.collection("events").document(event.getEventId()).delete().addOnSuccessListener(delete -> {
                eventList.remove(eventDelete);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Test
    public void adminDeleteEventTest(){
        when(eventDoc.delete()).thenReturn(deleteTask);
        ArgumentCaptor<OnSuccessListener<Void>> successCaptor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        when(deleteTask.addOnSuccessListener(successCaptor.capture()))
                .thenReturn(deleteTask);

        adapter.deleteListener.onDeleteClick(event);

        successCaptor.getValue().onSuccess(null);

        verify(db).collection("events");
        verify(eventCollection).document(eventId);
        verify(eventDoc).delete();

        assertFalse(eventList.contains(event));
    }
}
