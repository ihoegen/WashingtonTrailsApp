package com.hoegendevelopments.washingtontrailsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    static final String apiURL = "http://washingtontrailfinder.herokuapp.com/";
    static final int LOCATION_PERMISSION = 69;
    static GoogleMap mMap;
    static Polyline path = null;
    static MapActivity currentActivity = null;

    static void drawPaths(TrailCoords[] trailCoords, String lastTrail) {
        if (trailCoords == null) {
            Toast.makeText(MapActivity.currentActivity, "Cannot find trail " + lastTrail,
                    Toast.LENGTH_SHORT).show();
            final TextView trailname = (TextView) MapActivity.currentActivity.findViewById(R.id.trailname);
            trailname.setVisibility(View.GONE);

            return;
        }
        Double maxLat = null, minLat = null, minLon = null, maxLon = null;
        ArrayList<LatLng> coordsList = new ArrayList<>();
        for (MapActivity.TrailCoords trailCoord : trailCoords) {
            // Find out the maximum and minimum latitudes & longitudes
            // Latitude
            maxLat = maxLat != null ? Math.max(trailCoord.lat, maxLat) : trailCoord.lat;
            minLat = minLat != null ? Math.min(trailCoord.lat, minLat) : trailCoord.lat;
            maxLon = maxLon != null ? Math.max(trailCoord.lng, maxLon) : trailCoord.lng;
            minLon = minLon != null ? Math.min(trailCoord.lng, minLon) : trailCoord.lng;
            coordsList.add(new LatLng(trailCoord.lat, trailCoord.lng));
        }

        GoogleMap currentMap = MapActivity.mMap;
        if (MapActivity.path != null) {
            MapActivity.path.remove();
        }
        MapActivity.path = currentMap.addPolyline(new PolylineOptions()
                .addAll(coordsList)
                .width(5)
                .color(Color.BLACK));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(maxLat, maxLon));
        builder.include(new LatLng(minLat, minLon));
        currentMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 48));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new GetTrails().execute();
        currentActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final SearchView searchbar = (SearchView) findViewById(R.id.search_bar);
        final TextView trailname = (TextView) findViewById(R.id.trailname);
        final ListView listView = (ListView) findViewById(R.id.search_suggestions);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = (String) parent.getItemAtPosition(position);
                performSearch(value);
            }
        });
        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                updateCurrentTrail();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<String> possibleMatches = getSuggestions(newText);
                // Get a handle to the list view
                listView.setVisibility(View.VISIBLE);
                // Convert ArrayList to array
                String[] suggestions = Arrays.copyOf(possibleMatches.toArray(), possibleMatches.size(), String[].class);
                listView.setAdapter(new ArrayAdapter<String>(MapActivity.this,
                        android.R.layout.simple_list_item_1, suggestions));
                return false;
            }
        });
        searchbar.clearFocus();
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
        mMap.setPadding(0, 165, 14, 75);
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
        final TextView trailInfo = (TextView) findViewById(R.id.trailname);
        final SearchView searchbar = (SearchView) findViewById(R.id.search_bar);
        final ListView listView = (ListView) findViewById(R.id.search_suggestions);
        searchbar.setQuery("", false);
        searchbar.clearFocus();
        listView.setVisibility(View.GONE);
        trailInfo.setVisibility(View.VISIBLE);
        trailInfo.setText(searchString);
        System.out.println(searchString);
        new GetCoords().execute(searchString);
    }

    void updateCurrentTrail() {
        final SearchView searchbar = (SearchView) findViewById(R.id.search_bar);
        String trailName = searchbar.getQuery().toString();
        performSearch(trailName);
    }

    ArrayList<String> getSuggestions(String query) {
        ArrayList<String> suggestionList = new ArrayList<>();
        if (query.length() == 0) {
            return suggestionList;
        }
        for (String trail : GetTrails.trails) {
            if (trail == null)
                continue;
            if (trail.toLowerCase().contains(query.toLowerCase())) {
                suggestionList.add(trail);
            }
        }
        return suggestionList;
    }

    public class TrailCoords {
        double lat;
        double lng;
    }
}