package com.hoegendevelopments.washingtontrailsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.net.*;
import java.io.*;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    static final String apiURL = "http://washingtontrailfinder.herokuapp.com/";
    static GoogleMap mMap;
    static final int LOCATION_PERMISSION = 69;
    static Polyline path = null;
    static MapActivity currentActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final Button search = (Button) findViewById(R.id.search);
        final EditText searchbar = (EditText) findViewById(R.id.search_bar);
        final TextView trailname = (TextView) findViewById(R.id.trailname);
        final Button clear = (Button) findViewById(R.id.clear);
        searchbar.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER) && searchbar.getText().length() > 0) {
                    updateCurrentTrail();
                    return true;
                }
                return false;
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchbar.getText().length() > 0) {
                    updateCurrentTrail();
                }
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear.setVisibility(View.GONE);
                trailname.setVisibility(View.GONE);
                searchbar.setText("");
                if (path!=null)
                    path.remove();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 125, 10, 0);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION);
        } else {
            mMap.setMyLocationEnabled(true);
        }
        performSearch("Lena Lake Trail");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                }
        }
    }
    void performSearch(String searchString) {
        System.out.println(searchString);
        new RetrieveTrails().execute(searchString);
    }

    void updateCurrentTrail() {
        final Button search = (Button) findViewById(R.id.search);
        final EditText searchbar = (EditText) findViewById(R.id.search_bar);
        final TextView trailname = (TextView) findViewById(R.id.trailname);
        final Button clear = (Button) findViewById(R.id.clear);
        String trailName = searchbar.getText().toString();
        trailname.setVisibility(View.VISIBLE);
        trailname.setText(trailName);
        performSearch(trailName);
        clear.setVisibility(View.VISIBLE);
    }

    public class TrailCoords {
        double lat;
        double lng;
    }
}