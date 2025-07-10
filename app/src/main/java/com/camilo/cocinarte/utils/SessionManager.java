package com.camilo.cocinarte.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "CocinarteSession";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * 🔐 Guardar token de autenticación
     */
    public void saveAuthToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
        Log.d(TAG, "✅ Token guardado");
    }

    /**
     * 🔑 Obtener token de autenticación
     */
    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    /**
     * 👤 Guardar información del usuario
     */
    public void saveUserData(int userId, String email, String name) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
        Log.d(TAG, "✅ Datos de usuario guardados");
    }

    /**
     * 🔍 Verificar si el usuario está logueado
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false) && getToken() != null;
    }

    /**
     * 👤 Obtener ID del usuario
     */
    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    /**
     * 📧 Obtener email del usuario
     */
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * 👤 Obtener nombre del usuario
     */
    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, null);
    }

    /**
     * 🔓 Cerrar sesión
     */
    public void logout() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "✅ Sesión cerrada");
    }

    /**
     * 🔄 Actualizar token
     */
    public void updateToken(String newToken) {
        saveAuthToken(newToken);
    }
}