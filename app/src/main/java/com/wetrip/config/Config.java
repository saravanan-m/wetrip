package com.wetrip.config;

public class Config {
    // server URL configuration
    public static final String SERVER_URL = "http://ec2-54-172-101-14.compute-1.amazonaws.com/api/";
    public static final String URL_REQUEST_SMS = SERVER_URL+"sendotp";
    public static final String URL_VERIFY_OTP = SERVER_URL+"login";
    public static final String URL_UPLOAD_FILE = SERVER_URL+"trip/uploadfile";
    public static final String URL_TRIP_PHOTO = SERVER_URL+"trip/gettrip?trip=1";

    public static final String OTP_DELIMITER = "-";
}