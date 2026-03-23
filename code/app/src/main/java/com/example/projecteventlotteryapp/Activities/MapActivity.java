package com.example.projecteventlotteryapp.Activities;

import android.location.Location;
import android.os.Bundle;

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
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String eventId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

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

        LatLng edmonton = new LatLng(53.5461, -113.4938);
        //LatLng mylocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(edmonton).title("Edmonton"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, 10f));
    }
}