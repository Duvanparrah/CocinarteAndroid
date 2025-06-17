package com.camilo.cocinarte.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.camilo.cocinarte.utils.Constants;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {

    private static final String TAG = "SessionManager";

    // Keys para SharedPreferences
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_SESSION_TIMESTAMP = "session_timestamp";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    // Singleton instance
    private static SessionManager instance;

    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        initializeEncryptedPreferences();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    /**
     * Inicializa SharedPreferences encriptadas
     */
    private void initializeEncryptedPreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    "encrypted_session_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            editor = prefs.edit();

        } catch (GeneralSecurityException | IOException e) {
            // Fallback a SharedPreferences normales si hay error
            android.util.Log.e(TAG, "Error creating encrypted preferences", e);
            prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            editor = prefs.edit();
        }
    }

    /**
     * Guarda la sesión completa del usuario
     */
    public void saveUserSession(String email, String password, String token) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, encodePassword(password)); // Codificar contraseña
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Guarda información adicional del usuario
     */
    public void saveUserInfo(String userId, String userName) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    /**
     * Guarda solo email y contraseña (para registro)
     */
    public void saveUser(String email, String password) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, encodePassword(password));
        editor.apply();
    }

    /**
     * Guarda el token de autenticación
     */
    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Guarda el refresh token
     */
    public void saveRefreshToken(String refreshToken) {
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    /**
     * Obtiene el token de autenticación
     */
    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Obtiene el refresh token
     */
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Obtiene el email del usuario
     */
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    /**
     * Obtiene la contraseña decodificada
     */
    public String getPassword() {
        String encoded = prefs.getString(KEY_PASSWORD, null);
        return encoded != null ? decodePassword(encoded) : null;
    }

    /**
     * Obtiene el ID del usuario
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Obtiene el nombre del usuario
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    /**
     * Verifica si el usuario está logueado
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Verifica si existe un token válido
     */
    public boolean hasValidToken() {
        String token = getAuthToken();
        return token != null && !token.isEmpty();
    }

    /**
     * Verifica si la sesión ha expirado
     */
    public boolean isSessionExpired() {
        long loginTime = prefs.getLong(KEY_SESSION_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - loginTime) > Constants.SESSION_DURATION_MILLIS;
    }

    /**
     * Renueva el timestamp de la sesión
     */
    public void renewSession() {
        editor.putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Actualiza el token
     */
    public void updateToken(String newToken) {
        saveAuthToken(newToken);
    }

    /**
     * Limpia solo el token
     */
    public void clearToken() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.apply();
    }

    /**
     * Cierra la sesión del usuario
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Verifica si existe un usuario registrado
     */
    public boolean isUserExist() {
        return prefs.contains(KEY_EMAIL);
    }

    /**
     * Verifica si las credenciales coinciden con las guardadas
     */
    public boolean isUserRegistered(String email, String password) {
        String savedEmail = getEmail();
        String savedPassword = getPassword();
        return email.equals(savedEmail) && password.equals(savedPassword);
    }

    /**
     * Obtiene todos los datos de la sesión
     */
    public SessionData getSessionData() {
        return new SessionData(
                getEmail(),
                getPassword(),
                getAuthToken(),
                getRefreshToken(),
                getUserId(),
                getUserName(),
                isLoggedIn(),
                !isSessionExpired()
        );
    }

    /**
     * Codifica la contraseña en Base64
     */
    private String encodePassword(String password) {
        if (password == null) return null;
        return Base64.encodeToString(password.getBytes(), Base64.NO_WRAP);
    }

    /**
     * Decodifica la contraseña de Base64
     */
    private String decodePassword(String encoded) {
        if (encoded == null) return null;
        return new String(Base64.decode(encoded, Base64.NO_WRAP));
    }

//    añadidos para traer informaciondel llogin al panel:

    /**
     * Clase para encapsular los datos de sesión
     */
    public static class SessionData {
        public final String email;
        public final String password;
        public final String token;
        public final String refreshToken;
        public final String userId;
        public final String userName;
        public final boolean isLoggedIn;
        public final boolean isSessionValid;

        public SessionData(String email, String password, String token, String refreshToken,
                           String userId, String userName, boolean isLoggedIn, boolean isSessionValid) {
            this.email = email;
            this.password = password;
            this.token = token;
            this.refreshToken = refreshToken;
            this.userId = userId;
            this.userName = userName;
            this.isLoggedIn = isLoggedIn;
            this.isSessionValid = isSessionValid;
        }
    }
}