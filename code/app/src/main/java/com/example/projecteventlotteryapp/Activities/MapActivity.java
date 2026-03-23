package com.example.projecteventlotteryapp.Activities;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.projecteventlotteryapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String eventId;
    private FirebaseFirestore db;
    private int loadedEntrants = 0;
    private int totalEntrants = 0;
    private int addedMarkers = 0;
    private LatLngBounds.Builder boundsBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        db = FirebaseFirestore.getInstance();
        eventId = getIntent().getStringExtra("eventId");

        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment =
                (SupportMapFragment) fragmentManager.findFragmentById(R.id.fragment_map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        loadWaitlistMarkers();
    }

    private void loadWaitlistMarkers(){
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                   if(!eventDoc.exists()){
                       Log.e("MAP", "Event document does not exist");
                       showDefaultLocation();
                       return;
                   }

                   List<String>  waitlistIds = (List<String>) eventDoc.get("waitlist");

                   if(waitlistIds == null || waitlistIds.isEmpty()){
                       Log.e("MAP", "Waitlist is empty");
                       showDefaultLocation();
                       return;
                   }

                   totalEntrants = waitlistIds.size();
                   loadedEntrants = 0;
                   addedMarkers = 0;
                   boundsBuilder = new LatLngBounds.Builder();

                   for(String entrantId : waitlistIds){
                       loadEntrantLocation(entrantId);

                   }
                }).addOnFailureListener(e -> {
                    Log.e("MAP", "Failed to load event document", e);
                    showDefaultLocation();
                });
    }

    private void loadEntrantLocation(String entrantId){
        db.collection("entrants").document(entrantId).get()
                .addOnSuccessListener(entrantDoc -> {
                    loadedEntrants++;

                    if (entrantDoc.exists()) {
                        addMarkerForEntrant(entrantDoc);
                    }
                    finishLoading();
                }).addOnFailureListener(e -> {
                    loadedEntrants++;
                    Log.e("MAP", "Failed to load entrant document", e);
                    finishLoading();
                });
    }

    private void addMarkerForEntrant(DocumentSnapshot entrantDoc) {
        GeoPoint location = entrantDoc.getGeoPoint("location");
        String name = entrantDoc.getString("name");

        if (location == null) {
            Log.d("MAP", "Entrant has no location");
            return;
        }
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(position).title(name != null ? name : "Entrant"));
        boundsBuilder.include(position);
        addedMarkers++;
    }

    private void finishLoading() {
        if (loadedEntrants == totalEntrants) {
            if (addedMarkers > 0) {
                if (addedMarkers == 1) {
                    // If only one marker, bounds zoom can be weird, so just zoom directly
                    // You could store the last LatLng if you want, but simplest is leave as-is
                    // or set a default close zoom manually in addMarkerForEntrant.
                    try {
                        LatLngBounds bounds = boundsBuilder.build();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 12f));
                    } catch (Exception e) {
                        showDefaultLocation();
                    }
                } else {
                    try {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150));
                    } catch (Exception e) {
                        Log.e("MAP", "Error adjusting camera", e);
                        showDefaultLocation();
                    }
                }
            } else {
                showDefaultLocation();
            }
        }
    }

    private void showDefaultLocation(){
        LatLng defaultLocation = new LatLng(53.5461, -113.4938);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

    }

}