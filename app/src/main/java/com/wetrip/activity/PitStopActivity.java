package com.wetrip.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wetrip.R;
import com.wetrip.utils.GPSTracker;
import com.wetrip.utils.PlacesFetcher;

public class PitStopActivity extends AppCompatActivity {
    private static final String GOOGLE_API_KEY = "AIzaSyA0cpo5Cakd7-5NmUyiVAOYyshE3my28pQ";
    GoogleMap mMap;
    EditText placeText;
    double latitude = 42.93708;
    double longitude = -75.6107;
    private int PROXIMITY_RADIUS = 5000;
    protected TextView mLocationText;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pit_stop);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context= this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        mLocationText = (TextView) findViewById((R.id.latlongLocation));
//        mLocationText.setText("New York");

//        placeText = (EditText) findViewById(R.id.placeText);
//        Button btnFind = (Button) findViewById(R.id.btnFind);
//
//        btnFind.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Toast.makeText(getApplicationContext(), "finding 2", Toast.LENGTH_LONG).show();
//                searchGooglePlaces();
//            }
//        });

        FloatingActionButton hospital = (FloatingActionButton) findViewById(R.id.hospital);
        hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "finding ", Toast.LENGTH_LONG).show();
                searchGooglePlaces("hospital");
            }
        });

        FloatingActionButton cafe = (FloatingActionButton) findViewById(R.id.cafe);
        cafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "finding ", Toast.LENGTH_LONG).show();
                searchGooglePlaces("cafe,restaurant");
            }
        });

        FloatingActionButton gas = (FloatingActionButton) findViewById(R.id.pertol_pump);
        gas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "finding ", Toast.LENGTH_LONG).show();
                searchGooglePlaces("gas_station");
            }
        });

        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
                // TODO get current location
                GPSTracker gpsTracker = new GPSTracker(getApplicationContext());

                latitude = gpsTracker.getLocation().getLatitude();
                longitude = gpsTracker.getLocation().getLongitude();
                LatLng currentLocation = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are Here"));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(100));
            }
        });
    }

    private void searchGooglePlaces(String type){
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + type);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);

        PlacesFetcher placesRFetcher = new PlacesFetcher();
        Object[] toPass = new Object[3];
        toPass[0] = mMap;
        toPass[1] = googlePlacesUrl.toString();
        toPass[2] = type;
        placesRFetcher.execute(toPass);
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    private void updateLocationOnMap(LatLng location, String marker){
        mMap.addMarker(new MarkerOptions().position(location).title(marker));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
//        mLocationText.setText(" Current Location: " + marker);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}