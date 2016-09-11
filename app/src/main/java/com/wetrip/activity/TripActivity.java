package com.wetrip.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.NoConnectionError;
import com.android.volley.error.TimeoutError;
import com.android.volley.error.VolleyError;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.wetrip.app.WeTripApplication;
import com.wetrip.config.Config;
import com.wetrip.fragment.GalleryFragment;
import com.wetrip.service.ChatHeadService;
import com.wetrip.service.LocationSyncService;
import com.wetrip.R;
import com.wetrip.utils.AppHelper;
import com.wetrip.utils.SharedPrefsUtils;
import com.wetrip.utils.VolleyMultipartRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final LatLng START_POINT = new LatLng(12.971599,77.594563);
    private static final LatLng END_POINT = new LatLng(12.295810, 76.639381);
    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private static final int CUSTOM_OVERLAY_PERMISSION_REQUEST_CODE = 101;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private GoogleMap mMap;

    private Marker mMarkerStart;
    private Marker mMarkerEnd;
    private static final int CAMERA_REQUEST = 1888;
    private HashMap<String,Marker> markerMap = new HashMap<>();
    private Button btnPitStop;
    private Button btnPoke;

    private ViewFlipper flipper;
    float initialX;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        flipper = (ViewFlipper)findViewById(R.id.flipper);
        flipper.setAutoStart(true);
        flipper.setFlipInterval(30000);
        flipper.startFlipping();

        flipper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        float finalX = event.getX();
                        if (initialX > finalX) {
                            if (flipper.getDisplayedChild() == 1)
                                break;

                            flipper.setInAnimation(TripActivity.this, R.anim.in_from_left);
                            flipper.setOutAnimation(TripActivity.this, R.anim.out_to_right);

                            flipper.showNext();
                        } else {
                            if (flipper.getDisplayedChild() == 0)
                                break;

                            flipper.setInAnimation(TripActivity.this, R.anim.in_from_right);
                            flipper.setOutAnimation(TripActivity.this, R.anim.out_to_left);
                            flipper.showPrevious();
                        }
                        break;
                }
                return true;
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar ab = getSupportActionBar();
            ab.setTitle("Bangalore to Mysore");
            ab.setSubtitle("152 Km");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnPitStop = (Button) findViewById(R.id.btn_pit_stop);
        btnPoke = (Button) findViewById(R.id.btn_poke);

        btnPitStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), PitStopActivity.class));
            }
        });

        btnPoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity.this);
                builder.setTitle("Message");
                final EditText input = new EditText(TripActivity.this);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String m_Text = input.getText().toString();

                        Intent intent = new Intent("poke-msg");
                        intent.putExtra("msg", m_Text);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        startService(new Intent(this,LocationSyncService.class));
        IntentFilter filter = new IntentFilter();
        filter.addAction("lat-lng-event");
        filter.addAction("capture-image");
        filter.addAction("alert-bar");

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                filter);


        showChatHead(getApplicationContext(),true);
        showGallery();



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        stopService(new Intent(this,LocationSyncService.class));
    }

    @SuppressLint("NewApi")
    private void showChatHead(Context context, boolean isShowOverlayPermission) {
        // API22以下かチェック
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            context.startService(new Intent(context,ChatHeadService.class));
            return;
        }

        // 他のアプリの上に表示できるかチェック
        if (Settings.canDrawOverlays(context)) {
            context.startService(new Intent(context, ChatHeadService.class));
            return;
        }

        // オーバレイパーミッションの表示
        if (isShowOverlayPermission) {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if(intent.getAction().equals("lat-lng-event")) {
                double lat = intent.getDoubleExtra("lat", -1);
                double lng = intent.getDoubleExtra("lng", -1);
                String id = intent.getStringExtra("id");

                if (id != null && mMap != null) {
                    Marker marker = markerMap.get(id);
                    LatLng loc = new LatLng(lat,
                            lng);
                    if (marker == null) {
                        MarkerOptions options = new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(textAsBitmap(id, 36, Color.GREEN)));
                        marker = mMap.addMarker(options);
                        markerMap.put(id, marker);
                    } else {
                        marker.setPosition(loc);
                    }
                }
            }else if(intent.getAction().equals("alert-bar")){
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                flipper.stopFlipping();
                String dst = intent.getStringExtra("dist");
                String id = intent.getStringExtra("id");

                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = mInflater.inflate(R.layout.item_card, flipper, false);

                TextView heading = (TextView)view.findViewById(R.id.heading);
                TextView sub = (TextView)view.findViewById(R.id.sub_title);

                heading.setText(id);
                sub.setText(dst);

                if(flipper.getChildCount() > 10){
                    flipper.removeViewAt(0);
                }

                flipper.addView(view);

                flipper.setInAnimation(TripActivity.this, R.anim.in_from_left);
                flipper.setOutAnimation(TripActivity.this, R.anim.out_to_right);

                flipper.setDisplayedChild(flipper.getChildCount()-1);
                flipper.startFlipping();
            }
            String msg = intent.getStringExtra("message");
            if(msg != null && msg.equals("capture_image"))
            {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }

        }
    };
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
      /*  LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

       /* MarkerOptions options = new MarkerOptions();
        options.position(LOWER_MANHATTAN);
        //options.position(BROOKLYN_BRIDGE);
        options.position(WALL_STREET);*/

        mMarkerStart = mMap.addMarker(new MarkerOptions()
                .position(START_POINT)
                .title("Start"));
        mMarkerStart.setTag(0);


        mMarkerEnd = mMap.addMarker(new MarkerOptions()
                .position(END_POINT)
                .title("End"));
        mMarkerEnd.setTag(0);


        String url = getDirectionsUrl(START_POINT,END_POINT);
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(START_POINT,
                9));
    }


   /* private String getMapsApiDirectionsUrl() {
        String waypoints = "waypoints=optimize:true|"
                + LOWER_MANHATTAN.latitude + "," + LOWER_MANHATTAN.longitude
                + "|" + "|" + BROOKLYN_BRIDGE.latitude + ","
                + BROOKLYN_BRIDGE.longitude + "|" + WALL_STREET.latitude + ","
                + WALL_STREET.longitude;

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        return url;
    }*/

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        Log.i("map url",url);
        return url;
    }


    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                routes = parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(5);
                polyLineOptions.color(Color.BLUE);
            }

            mMap.addPolyline(polyLineOptions);
        }
    }

    public String readUrl(String mapsApiDirectionsUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(mapsApiDirectionsUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        try {
            jRoutes = jObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps
                                .get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat",
                                    Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng",
                                    Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return routes;
    }

    public Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
       // int width = (int) (paint.measureText(text) + 0.5f); // round
       // int height = (int) (baseline + paint.descent() + 0.5f);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        Bitmap image = Bitmap.createBitmap(bounds.width(), 48, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setAntiAlias(true);

        canvas.drawCircle(bounds.width()/2, bounds.height()/2, bounds.width()/2, circlePaint);
        canvas.drawText(text, 0, baseline, paint);

        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Log.d("Photo","Photo aa gaya?");
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            uploadImage(imageBitmap);
        }
    }



    private void uploadImage(final Bitmap bitmap){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, Config.URL_UPLOAD_FILE, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                loading.hide();
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    JSONArray file = result.getJSONArray("file");
                    if (file != null) {
                        // tell everybody you have succed upload image and post strings
                        Log.i("Messsage", "Upload Successfully.");
                    } else {
                        Log.i("Unexpected", "Somthing Went Wrong.");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loading.hide();
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("trip","1");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("file", new DataPart("image.jpg", AppHelper.getByteArray(getBaseContext(), bitmap), "image/jpeg"));

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("X-AUTH-TOKEN",SharedPrefsUtils.getStringPreference(getApplicationContext(),"token"));
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = WeTripApplication.getInstance().getRequestQueue();

        //Adding request to the queue
        requestQueue.add(multipartRequest);
    }


    public void showGallery()
    {
        GalleryFragment newFragment = new GalleryFragment();
        SupportMapFragment mapFragment = new SupportMapFragment();
        Bundle args = new Bundle();
//        args.putInt(GalleryFragment.ARG_POSITION, position);
//        newFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack so the user can navigate back
        transaction.add(R.id.image_fragment,newFragment);
        transaction.add(R.id.map_fragment,mapFragment);


// Commit the transaction
        transaction.commit();

        mapFragment.getMapAsync(this);

    }
}
