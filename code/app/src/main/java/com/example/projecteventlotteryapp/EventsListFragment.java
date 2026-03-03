package com.example.projecteventlotteryapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events_list, container, false);

        // get ListView
        eventsListView = view.findViewById(R.id.lv_events_list);

        // set up arrays
        eventsArrayList = new ArrayList<>();
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

        // set listener
        eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(EventsListFragment.class.getSimpleName() + " Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                eventsArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    Event event = fetchEvent(snapshot);
                    eventsArrayList.add(event);

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
                                    String organizerName = documentSnapshot.getString("name");
                                    // event.setOrganizerName(organizerName); if we add organizer to Event
                                }
                            }
                        });
                    }

                    DocumentReference posterRef = snapshot.getDocumentReference("poster");
                    if (posterRef != null) {
                        posterRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String posterPath = documentSnapshot.getString("path");
                                    // something to do with PosterImageHandler
                                }
                            }
                        });
                    }

                }
                eventsArrayAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    private Event fetchEvent(QueryDocumentSnapshot snapshot) {
        // get string fields
        String description = snapshot.getString("description");
        String location = snapshot.getString("location");
        String name = snapshot.getString("name");
        String qrcodePath = snapshot.getString("qrCodePath");

        // get number fields
        int attendeesLimit = fetchInt(snapshot, "entrantsLimit");
        int waitlistLimit = fetchInt(snapshot, "waitlistLimit");

        // get boolean field
        boolean geolocationEnabled = fetchBoolean(snapshot, "geoLocationEnabled");

        // get timestamp fields
        LocalDateTime drawDate = fetchLocalDateTime(snapshot, "drawDate");
        LocalDate registrationEndDate = fetchLocalDate(snapshot, "registrationEndDate");
        LocalDate registrationStartDate = fetchLocalDate(snapshot, "registrationStartDate");

        // get array field
        ArrayList<String> tagsList = fetchStringArrayList(snapshot, "tags");


        Event event = new Event (
                name,
                registrationStartDate,
                registrationEndDate,
                drawDate,
                attendeesLimit
        );

        event.setDescription(description);
        // event.setLocation(location);
        event.setGeolocationEnabled(geolocationEnabled);
        event.setTagsList(tagsList);
        event.setWaitlistLimit(waitlistLimit);

        return event;
    }

    private int fetchInt(QueryDocumentSnapshot snapshot, String field) {
        Long value = snapshot.getLong(field);

        if (value != null) {
            return value.intValue();
        } else {
            return -1;
        }
    }

    private LocalDate fetchLocalDate(QueryDocumentSnapshot snapshot, String field) {
        /*
        The following code is adapted from...
        Author: Ruslan https://stackoverflow.com/users/2032701/ruslan
        Title: "How to convert java.sql.timestamp to LocalDate (java8) java.time?"
        Answer: https://stackoverflow.com/a/57101544
        Date: 2019-07-18
        Retrieved: 2026-02-28
        License: CC-BY-SA 4.0
        */
        Timestamp value = snapshot.getTimestamp(field);

        if (value != null) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

    private LocalDateTime fetchLocalDateTime(QueryDocumentSnapshot snapshot, String field) {
        Timestamp value = snapshot.getTimestamp(field);

        if (value != null) {
            return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else {
            return null;
        }
    }

    private boolean fetchBoolean(QueryDocumentSnapshot snapshot, String field) {
        Boolean value = snapshot.getBoolean(field);

        if (value != null) {
            return value.booleanValue();
        } else {
            return false;
        }
    }

    private ArrayList<String> fetchStringArrayList(QueryDocumentSnapshot snapshot, String field) {
        /*
        The following code is adapted from...
        Author: Doug Stevenson https://stackoverflow.com/users/807126/doug-stevenson
        Title: "How to get an array from Firestore?"
        Answer: https://stackoverflow.com/a/50236950
        Date: 2018-05-08
        Retrieved: 2026-02-28
        License: CC-BY-SA 4.0
        */
        Object value = snapshot.get(field);

        if (value instanceof ArrayList) {
            return (ArrayList<String>) value;
        } else {
            return new ArrayList<String>();
        }
    }

}