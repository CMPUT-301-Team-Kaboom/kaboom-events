package com.example.projecteventlotteryapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.projecteventlotteryapp.Activities.EventDetailsActivity;
import com.example.projecteventlotteryapp.Models.Event;
import com.example.projecteventlotteryapp.Enums.Role;
import com.example.projecteventlotteryapp.Models.EventsFilter;
import com.example.projecteventlotteryapp.Models.MyApp;
import com.example.projecteventlotteryapp.Models.User;
import com.example.projecteventlotteryapp.dbUtils.EventUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private EventUtils eventUtils;
    private User globalUser;
    private ListView eventsListView;
    private ArrayList<Event> eventsArrayList;
    private EventArrayAdapter eventsArrayAdapter;
    private Map<String, String> eventStatuses;
    private EventsFilter currentFilter;

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
        eventUtils = new EventUtils(db);
        eventsRef = db.collection("events");

        // get user role
        MyApp app = (MyApp) getActivity().getApplication();
        globalUser = app.getCurrentUser();

         eventStatuses = new HashMap<>();
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
     * Code Citation:
     *     [1] Author: user658042
     *         Title: "Using context in a fragment"
     *         Answer: https://stackoverflow.com/a/8215398
     *         Date: 2011-11-12
     *         Retrieved: 2026-02-28
     *         License: CC-BY-SA 4.0
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

        // setup ListView and EventArrayAdapter (see citation [1])
        eventsListView = view.findViewById(R.id.lv_events_list);
        eventsArrayList = new ArrayList<Event>();
        eventsArrayAdapter = new EventArrayAdapter(getActivity(), eventsArrayList, eventStatuses);
        eventsListView.setAdapter(eventsArrayAdapter);

        // listener for a ListView event items
        eventsListView.setOnItemClickListener((parent, view1, position, id) -> {
            // don't allow clicking for empty event list
            if (position < 0 || position >= eventsArrayList.size()) {
                return;
            }

            Event selectedEvent = eventsArrayList.get(position);

            Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getEventId());

            startActivity(intent);
        });

        // get events for user role
        getEventsForRole();

        return view;
    }

    /**
     * Fetches events from the database according to user role.
     *
     * Code Citation:
     *     [1] The following code is adapted from...
     *         Author: Alex Mamo https://stackoverflow.com/users/5246885/alex-mamo
     *         Title: "Check if Firestore query is empty"
     *         Answer: https://stackoverflow.com/a/56847476
     *         Date: 2019-07-02
     *         Retrieved: 2026-03-12
     *         License: CC-BY-SA 4.0
     */
    private void getEventsForRole() {
        Query query = eventsRef;

        // filter by role
        if (globalUser.getRole() == Role.ORGANIZER) {
            DocumentReference organizerRef = db.collection("organizers")
                    .document(globalUser.getUserId());
            query = query.whereEqualTo("organizer", organizerRef);
        }

        // get events
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            eventsArrayList.clear();

            // handle empty query (see citation [1])
            if (queryDocumentSnapshots.isEmpty()) {
                eventsArrayAdapter.notifyDataSetChanged();
                return;
            }

            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                Event event = eventUtils.fetchEventFromSnapshot(snapshot);

                // get organizer
                DocumentReference organizerRef = snapshot.getDocumentReference("organizer");
                if (organizerRef != null) {
                    eventUtils.fetchOrganizerForEvent(event, organizerRef)
                            .addOnSuccessListener(aVoid -> {
                                eventsArrayList.add(event);
                                eventsArrayAdapter.notifyDataSetChanged();
                            });
                } else {
                    eventsArrayList.add(event);
                    eventsArrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Make a call to apply filters to a query for a list of events from the database.
     *
     * @param filter
     */
    void applyFilters(EventsFilter filter) {
        // store filter parameters
        currentFilter = filter;

        // get filtered events
        getFilteredEvents();
    }

    /**
     *  Get all events again without filters.
     */
    void clearFilters() {
        currentFilter = null;

        Log.d("EventListFragment", "Cleared Filters");
        getEventsForRole();
    }

    /**
     * Perform the filtering of events fetched from the database.
     *
     * Code Citation:
     *     [1] Title: "Perform simple and compound queries in Cloud Firestore"
     *         Source: https://firebase.google.com/docs/firestore/query-data/
     *         Retrieved: 2026-03-11
     *
     *     [2] Author: Alex Mamo https://stackoverflow.com/users/5246885/alex-mamo
     *         Title: "Check if Firestore query is empty"
     *         Answer: https://stackoverflow.com/a/56847476
     *         Date: 2019-07-02
     *         Retrieved: 2026-03-12
     *         License: CC-BY-SA 4.0
     *
     */
    private void getFilteredEvents() {
        Query query = eventsRef;

        // filter by role (see citation [1])
        if (globalUser.getRole() == Role.ORGANIZER) {
            Log.d("EventsListFragment", "filter organizer: " + globalUser.getUserId());
            DocumentReference organizerRef = db.collection("organizers")
                    .document(globalUser.getUserId());
            query = query.whereEqualTo("organizer", organizerRef);
        }

        // get events
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            eventsArrayList.clear();

            //  handle empty query (see citation [2])
            if (queryDocumentSnapshots.isEmpty()) {
                eventsArrayAdapter.notifyDataSetChanged();
                return;
            }

            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                Event event = eventUtils.fetchEventFromSnapshot(snapshot);
                Log.d("EventsListFragment", "Event: " + event.getName());
                // filter
                if (currentFilter.isMatch(event, snapshot, globalUser.getUserId())) {
                    // get organizer
                    DocumentReference organizerRef = snapshot.getDocumentReference("organizer");
                    if (organizerRef != null) {
                        eventUtils.fetchOrganizerForEvent(event, organizerRef)
                                .addOnSuccessListener(aVoid -> {
                                    eventsArrayList.add(event);
                                    eventsArrayAdapter.notifyDataSetChanged();
                                });
                    } else {
                        eventsArrayList.add(event);
                        eventsArrayAdapter.notifyDataSetChanged();
                    }
                }
            }
            eventsArrayAdapter.setEventStatuses(new HashMap<>());
        });
    }

    /**
     * Fetch events user has attended from the database and status.
     *
     * Code Citation:
     *     [1] Title: "Perform simple and compound queries in Cloud Firestore"
     *         Source: https://firebase.google.com/docs/firestore/query-data/
     *         Retrieved: 2026-03-11
     *
     *     [2] Author: Alex Mamo https://stackoverflow.com/users/5246885/alex-mamo
     *         Title: "Check if Firestore query is empty"
     *         Answer: https://stackoverflow.com/a/56847476
     *         Date: 2019-07-02
     *         Retrieved: 2026-03-12
     *         License: CC-BY-SA 4.0
     *
     */
    public void getEventsHistory(String userId) {
        Query query = eventsRef;

        // get events
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            eventsArrayList.clear();
            eventStatuses.clear();

            //  handle empty query (see citation [2])
            if (queryDocumentSnapshots.isEmpty()) {
                eventsArrayAdapter.notifyDataSetChanged();
                return;
            }

            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                Event event = eventUtils.fetchEventFromSnapshot(snapshot);

                // get status (assume user can only be in one list at a time)
                String status = null;
                if (snapshot.get("enrolled") != null && ((ArrayList<String>) snapshot.get("enrolled")).contains(userId)) {
                    status = "enrolled";
                } else if (snapshot.get("declined") != null && ((ArrayList<String>) snapshot.get("declined")).contains(userId)) {
                    status = "declined";
                } else if (snapshot.get("invited") != null && ((ArrayList<String>) snapshot.get("invited")).contains(userId)) {
                    status = "invited";
                } else if (snapshot.get("waitlist") != null && ((ArrayList<String>) snapshot.get("waitlist")).contains(userId)) {
                    status = "waitlist";
                }

                if (status != null) {
                    eventStatuses.put(event.getEventId(), status);

                    // get organizer
                    DocumentReference organizerRef = snapshot.getDocumentReference("organizer");
                    if (organizerRef != null) {
                        eventUtils.fetchOrganizerForEvent(event, organizerRef)
                                .addOnSuccessListener(aVoid -> {
                                    eventsArrayList.add(event);
                                    eventsArrayAdapter.notifyDataSetChanged();
                                });
                    } else {
                        eventsArrayList.add(event);
                        eventsArrayAdapter.notifyDataSetChanged();
                    }
                }
            }
            eventsArrayAdapter.setEventStatuses(eventStatuses);
        });
    }

    /**
     * Get all events again.
     */
    public void refreshEventList(){
        getEventsForRole();
    }
}