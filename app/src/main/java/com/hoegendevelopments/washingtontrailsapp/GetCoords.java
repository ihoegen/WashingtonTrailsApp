package com.hoegendevelopments.washingtontrailsapp;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

class GetCoords extends AsyncTask<String, Void, MapActivity.TrailCoords[]> {

    String lastTrail;
    private Exception exception;

    protected MapActivity.TrailCoords[] doInBackground(String... trailName) {
        try {
            lastTrail = trailName[0];
            URL url = new URL(MapActivity.apiURL + "api/trail/" + URLEncoder.encode(trailName[0], "UTF-8").replaceAll("\\+", "%20"));
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
        MapActivity.drawPaths(trailCoords, lastTrail);
    }
}