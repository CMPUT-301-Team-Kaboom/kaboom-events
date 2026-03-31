package com.example.projecteventlotteryapp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.lang.reflect.Field;
import java.util.*;

public class EntrantEventHistoryTest {

    @Mock CollectionReference eventsCollection;
    @Mock Task<QuerySnapshot> queryTask;
    @Mock QuerySnapshot querySnapshot;
    @Mock QueryDocumentSnapshot documentSnapshot;

    @Mock
    EventUtils eventUtils;
    @Mock
    EventArrayAdapter eventsArrayAdapter;

    private EventsListFragment fragment;

    private ArrayList<Event> eventsArrayList;
    private Map<String, String> eventStatuses;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        fragment = new EventsListFragment();

        eventsArrayList = new ArrayList<>();
        eventStatuses = new HashMap<>();

        // inject needed fields for fragment
        setField("eventsRef", eventsCollection);
        setField("eventUtils", eventUtils);
        setField("eventsArrayList", eventsArrayList);
        setField("eventStatuses", eventStatuses);
        setField("eventsArrayAdapter", eventsArrayAdapter);

        when(eventsCollection.get()).thenReturn(queryTask);
    }

    @Test
    public void getEventsHistoryTest() {
        String userId = "user111";

        // mock event
        Event event = mock(Event.class);
        when(event.getEventId()).thenReturn("event1");

        // mock snapshot data (user is enrolled)
        when(documentSnapshot.get("enrolled")).thenReturn(new ArrayList<>(List.of(userId)));
        when(documentSnapshot.get("declined")).thenReturn(null);
        when(documentSnapshot.get("invited")).thenReturn(null);
        when(documentSnapshot.get("waitlist")).thenReturn(null);

        when(eventUtils.fetchEventFromSnapshot(documentSnapshot)).thenReturn(event);

        // capture success listener (reference: EventCommentsTest.java)
        ArgumentCaptor<OnSuccessListener<QuerySnapshot>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        when(queryTask.addOnSuccessListener(captor.capture())).thenReturn(queryTask);

        // mock query result (not empty, iterate over docs) (reference: https://stackoverflow.com/questions/21436867/mockito-how-to-get-a-mock-object-when-iterate-a-list)
        when(querySnapshot.isEmpty()).thenReturn(false);
        when(querySnapshot.iterator()).thenReturn(List.of(documentSnapshot).iterator());

        // run function to test
        fragment.getEventsHistory(userId);

        // simulate success callback
        captor.getValue().onSuccess(querySnapshot);

        // verify results
        assertEquals(1, eventsArrayList.size());
        assertEquals("enrolled", eventStatuses.get("event1"));

        // (references: Google AI Ovreview for query "mockito verify notify dataset changed")
        verify(eventsArrayAdapter).notifyDataSetChanged();
    }

    @Test
    public void testGetEventsHistory_empty() {
        // capture success listener (reference: EventCommentsTest.java)
        ArgumentCaptor<OnSuccessListener<QuerySnapshot>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        when(queryTask.addOnSuccessListener(captor.capture())).thenReturn(queryTask);

        // mock query result (empty)
        when(querySnapshot.isEmpty()).thenReturn(true);

        // run function to test
        fragment.getEventsHistory("user111");

        // simulate success callback
        captor.getValue().onSuccess(querySnapshot);

        // verify
        assertTrue(eventsArrayList.isEmpty());
        assertTrue(eventStatuses.isEmpty());

        verify(eventsArrayAdapter).notifyDataSetChanged();
    }

    // helper to set private fields (reference: https://stackoverflow.com/a/34658)
    private void setField(String name, Object value) throws Exception {
        Field field = EventsListFragment.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(fragment, value);
    }
}