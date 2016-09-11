package com.wetrip.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

public class PlacesFetcher extends AsyncTask<Object, Integer, String> {
    String googlePlacesData = null;
    GoogleMap googleMap;
    String type;

    @Override
    protected String doInBackground(Object... inputObj) {
        try {
            googleMap = (GoogleMap) inputObj[0];
            type = (String) inputObj[2];
            String googlePlacesUrl = (String) inputObj[1];
            HttpFetcher http = new HttpFetcher();
            googlePlacesData = http.read(googlePlacesUrl);
        } catch (Exception e) {
            Log.d("Google Place Read Task", e.toString());
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        PlacesDisplayOnMap placesDisplayTask = new PlacesDisplayOnMap();
        Object[] toPass = new Object[3];
        toPass[0] = googleMap;
        toPass[1] = result;
        toPass[2] = type;
        placesDisplayTask.execute(toPass);
    }
}