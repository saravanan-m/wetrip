package com.wetrip.config;

public class Config {
    // server URL configuration
    public static final String SERVER_URL = "http://ec2-54-172-101-14.compute-1.amazonaws.com/api/";
    public static final String URL_REQUEST_SMS = SERVER_URL+"sendotp";
    public static final String URL_VERIFY_OTP = SERVER_URL+"login";

    public static final String OTP_DELIMITER = "-";
}