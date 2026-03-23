package com.example.projecteventlotteryapp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * References:
 * Tests generated using AI
 * Prompt: Please write me testcases for functions with these descriptions:
 *
     * Adds a comment to the event
     *
     * This function is used to add a new comment to the comments collection in the database, as well as updates the array
     * of comment ID strings in the event. It returns a document reference task so that the comment document ID can be
     * referenced after it's been added.
     *
     * NOTE: the new comment must contain the comment text, the timestamp of the comment, and the ID of the user who
     * made the comment.
     *
     * param eventID ID of the event that the comment is being added onto.
     * param newComment A hashmap object of a new comment, includes the text, timestamp, and user ID of the commenter
     * return a task containing the document reference of the new comment in the comments collection
 *
     * Delete comment from database
     *
     * <p>This method deletes a comment from both the comments collection in the database and
     * the comments array in the events. The comment MUST already exist in both cases</p>
     *
     * param commentId Document ID of the comment to delete
 */
@ExtendWith(MockitoExtension.class)
public class EventCommentsTest {
    @Mock
    FirebaseFirestore db;
    @Mock
    CollectionReference commentsCollection;
    @Mock CollectionReference eventsCollection;
    @Mock
    DocumentReference eventDocRef;
    @Mock DocumentReference commentDocRef;
    @Mock
    Task<DocumentReference> addTask;
    @Mock
    Query query;
    @Mock Task<QuerySnapshot> queryTask;
    @Mock
    QuerySnapshot querySnapshot;
    @Mock
    QueryDocumentSnapshot documentSnapshot;
    @InjectMocks
    EventUtils eventUtils;

    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddCommentToEvent_success() {
        String eventId = "event123";
        String generatedCommentId = "commentABC";
        Map<String, Object> commentData = new HashMap<>();
        eventUtils = new EventUtils(db);

        // Mock Firestore structure
        when(db.collection("comments")).thenReturn(commentsCollection);
        when(db.collection("events")).thenReturn(eventsCollection);
        when(eventsCollection.document(eventId)).thenReturn(eventDocRef);

        when(commentsCollection.add(commentData)).thenReturn(addTask);

        // Capture success listener
        ArgumentCaptor<OnSuccessListener<DocumentReference>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        when(addTask.addOnSuccessListener(captor.capture())).thenReturn(addTask);

        // Mock generated document ID
        when(commentDocRef.getId()).thenReturn(generatedCommentId);

        // Execute
        eventUtils.addCommentToEvent(eventId, commentData);

        // Simulate success callback
        captor.getValue().onSuccess(commentDocRef);

        // Verify update to event document
        verify(eventDocRef).update(eq("comments"), any(FieldValue.class));
    }

    @Test
    public void testDeleteCommentFromEvent_success() {
        String commentId = "comment123";

        // Mock Firestore structure
        when(db.collection("comments")).thenReturn(commentsCollection);
        when(db.collection("events")).thenReturn(eventsCollection);

        when(commentsCollection.document(commentId)).thenReturn(commentDocRef);
        when(eventsCollection.whereArrayContains("comments", commentId)).thenReturn(query);
        when(query.get()).thenReturn(queryTask);

        // Capture success listener
        ArgumentCaptor<OnSuccessListener<QuerySnapshot>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        when(queryTask.addOnSuccessListener(captor.capture())).thenReturn(queryTask);

        // Mock query results
        List<QueryDocumentSnapshot> docs = Collections.singletonList(documentSnapshot);

        when(querySnapshot.iterator()).thenReturn(docs.iterator());
        when(documentSnapshot.getReference()).thenReturn(eventDocRef);

        // Execute
        eventUtils.deleteCommentFromEvent(commentId);

        // Verify deletion of comment document
        verify(commentDocRef).delete();

        // Simulate Firestore success
        captor.getValue().onSuccess(querySnapshot);

        // Verify event update
        verify(eventDocRef).update(eq("comments"), any(FieldValue.class));
    }
}
