package com.example.projecteventlotteryapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsListFragment extends Fragment {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private CollectionReference organizerRef;
    private CollectionReference posterRef;

    private User globalUser;

    private ListView eventsListView;

    private ArrayList<Event> eventsArrayList;
    private ArrayAdapter<Event> eventsArrayAdapter;


    public EventsListFragment() {
        // required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment.
     *
     * @return A new instance of fragment EventsListFragment.
     */
    public static EventsListFragment newInstance() {
        return new EventsListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup db
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        // get user role
        MyApp app = (MyApp) getActivity().getApplication();
        globalUser = app.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events_list, container, false);

        // get ListView
        eventsListView = view.findViewById(R.id.lv_events_list);

        // set up arrays
        eventsArrayList = new ArrayList<Event>();
        /*
        The following code is adapted from...
        Author: user658042
        Title: "Using context in a fragment"
        Answer: https://stackoverflow.com/a/8215398
        Date: 2011-11-12
        Retrieved: 2026-02-28
        License: CC-BY-SA 4.0
        */
        eventsArrayAdapter = new EventArrayAdapter(getActivity(), eventsArrayList);
        eventsListView.setAdapter(eventsArrayAdapter);

        // clickListener for a ListView item
        eventsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Event selectedEvent = eventsArrayList.get(position);

            Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getEventId());

            startActivity(intent);
        });

        /*
        TODO:
            This works fine. As a stretch goal and if we have time, update so there is separate logic
            for entrant/organizer, and change so that organizer does a query on events instead of
            fetching all events and filtering by looking at the organizerRef for each.
         */
        // set listener
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(EventsListFragment.class.getSimpleName() + " Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                eventsArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    Event.fetchEventFromSnapshot(snapshot, event -> {
                        // fetch organizer and poster from DocumentReferences
                    /*
                    The following code is adapted from...
                    Source: https://firebase.google.com/docs/firestore/query-data/get-data
                    Title: "Get data with Cloud Firestore"
                    Retrieved: 2026-03-03
                    */
                        DocumentReference organizerRef = snapshot.getDocumentReference("organizer");
                        if (organizerRef != null) {
                            organizerRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        String organizerId = documentSnapshot.getId();
                                        String organizerName = documentSnapshot.getString("name");
                                        event.setOrganizerId(organizerId);
                                        event.setOrganizerName(organizerName);

                                        // filter (maybe add the above code to Event via fetchEventFromSnapshot later)
                                        if (displayEventForRole(event, globalUser)) {
                                            eventsArrayList.add(event);
                                            eventsArrayAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }

                        String poster = snapshot.getString("poster");
                        event.setPoster(poster);
                    });
                }
                eventsArrayAdapter.notifyDataSetChanged(); // maybe redundant
            }
        });
        return view;
    }

    private boolean displayEventForRole(Event event, User user) {
        if (user.getRole() == Role.ORGANIZER) {
            return (user.getUserId().equals(event.getOrganizerId()));
        } else {
            return true;
        }
    }
}