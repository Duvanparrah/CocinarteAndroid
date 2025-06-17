package com.camilo.cocinarte.utils;

public class Constants {

    // API Configuration
    public static final String BASE_URL = "http://192.168.18.7:3000/api/";
    public static final int NETWORK_TIMEOUT = 30; // segundos

    // SharedPreferences Keys
    public static final String PREF_NAME = "cocinarte_prefs";
    public static final String KEY_USER_TOKEN = "user_token";
    public static final String KEY_USER_EMAIL = "user_email";

    // Navigation
    public static final String NAV_LOGIN = "login";
    public static final String NAV_MAIN = "main";
    public static final String NAV_REGISTER = "register";

    // Session
    public static final long SESSION_DURATION_MILLIS = 30 * 60 * 1000; // 30 minutos

    // Request Codes
    public static final int REQUEST_CODE_LOGIN = 1001;
    public static final int REQUEST_CODE_REGISTER = 1002;

    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int RESET_CODE_LENGTH = 6;
}