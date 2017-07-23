package com.hoegendevelopments.washingtontrailsapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    static final String apiURL = "http://washingtontrailfinder.herokuapp.com/";
    static final int LOCATION_PERMISSION = 69;
    static GoogleMap mMap;
    static Polyline path = null;
    static MapActivity currentActivity = null;
    static LineChart lineChart = null;
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
        new Charts(MapActivity.lineChart).drawData(Charts.convertData(trailCoords));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new GetTrails().execute();
        currentActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        CustomFragment fragment = ((CustomFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        fragment.getMapAsync(this);
        final NestedScrollView mScrollView = (NestedScrollView) findViewById(R.id.scroll);
        fragment.setListener(new CustomFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });
        final SearchView searchbar = (SearchView) findViewById(R.id.search_bar);
        final TextView trailname = (TextView) findViewById(R.id.trailname);
        final ListView listView = (ListView) findViewById(R.id.search_suggestions);
        lineChart = (LineChart) findViewById(R.id.chart);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        if (params.getBehavior() == null)
            params.setBehavior(new AppBarLayout.Behavior());
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
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
                if (possibleMatches.size() > 0) {
                    listView.setVisibility(View.VISIBLE);
                } else {
                    listView.setVisibility(View.GONE);
                }
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
        mMap.setPadding(0, 40, 14, 75);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION);
        } else {
            mMap.setMyLocationEnabled(true);
        }
        performSearch("Lena Lake Trail");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationManager manager = (LocationManager) MapActivity.this.getSystemService(Context.LOCATION_SERVICE);
                    Criteria mCriteria = new Criteria();
                    String bestProvider = String.valueOf(manager.getBestProvider(mCriteria, true));

                    Location mLocation = manager.getLastKnownLocation(bestProvider);
                    if (mLocation != null) {
                        final double currentLatitude = mLocation.getLatitude();
                        final double currentLongitude = mLocation.getLongitude();
                        LatLng loc1 = new LatLng(currentLatitude, currentLongitude);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), 15));
                    }
                }
            }
        });
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