package com.hoegendevelopments.washingtontrailsapp;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class Charts {
    LineChart lineChart;

    Charts(LineChart lineChart) {
        this.lineChart = lineChart;
    }

    public void drawData(List<Float[]> dataSet) {
        List<Entry> entries = new ArrayList<Entry>();

        for (Float[] data : dataSet) {
            entries.add(new Entry(data[0], data[1]));
        }
        LineDataSet lineDataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        LineData lineData = new LineData(lineDataSet);
        this.lineChart.setData(lineData);
        this.lineChart.invalidate(); // refresh
    }

    public static ArrayList<Float[]> convertData(MapActivity.TrailCoords[] coords) {
        ArrayList<Float[]> data = new ArrayList<>();
        float lastDistance = (float) 0;
        for (int i = 1; i < coords.length; i++) {
            float distance = getDistance(coords[i - 1], coords[i]);
            Float[] dataItem = {distance + lastDistance, (float) i};
            lastDistance = distance;
            data.add(dataItem);
        }
        System.out.println(lastDistance);
        return data;
    }

    public static float getDistance(MapActivity.TrailCoords coords1, MapActivity.TrailCoords coords2) {
        //Get values of our inputs
        double lat1 = coords1.lat;
        double lng1 = coords1.lng;
        double lat2 = coords2.lat;
        double lng2 = coords2.lng;
        // Radius of earth, KM
        final long radiusOfEarth = 6371;
        double lattitudeDifference = lat2 - lat1;
        double dLat = lattitudeDifference * Math.PI / 180;;
        double longitudeDifference = lng2 - lng1;
        double dLon = longitudeDifference * Math.PI / 180;
      /*
      a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
      c = 2 ⋅ atan2( √a, √(1−a) )
      d = R ⋅ c
      where	φ is latitude, λ is longitude, R is earth’s radius
      Assumes that the earth radius is 6,371 KM
      */
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos((lat1 * Math.PI / 180)) * Math.cos((lat2 * Math.PI / 180)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = radiusOfEarth * c;
        float distanceInMiles = (float) 0.62137119 * (float) distance;
        return Math.abs(distanceInMiles * 5280);
    }

}
