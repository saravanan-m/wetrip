package com.wetrip.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wetrip.R;
import com.wetrip.model.Place;

import org.json.JSONObject;
import java.util.List;

public class PlacesDisplayOnMap extends AsyncTask<Object, Integer, List<Place>> {

    JSONObject placesJsonResponse;
    GoogleMap googleMap;
    String type;

    @Override
    protected List<Place> doInBackground(Object... inputObj) {

        List<Place> googlePlacesList = null;
        PlacesJsonParser placeJsonParser = new PlacesJsonParser();

        try {
            googleMap = (GoogleMap) inputObj[0];
            type = (String)inputObj[2];
            placesJsonResponse = new JSONObject((String) inputObj[1]);
            googlePlacesList = placeJsonParser.parse(placesJsonResponse);
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
        return googlePlacesList;
    }

    @Override
    protected void onPostExecute(List<Place> placeList) {
        googleMap.clear();
        if(placeList.size() > 0) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(placeList.get(0).latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        }
        for (int i = 0; i < placeList.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            Place place = placeList.get(i);
            String placeName = place.name;
            String vicinity = place.vicinity;
            LatLng latLng = place.latLng;
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);
            if(type.equalsIgnoreCase("gas_station"))
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_gas));
            else if(type.equalsIgnoreCase("cafe,restaurant"))
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cafe));
            else if(type.equalsIgnoreCase("hospital"))
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital));

            googleMap.addMarker(markerOptions);
        }
    }
}