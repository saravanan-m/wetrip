package com.wetrip.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.wetrip.activity.ActiveMap;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationSyncService extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    LocationManager locationManager = null;


    final String serverUri = "tcp://ec2-54-172-101-14.compute-1.amazonaws.com:1883";

    final String clientId = "wetrip-android";
    final String subscriptionTopic = "wetrip-first";
    final String publishTopic = "wetrip-first";
    final String publishMessage = "hii hii";

    MqttAndroidClient mqttclient;

    boolean isSubscribed = false;
    boolean isConnected = false;
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Acquire a reference to the system Location Manager
            // Define a listener that responds to location updates
            // Register the listener with the Location Manager to receive location updates
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }catch (SecurityException e){
                e.printStackTrace();
            }
        }
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if(location!=null) {
                publishMessage(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        setupmqtt();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            locationManager.removeUpdates(locationListener);

            if(mqttclient!=null){
                mqttclient.unsubscribe(subscriptionTopic);
                if(mqttclient.isConnected()) {
                    mqttclient.disconnect();
                }
            }
        }catch (SecurityException e){
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public LocationSyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void setupmqtt(){
        String clientId = MqttClient.generateClientId();
        mqttclient =
                new MqttAndroidClient(this.getApplicationContext(), serverUri,
                        clientId);

        try {
            IMqttToken token = mqttclient.connect();
            mqttclient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    JSONObject jsonObject = new JSONObject(new String(message.getPayload()));

                    Intent intent = new Intent("lat-lng-event");
                    // You can also include some extra data.
                    intent.putExtra("lat",jsonObject.getDouble("lat"));
                    intent.putExtra("lng",jsonObject.getDouble("lng"));
                    intent.putExtra("id",jsonObject.getString("id"));

                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    isConnected = true;
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    isConnected = false;
                }}
            );

        } catch (MqttException e) {
            e.printStackTrace();
        }


    }


    public void subscribe(){
        int qos = 1;
        try {
            IMqttToken subToken = mqttclient.subscribe(subscriptionTopic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    isSubscribed = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                    isSubscribed = false;

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(Location loc){
        try {

            JSONObject jmain = new JSONObject();
            jmain.put("lat",loc.getLatitude());
            jmain.put("lng",loc.getLongitude());
            jmain.put("id", ActiveMap.NAME);

            MqttMessage message = new MqttMessage(jmain.toString().getBytes());
            if(mqttclient !=null && mqttclient.isConnected() ) {
                mqttclient.publish(subscriptionTopic, message);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addToHistory(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
