package com.camilo.cocinarte.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.models.Usuario;
import com.camilo.cocinarte.session.SessionManager;

public class LoginManager {
    private static final String TAG = "LoginManager";
    private SharedPreferences prefs;

    public LoginManager(Context context) {
        prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    // ✅ NUEVO: Guardar token y refresh token
    public void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", accessToken);
        editor.putString("refresh_token", refreshToken);
        editor.putLong("token_timestamp", System.currentTimeMillis());
        editor.apply();
        Log.d(TAG, "✅ Tokens guardados exitosamente");
    }

    // ✅ MANTENER compatibilidad con método anterior
    public void saveToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", token);
        editor.putLong("token_timestamp", System.currentTimeMillis()); // ✅ AGREGAR ESTA LÍNEA
        editor.apply();
        Log.d(TAG, "Token guardado exitosamente");
    }

    // ✅ NUEVO: Obtener refresh token
    public String getRefreshToken() {
        String refreshToken = prefs.getString("refresh_token", null);
        Log.d(TAG, "Refresh token: " + (refreshToken != null ? "presente" : "ausente"));
        return refreshToken;
    }

    // ✅ NUEVO: Verificar si el token está expirado (aproximadamente)
    public boolean isTokenExpired() {
        long tokenTimestamp = prefs.getLong("token_timestamp", 0);
        if (tokenTimestamp == 0) {
            Log.d(TAG, "No hay timestamp del token");
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long tokenAge = currentTime - tokenTimestamp;
        long sevenDays = 7 * 24 * 60 * 60 * 1000L; // ✅ CAMBIAR A 7 DÍAS

        boolean expired = tokenAge > sevenDays;
        Log.d(TAG, "Token expirado: " + expired + " (edad: " + (tokenAge / 1000 / 60 / 60) + " horas)");
        return expired;
    }

    // ✅ MÉTODO CORREGIDO: Guardar datos del usuario con campos consistentes
    public void saveUser(LoginResponse.UserData usuario) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("id_usuario", String.valueOf(usuario.getId()));
        editor.putString("correo", usuario.getEmail()); // ✅ Guardar email como "correo"
        editor.putString("tipo_usuario", usuario.getTipo_usuario());
        editor.putString("nombre_usuario", usuario.getNombre()); // ✅ Guardar nombre como "nombre_usuario"
        editor.putString("foto_perfil", usuario.getFoto());
        editor.apply();

        Log.d(TAG, "✅ Usuario guardado - ID: " + usuario.getId() +
                ", Email: " + usuario.getEmail() +
                ", Nombre: " + usuario.getNombre());
    }

    // ✅ MÉTODO CORREGIDO: Leer datos del usuario con campos consistentes
    public Usuario getUsuario() {
        String id = prefs.getString("id_usuario", null);
        String correo = prefs.getString("correo", null);
        String tipoUsuario = prefs.getString("tipo_usuario", null);
        String nombreUsuario = prefs.getString("nombre_usuario", null);
        String fotoPerfil = prefs.getString("foto_perfil", null);

        Log.d(TAG, "📖 Leyendo usuario - ID: " + id + ", Correo: " + correo + ", Nombre: " + nombreUsuario);

        if (id == null || correo == null) {
            Log.w(TAG, "⚠️ No se encontraron datos completos del usuario");
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(Integer.parseInt(id));
        usuario.setCorreo(correo);
        usuario.setTipoUsuario(tipoUsuario);
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setFotoPerfil(fotoPerfil);

        Log.d(TAG, "✅ Usuario cargado exitosamente: " + nombreUsuario);
        return usuario;
    }

    public String getToken() {
        String token = prefs.getString("token", null);
        Log.d(TAG, "Token obtenido: " + (token != null ? "presente" : "ausente"));
        return token;
    }

    // ✅ MÉTODO MEJORADO: Verificar si hay sesión activa Y token válido
    public boolean hasActiveSession() {
        String token = getToken();
        Usuario usuario = getUsuario();
        boolean hasSession = token != null && usuario != null && !isTokenExpired();
        Log.d(TAG, "Sesión activa: " + hasSession);
        return hasSession;
    }

    // ✅ NUEVO: Verificar si necesita renovar token
    public boolean needsTokenRefresh() {
        String token = getToken();
        String refreshToken = getRefreshToken();
        boolean needsRefresh = token != null && refreshToken != null && isTokenExpired();
        Log.d(TAG, "Necesita renovar token: " + needsRefresh);
        return needsRefresh;
    }

    // ✅ MÉTODO ADICIONAL: Obtener ID del usuario actual
    public int getCurrentUserId() {
        Usuario usuario = getUsuario();
        if (usuario != null) {
            return usuario.getIdUsuario();
        }
        return -1;
    }

    // ✅ MÉTODO CORREGIDO: Migrar datos desde SessionManager usando métodos reales
    public void migrarDesdeSessionManager(Context context) {
        try {
            SessionManager sessionManager = SessionManager.getInstance(context);

            // ✅ USAR MÉTODOS CORRECTOS DE SessionManager
            String email = sessionManager.getEmail();
            String nombre = sessionManager.getUserName();
            String userId = String.valueOf(sessionManager.getUserId());
            String token = sessionManager.getAuthToken();
            String foto = sessionManager.getUserPhoto();
            String tipo = sessionManager.getUserType();

            if (email != null && nombre != null && userId != null && token != null) {
                Log.d(TAG, "🔄 Migrando datos desde SessionManager...");

                // Guardar token (sin refresh token porque SessionManager no lo tiene)
                saveToken(token);

                // Crear y guardar usuario
                LoginResponse.UserData userData = new LoginResponse.UserData();
                userData.setId(Integer.parseInt(userId));
                userData.setEmail(email);
                userData.setNombre(nombre);
                userData.setFoto(foto);
                userData.setTipo_usuario(tipo != null ? tipo : "usuario");

                saveUser(userData);

                Log.d(TAG, "✅ Migración completada exitosamente");
                debugPrintUserData();
            } else {
                Log.w(TAG, "⚠️ Datos incompletos en SessionManager para migrar");
                Log.d(TAG, "SessionManager - Email: " + email + ", Nombre: " + nombre +
                        ", UserID: " + userId + ", Token: " + (token != null ? "presente" : "ausente"));
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error durante migración: " + e.getMessage());
        }
    }

    // ✅ MÉTODO PARA AUTO-SINCRONIZAR: Verificar y migrar automáticamente si es necesario
    public void autoSincronizarConSessionManager(Context context) {
        // Solo migrar si LoginManager está vacío pero SessionManager tiene datos
        if (!hasActiveSession()) {
            Log.d(TAG, "🔍 LoginManager vacío, verificando SessionManager...");
            try {
                SessionManager sessionManager = SessionManager.getInstance(context);
                if (sessionManager.getUserId() != -1 && sessionManager.getAuthToken() != null) {
                    Log.d(TAG, "🔄 Datos encontrados en SessionManager, migrando automáticamente...");
                    migrarDesdeSessionManager(context);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error en auto-sincronización: " + e.getMessage());
            }
        }
    }

    // ✅ NUEVO: Limpiar solo tokens (mantener datos de usuario)
    public void clearTokens() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("token");
        editor.remove("refresh_token");
        editor.remove("token_timestamp");
        editor.apply();
        Log.d(TAG, "✅ Tokens eliminados");
    }

    public void clear() {
        prefs.edit().clear().apply();
        Log.d(TAG, "✅ Datos de sesión eliminados");
    }

    // ✅ MÉTODO DE DEBUG: Mostrar todos los datos guardados
    public void debugPrintUserData() {
        Log.d(TAG, "=== DATOS GUARDADOS EN SHARED PREFERENCES ===");
        Log.d(TAG, "Token: " + prefs.getString("token", "NO ENCONTRADO"));
        Log.d(TAG, "Refresh Token: " + prefs.getString("refresh_token", "NO ENCONTRADO"));
        Log.d(TAG, "Token Timestamp: " + prefs.getLong("token_timestamp", 0));
        Log.d(TAG, "ID Usuario: " + prefs.getString("id_usuario", "NO ENCONTRADO"));
        Log.d(TAG, "Correo: " + prefs.getString("correo", "NO ENCONTRADO"));
        Log.d(TAG, "Nombre Usuario: " + prefs.getString("nombre_usuario", "NO ENCONTRADO"));
        Log.d(TAG, "Tipo Usuario: " + prefs.getString("tipo_usuario", "NO ENCONTRADO"));
        Log.d(TAG, "Foto Perfil: " + prefs.getString("foto_perfil", "NO ENCONTRADO"));
        Log.d(TAG, "===============================================");
    }

    // ✅ MÉTODO DE DEBUG ADICIONAL: Mostrar datos de SessionManager
    public void debugPrintSessionManagerData(Context context) {
        try {
            SessionManager sessionManager = SessionManager.getInstance(context);
            Log.d(TAG, "=== DATOS EN SESSION MANAGER ===");
            Log.d(TAG, "Email: " + sessionManager.getEmail());
            Log.d(TAG, "User Name: " + sessionManager.getUserName());
            Log.d(TAG, "User ID: " + sessionManager.getUserId());
            Log.d(TAG, "Token: " + (sessionManager.getAuthToken() != null ? "presente" : "ausente"));
            Log.d(TAG, "User Photo: " + sessionManager.getUserPhoto());
            Log.d(TAG, "User Type: " + sessionManager.getUserType());
            Log.d(TAG, "Is Logged In: " + sessionManager.isLoggedIn());
            Log.d(TAG, "Is Verified: " + sessionManager.isUserVerified());
            Log.d(TAG, "================================");
        } catch (Exception e) {
            Log.e(TAG, "Error al leer SessionManager: " + e.getMessage());
        }
    }
}