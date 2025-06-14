package com.camilo.cocinarte.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_SESSION_TIMESTAMP = "session_timestamp";

    // ⏰ Duración de sesión en milisegundos (30 minutos)
    private static final long SESSION_DURATION_MILLIS = 30 * 60 * 1000;

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveUser(String email, String password) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }

    public boolean isUserRegistered(String email, String password) {
        String savedEmail = prefs.getString(KEY_EMAIL, null);
        String savedPassword = prefs.getString(KEY_PASSWORD, null);
        return email.equals(savedEmail) && password.equals(savedPassword);
    }

    public boolean isUserExist() {
        return prefs.contains(KEY_EMAIL);
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    public void logout() {
        editor.clear().apply();
    }

    public void saveToken(String token) {
        saveAuthToken(token);
    }

    public String getToken() {
        return getAuthToken();
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, null);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean hasValidToken() {
        String token = getAuthToken();
        return token != null && !token.isEmpty();
    }

    public void clearToken() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.apply();
    }

    public void updateToken(String newToken) {
        saveAuthToken(newToken);
    }

    public void saveUserSession(String email, String password, String token) {
        long timestamp = System.currentTimeMillis(); // ⏱ Guardar hora actual

        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_SESSION_TIMESTAMP, timestamp);
        editor.apply();
    }

    public boolean isSessionExpired() {
        long loginTime = prefs.getLong(KEY_SESSION_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - loginTime) > SESSION_DURATION_MILLIS;
    }

    public SessionData getSessionData() {
        return new SessionData(
                getEmail(),
                getPassword(),
                getAuthToken(),
                isLoggedIn()
        );
    }

    public static class SessionData {
        public final String email;
        public final String password;
        public final String token;
        public final boolean isLoggedIn;

        public SessionData(String email, String password, String token, boolean isLoggedIn) {
            this.email = email;
            this.password = password;
            this.token = token;
            this.isLoggedIn = isLoggedIn;
        }
    }
}
