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
 * A simple {@link Fragment} subclass used to display a list of events based off certain conditions.
 *
 * <p>This Fragment class is hosted by the EventsListActivity to display a list of events from the
 * database based on certain conditions (user role and filtering). It is the main menu of entrants
 * and organizers. </p>
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
                    Event event = Event.fetchEventFromSnapshot(snapshot);
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
                                        Log.d("EventList", "Added event: " + event.getEventId() + " for user: " + globalUser.getUserId());
                                        eventsArrayList.add(event);
                                        eventsArrayAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        });
                    }

                    DocumentReference posterRef = snapshot.getDocumentReference("poster");
                    if (posterRef != null){
                        posterRef.get().addOnSuccessListener(doc -> {
                            if (doc.exists()){
                                String imageUrl = doc.getString("url");
                                event.setPoster(imageUrl);
                            }
                        });
                    }
                }
                eventsArrayAdapter.notifyDataSetChanged(); // maybe redundant
            }
        });
        return view;
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