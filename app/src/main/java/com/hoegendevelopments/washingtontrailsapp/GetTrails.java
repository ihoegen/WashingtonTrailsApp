package com.hoegendevelopments.washingtontrailsapp;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class GetTrails extends AsyncTask<String, Void, String[]> {
    static String[] trails = new String[0];
    private Exception exception;

    protected String[] doInBackground(String... data) {
        try {
            URL url = new URL(MapActivity.apiURL + "api/trailnames");
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
                String[] response = gson.fromJson(dta.toString(), String[].class);
                return response;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    protected void onPostExecute(String[] trailNames) {
        if (trailNames == null) {
            Toast.makeText(MapActivity.currentActivity, "Connection error ",
                    Toast.LENGTH_SHORT).show();
            final TextView trailname = (TextView) MapActivity.currentActivity.findViewById(R.id.trailname);
            trailname.setVisibility(View.GONE);
        }
        trails = trailNames;
    }
}