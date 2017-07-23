package com.hoegendevelopments.washingtontrailsapp;

import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GetElevations extends AsyncTask<MapActivity.Coordinate[], Void, Charts.ElevationData> {
    private Exception exception;

    protected Charts.ElevationData doInBackground(MapActivity.Coordinate[]... data) {
        try {
            String asJson = "";
            for (int i = 0; i < data[0].length; i++) {
                MapActivity.Coordinate trailCoord = data[0][i];
                asJson += trailCoord.lat + "," + trailCoord.lng;
                if (i + 1 < data[0].length) {
                    asJson +="|";
                }
            }
            URL url = new URL("https://maps.googleapis.com/maps/api/elevation/json?key=AIzaSyDNLXDH2oviHXFq-42rtWnzi_uuq6ghp7k&locations=" + asJson);
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
                Charts.ElevationData response = gson.fromJson(dta.toString(), Charts.ElevationData.class);
                for (int i = 0; i <  response.results.size(); i++) {
                    System.out.println(response.results.get(i).elevation);
                }
                return response;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    protected void onPostExecute(Charts.ElevationData elevations) {
        if (elevations == null) {
            Toast.makeText(MapActivity.currentActivity, "Connection error ",
                    Toast.LENGTH_SHORT).show();
        }
        new Charts(MapActivity.lineChart).drawData(Charts.convertData(elevations));
    }
}