package com.hoegendevelopments.washingtontrailsapp;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class RetrieveTrails extends AsyncTask<String, Void, MapActivity.TrailCoords[]> {

    private Exception exception;
    String lastTrail;
    protected MapActivity.TrailCoords[] doInBackground(String... trailName) {
            try {
                lastTrail = trailName[0];
                URL url = new URL(MapActivity.apiURL + "api/trail/" + trailName[0]);
                System.out.println(url);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int statusCode = urlConnection.getResponseCode();
                if (statusCode == 200) {
                    InputStream it = new BufferedInputStream(urlConnection.getInputStream());
                    InputStreamReader read = new InputStreamReader(it);
                    BufferedReader buff = new BufferedReader(read);
                    StringBuilder dta = new StringBuilder();
                    String chunks;
                    while ((chunks = buff.readLine()) != null) {
                        dta.append(chunks);
                    }
                    Gson gson = new GsonBuilder().create();
                    MapActivity.TrailCoords[] response = gson.fromJson(dta.toString(), MapActivity.TrailCoords[].class);
                    return response;
                } else {
                    return null;
                }
            } catch (Exception e) {
                System.out.println(e);
                return null;
        }
    }

    protected void onPostExecute(MapActivity.TrailCoords[] trailCoords) {
        if (trailCoords == null) {
            Toast.makeText(MapActivity.currentActivity, "Cannot find trail " + lastTrail,
                    Toast.LENGTH_SHORT).show();
            final TextView trailname = (TextView) MapActivity.currentActivity.findViewById(R.id.trailname);
            trailname.setVisibility(View.GONE);

            return;
        }
        Double maxLat = null, minLat = null, minLon = null, maxLon = null;
            ArrayList<LatLng> coordsList = new ArrayList<>();
        for (MapActivity.TrailCoords trailCoord: trailCoords) {
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
}