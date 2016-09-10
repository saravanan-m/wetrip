package com.wetrip.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
 
import java.util.HashMap;
 
/**
 * Created by Ravi on 08/07/15.
 */
public class PrefManager {
    // Shared Preferences
    SharedPreferences pref;
 
    // Editor for Shared preferences
    Editor editor;
 
    // Context
    Context _context;
 
    // Shared pref mode
    int PRIVATE_MODE = 0;
 
    // Shared preferences file name
    private static final String PREF_NAME = "WeTrip";
 
    // All Shared Preferences Keys
    private static final String KEY_IS_WAITING_FOR_SMS = "IsWaitingForSms";
    private static final String KEY_MOBILE_NUMBER = "mobile_number";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_ID = "id";
    private static final String KEY_AUTH_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_NAME = "name";
 
    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
 
    public void setIsWaitingForSms(boolean isWaiting) {
        editor.putBoolean(KEY_IS_WAITING_FOR_SMS, isWaiting);
        editor.commit();
    }
 
    public boolean isWaitingForSms() {
        return pref.getBoolean(KEY_IS_WAITING_FOR_SMS, false);
    }
 
    public void setMobileNumber(String mobileNumber) {
        editor.putString(KEY_MOBILE_NUMBER, mobileNumber);
        editor.commit();
    }
 
    public String getMobileNumber() {
        return pref.getString(KEY_MOBILE_NUMBER, null);
    }
 
    public void createLogin(int id, String name, String email, String mobile,String token) {
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_MOBILE, mobile);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.commit();
    }
 
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
 
    public void clearSession() {
        editor.clear();
        editor.commit();
    }
 
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> profile = new HashMap<>();
        profile.put("id", String.valueOf(pref.getInt(KEY_ID, 0)));
        profile.put("name", pref.getString(KEY_NAME, null));
        profile.put("email", pref.getString(KEY_EMAIL, null));
        profile.put("mobile", pref.getString(KEY_MOBILE, null));
        return profile;
    }

    public String getToken() {
        return pref.getString(KEY_AUTH_TOKEN, null);
    }
}