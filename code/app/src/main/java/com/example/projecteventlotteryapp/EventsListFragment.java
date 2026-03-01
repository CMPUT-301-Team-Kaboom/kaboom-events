package com.example.projecteventlotteryapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                eventsArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    // get string fields
                    String description = snapshot.getString("description");
                    String location = snapshot.getString("location");
                    String name = snapshot.getString("name");
                    String qrcodePath = snapshot.getString("qrCodePath");

                    // get number fields
                    Long attendeesLimitLong = snapshot.getLong("entrantsLimit");
                    int attendeesLimit = attendeesLimitLong.intValue();
                    Long waitlistLimitLong = snapshot.getLong("waitlistLimit");
                    int waitlistLimit = waitlistLimitLong.intValue();

                    // get boolean field
                    boolean geolocationEnabled = snapshot.getBoolean("geoLocationEnabled");

                    // get timestamp fields
                    /*
                    The following code is adapted from...
                    Author: Ruslan https://stackoverflow.com/users/2032701/ruslan
                    Title: "How to convert java.sql.timestamp to LocalDate (java8) java.time?"
                    Answer: https://stackoverflow.com/a/57101544
                    Date: 2019-07-18
                    Retrieved: 2026-02-28
                    License: CC-BY-SA 4.0
                    */
                    Timestamp drawDateTimestamp = snapshot.getTimestamp("drawDate");
                    LocalDateTime drawDate = drawDateTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    Timestamp registrationEndDateTimestamp = snapshot.getTimestamp("drawDate");
                    LocalDate registrationEndDate = registrationEndDateTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    Timestamp registrationStartDateTimestamp = snapshot.getTimestamp("drawDate");
                    LocalDate registrationStartDate = registrationStartDateTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    // get array field
                    /*
                    The following code is adapted from...
                    Author: Doug Stevenson https://stackoverflow.com/users/807126/doug-stevenson
                    Title: "How to get an array from Firestore?"
                    Answer: https://stackoverflow.com/a/50236950
                    Date: 2018-05-08
                    Retrieved: 2026-02-28
                    License: CC-BY-SA 4.0
                    */
                    ArrayList<String> tagsList = (ArrayList<String>) snapshot.get("tags");

                    // get reference fields
                    DocumentReference organizerRef = snapshot.getDocumentReference("organizer");
                    DocumentReference posterRef = snapshot.getDocumentReference("poster");

                    // todo: retrieve references

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

                    eventsArrayList.add(event);
                }
                eventsArrayAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }
}