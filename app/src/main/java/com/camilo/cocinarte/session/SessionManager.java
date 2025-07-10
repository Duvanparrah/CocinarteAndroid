package com.camilo.cocinarte.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {
    private static final String PREF_NAME = "secure_session";
    private static final String TAG = "SessionManager";

    private static SessionManager instance;
    private SharedPreferences prefs;

    private SessionManager(Context context) throws GeneralSecurityException, IOException {
        String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

        prefs = EncryptedSharedPreferences.create(
                PREF_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    public static SessionManager getInstance(Context context) throws GeneralSecurityException, IOException {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    // Guardar sesión básica
    public void saveUserSession(String email, String password, String token) {
        prefs.edit()
                .putString("email", email)
                .putString("password", password)
                .putString("token", token)
                .apply();
    }

    // Guardar sesión completa
    public void saveCompleteUserSession(String email, String password, String token,
                                        String userId, String userName, String userPhoto,
                                        String userType, boolean isVerified) {
        prefs.edit()
                .putString("email", email)
                .putString("password", password)
                .putString("token", token)
                .putString("user_id", userId)
                .putString("user_name", userName)
                .putString("user_photo", userPhoto)
                .putString("user_type", userType)
                .putBoolean("is_verified", isVerified)
                .apply();
    }

    public void saveUser(String email, String password) {
        prefs.edit()
                .putString("email", email)
                .putString("password", password)
                .apply();
    }

    // ✅ MÉTODO AGREGADO: saveAuthToken
    public void saveAuthToken(String token) {
        prefs.edit()
                .putString("token", token)
                .apply();
        Log.d(TAG, "Token guardado exitosamente");
    }

    public String getAuthToken() {
        return prefs.getString("token", null);
    }

    public boolean isLoggedIn() {
        return getAuthToken() != null && getUserId() != -1;
    }

    public boolean isSessionExpired() {
        return false; // puedes implementar lógica de expiración si lo necesitas
    }

    public boolean hasValidToken() {
        return getAuthToken() != null;
    }

    public int getUserId() {
        try {
            return Integer.parseInt(prefs.getString("user_id", "-1"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getEmail() {
        return prefs.getString("email", null);
    }

    public String getUserName() {
        return prefs.getString("user_name", null);
    }

    public String getUserPhoto() {
        return prefs.getString("user_photo", null);
    }

    public String getUserType() {
        return prefs.getString("user_type", null);
    }

    public boolean isUserVerified() {
        return prefs.getBoolean("is_verified", false);
    }

    public SessionData getSessionData() {
        SessionData data = new SessionData();
        data.email = getEmail();
        data.userName = getUserName();
        data.userPhoto = getUserPhoto();
        data.userType = getUserType();
        data.token = getAuthToken();
        data.userId = getUserId();
        data.isVerified = isUserVerified();
        return data;
    }

    public void updateUserName(String newName) {
        prefs.edit().putString("user_name", newName).apply();
    }

    public void updateUserPhoto(String photoUrl) {
        prefs.edit().putString("user_photo", photoUrl).apply();
    }

    // ✅ MÉTODOS ADICIONALES ÚTILES
    public void saveUserInfo(String userId, String userName, String userPhoto,
                             String userType, boolean isVerified) {
        prefs.edit()
                .putString("user_id", userId)
                .putString("user_name", userName)
                .putString("user_photo", userPhoto)
                .putString("user_type", userType)
                .putBoolean("is_verified", isVerified)
                .apply();
        Log.d(TAG, "Información del usuario actualizada");
    }

    public void logout() {
        prefs.edit().clear().apply();
        Log.d(TAG, "Sesión cerrada");
    }

    public static class SessionData {
        public String email;
        public String userName;
        public String userPhoto;
        public String userType;
        public String token;
        public int userId;
        public boolean isVerified;

        @Override
        public String toString() {
            return "SessionData{" +
                    "email='" + email + '\'' +
                    ", userName='" + userName + '\'' +
                    ", userPhoto='" + userPhoto + '\'' +
                    ", userType='" + userType + '\'' +
                    ", token='" + (token != null ? "PRESENTE" : "NULL") + '\'' +
                    ", userId=" + userId +
                    ", isVerified=" + isVerified +
                    '}';
        }
    }
}