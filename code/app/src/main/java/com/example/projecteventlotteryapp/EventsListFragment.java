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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass used to display a list of events based off certain conditions.
 *
 * <p>This Fragment class is hosted by the EventsListActivity to display a list of events from the
 * database based on certain conditions (user role and filtering). It is the main menu of entrants
 * and organizers. </p>
 */
public class EventsListFragment extends Fragment {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private User globalUser;

    private ListView eventsListView;

    private ArrayList<Event> eventsArrayList;
    private ArrayAdapter<Event> eventsArrayAdapter;

    // filters
    private String filterName;
    private String filterStatus;
    private ArrayList<String> filterTags;
    private LocalDate filterRegStart;
    private LocalDate filterRegEnd;
    private LocalDate filterDrawDate;

    public EventsListFragment() {
        // required empty public constructor
    }

    public static EventsListFragment newInstance() {
        return new EventsListFragment();
    }

    /**
     * Entry point of the Fragment.
     *
     * <p>This function is the entry point of the Fragment. It sets up the db instance and gets the
     * user role.</p>
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
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

    /**
     * Fetches events from the database and configures the display for a given user depending on
     * their role.
     *
     * <p>If the user is an organizer, events specific to this organizer are displayed. Other events
     * are not retrieved</p>
     *
     * <p>If the user is an entrant, all events are displayed.</p>
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return View to be displayed
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events_list, container, false);

        // setup ListView and EventArrayAdapter
        eventsListView = view.findViewById(R.id.lv_events_list);
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

        // listener for a ListView event items
        eventsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Event selectedEvent = eventsArrayList.get(position);

            Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getEventId());

            startActivity(intent);
        });

        // get events for user role
        getEventsForRole();

        return view;
    }

    /*
        TODO:
            This works fine. As a stretch goal and if we have time, update so there is separate logic
            for entrant/organizer, and change so that organizer does a query on events instead of
            fetching all events and filtering by looking at the organizerRef for each.
    */
    private void getEventsForRole() {
        Query query = eventsRef;

        // filter by role
        if (globalUser.getRole() == Role.ORGANIZER) {
            query = query.whereEqualTo("organizer", globalUser.getUserId());
        }

        // get events
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            eventsArrayList.clear();

            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                Event event = Event.fetchEventFromSnapshot(snapshot);
                eventsArrayList.add(event);
            }
            eventsArrayAdapter.notifyDataSetChanged();
        });
    }

    void applyFilters(String name, String status, ArrayList<String> tags, LocalDate regStart, LocalDate regEnd, LocalDate drawDate) {
        // Store filter parameters
        filterName = name;
        filterStatus = status;
        filterTags = tags;
        filterRegStart = regStart;
        filterRegEnd = regEnd;
        filterDrawDate = drawDate;

        // get filtered events
        getFilteredEvents();
    }

    /*
    The following code is adapted from...
    Title: "Perform simple and compound queries in Cloud Firestore"
    Source: https://firebase.google.com/docs/firestore/query-data/
    Retrieved: 2026-03-11
    */
    private void getFilteredEvents() {
        Query query = eventsRef;
        Log.d("EventsListFragment", "check query: " + eventsRef.getClass());

        // check name filter
        if (filterName != null) {
            Log.d("EventsListFragment", "in check name: " + filterName);
            query = query.whereEqualTo("name", filterName);
        }

        /*
        todo: status not sure how this works yet
        if (filterStatus != null) {
            query = query.whereEqualTo("name", filterStatus);
        }
        */

        // check tags filter
        if (filterTags != null) {
            Log.d("EventsListFragment", "in check tags: " + filterTags);
            query = query.whereArrayContainsAny("tags", filterTags);
        }

        // check registration date filter
        if (filterRegStart != null && filterRegEnd != null) {
            Log.d("EventsListFragment", "in check registration: " + filterRegStart + "\n" + filterRegEnd);
            query = query.whereGreaterThanOrEqualTo("registrationStartDate", filterRegStart)
                    .whereLessThanOrEqualTo("registrationEndDate", filterRegEnd);
        } else if (filterRegStart != null) {
            Log.d("EventsListFragment", "in check registration: " + filterRegStart);
            query = query.whereGreaterThanOrEqualTo("registrationStartDate", filterRegStart);
        } else if (filterRegEnd != null) {
            Log.d("EventsListFragment", "in check registration: " + filterRegEnd);
            query = query.whereLessThanOrEqualTo("registrationEndDate", filterRegEnd);
        }

        // check draw date filter
        if (filterDrawDate != null) {
            Log.d("EventsListFragment", "in check draw: " + filterDrawDate);
            query = query.whereEqualTo("drawDate", filterDrawDate);
        }

        // do filtered query
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            eventsArrayList.clear();
            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                Event event = Event.fetchEventFromSnapshot(snapshot);
                if (displayEventForRole(event, globalUser)) {
                    eventsArrayList.add(event);
                }
            }
            eventsArrayAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Helper to determine whether or not display an event based on user role and ID.
     *
     * @param event to check
     * @param user to get role and ID of
     * @return boolean of whether or not this event should be displayed in the context
     */
    private boolean displayEventForRole(Event event, User user) {
        if (user.getRole() == Role.ORGANIZER) {
            Log.d("EventList", "Organizer: " + user.getUserId());
            return (user.getUserId().equals(event.getOrganizerId()));
        } else {
            Log.d("EventList", "User: " + user.getUserId());
            return true;
        }
    }
}