package com.camilo.cocinarte.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Método original - guardar usuario y contraseña
    public void saveUser(String email, String password) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // Método original - verificar si el usuario está registrado
    public boolean isUserRegistered(String email, String password) {
        String savedEmail = prefs.getString(KEY_EMAIL, null);
        String savedPassword = prefs.getString(KEY_PASSWORD, null);
        return email.equals(savedEmail) && password.equals(savedPassword);
    }

    // Método original - verificar si existe un usuario
    public boolean isUserExist() {
        return prefs.contains(KEY_EMAIL);
    }

    // Método original - guardar token de autenticación
    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    // Método original - obtener token de autenticación
    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    // Método original - cerrar sesión
    public void logout() {
        editor.clear().apply();
    }

    // ✅ MÉTODOS ADICIONALES ÚTILES

    // Alias para compatibilidad con tu código de registro
    public void saveToken(String token) {
        saveAuthToken(token);
    }

    // Alias para obtener token
    public String getToken() {
        return getAuthToken();
    }

    // Obtener email del usuario actual
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // Obtener contraseña (no recomendado en producción)
    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, null);
    }

    // Verificar si el usuario está logueado
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Verificar si hay un token válido
    public boolean hasValidToken() {
        String token = getAuthToken();
        return token != null && !token.isEmpty();
    }

    // Limpiar solo el token (mantener usuario)
    public void clearToken() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.apply();
    }

    // Actualizar solo el token
    public void updateToken(String newToken) {
        saveAuthToken(newToken);
    }

    // Guardar sesión completa (usuario + token)
    public void saveUserSession(String email, String password, String token) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // Obtener todos los datos de la sesión
    public SessionData getSessionData() {
        return new SessionData(
                getEmail(),
                getPassword(),
                getAuthToken(),
                isLoggedIn()
        );
    }

    // Clase interna para encapsular los datos de sesión
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