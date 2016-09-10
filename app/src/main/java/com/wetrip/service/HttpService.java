package com.wetrip.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
 
import com.android.volley.Request;
import com.android.volley.Response;

 
import org.json.JSONException;
import org.json.JSONObject;
 
import java.util.HashMap;
import java.util.Map;

import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.wetrip.activity.MainActivity;
import com.wetrip.app.WeTripApplication;
import com.wetrip.config.Config;
import com.wetrip.utils.PrefManager;
import com.wetrip.utils.SharedPrefsUtils;

/**
 * Created by Rizwan on 10/09/16.
 */
public class HttpService extends IntentService {
 
    private static String TAG = HttpService.class.getSimpleName();
 
    public HttpService() {
        super(HttpService.class.getSimpleName());
    }
 
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String otp = intent.getStringExtra("otp");
            String mobile = SharedPrefsUtils.getStringPreference(this,"mobile");
            verifyOtp(mobile,otp);
        }
    }
 
    /**
     * Posting the OTP to server and activating the user
     *
     * @param otp otp received in the SMS
     */
    private void verifyOtp(final String mobile,final String otp) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.URL_VERIFY_OTP, new Response.Listener<String>() {
 
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());
 
                try {
 
                    JSONObject responseObj = new JSONObject(response);
 
                    // Parsing json object response
                    // response will be a json object
                    JSONObject user = responseObj.getJSONObject("user");

                    if (user != null) {
                        // parsing the user profile information
                        JSONObject profileObj = responseObj.getJSONObject("user");

                        int id = profileObj.getInt("id");
                        String name = profileObj.getString("name");
                        String email = profileObj.getString("email");
                        String mobile = profileObj.getString("mobile");
                        String token = profileObj.getString("token");

                        PrefManager pref = new PrefManager(getApplicationContext());
                        pref.createLogin(id,name, email, mobile,token);
 
                        Intent intent = new Intent(HttpService.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
 
                        Toast.makeText(getApplicationContext(), "Error occurred.", Toast.LENGTH_LONG).show();
 
                    } else {

                        Toast.makeText(getApplicationContext(), "Error occurred.", Toast.LENGTH_LONG).show();
                    }
 
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
 
            }
        }, new Response.ErrorListener() {
 
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
 
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("otp", otp);
                params.put("mobile", mobile);
                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }
 
        };
 
        // Adding request to request queue
        WeTripApplication.getInstance().addToRequestQueue(strReq);
    }
 
}