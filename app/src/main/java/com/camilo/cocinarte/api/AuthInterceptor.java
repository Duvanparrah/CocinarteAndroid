package com.camilo.cocinarte.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.ui.authentication.InicioSesionActivity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 🔄 AuthInterceptor mejorado con refresh token automático
 * Basado en tu código existente pero con funcionalidad de renovación automática
 */
public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final Context context;
    private final SessionManager sessionManager;
    private final LoginManager loginManager;
    private final TokenRefreshService tokenRefreshService;

    public AuthInterceptor(Context context) throws GeneralSecurityException, IOException {
        this.context = context.getApplicationContext();
        this.sessionManager = SessionManager.getInstance(context);
        this.loginManager = new LoginManager(context);
        this.tokenRefreshService = new TokenRefreshService(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Si es una ruta de autenticación, no agregar token
        if (isAuthRoute(originalRequest)) {
            return chain.proceed(originalRequest);
        }

        // ✅ MEJORADO: Priorizar LoginManager, fallback a SessionManager
        String token = getValidToken();

        if (token == null) {
            Log.w(TAG, "⚠️ No hay token válido disponible");
            handleSessionExpired();
            return chain.proceed(originalRequest);
        }

        // Agregar token al header
        Request authenticatedRequest = originalRequest.newBuilder()
                .header(HEADER_AUTHORIZATION, TOKEN_PREFIX + token)
                .build();

        Response response = chain.proceed(authenticatedRequest);

        // ✅ MEJORADO: Manejar 401 con renovación automática
        if (response.code() == 401) {
            Log.w(TAG, "🔄 Recibido 401, intentando renovar token...");

            response.close(); // Cerrar respuesta original

            // Intentar renovar token
            String newToken = refreshTokenSync();

            if (newToken != null) {
                Log.d(TAG, "✅ Token renovado exitosamente, reintentando petición...");

                // Crear nueva petición con token renovado
                Request newRequest = originalRequest.newBuilder()
                        .removeHeader(HEADER_AUTHORIZATION)
                        .addHeader(HEADER_AUTHORIZATION, TOKEN_PREFIX + newToken)
                        .build();

                // Reintentar la petición
                return chain.proceed(newRequest);
            } else {
                Log.e(TAG, "❌ No se pudo renovar el token");
                handleUnauthorized();
                return response;
            }
        }

        return response;
    }

    /**
     * ✅ MEJORADO: Obtener token válido priorizando LoginManager
     */
    private String getValidToken() {
        // Priorizar LoginManager
        String token = loginManager.getToken();
        if (token != null && !loginManager.isTokenExpired()) {
            Log.d(TAG, "✅ Token válido desde LoginManager");
            return token;
        }

        // Fallback a SessionManager
        if (sessionManager != null) {
            String sessionToken = sessionManager.getAuthToken();
            if (sessionToken != null && !sessionManager.isSessionExpired()) {
                Log.d(TAG, "✅ Token válido desde SessionManager");
                // Migrar a LoginManager para futuras peticiones
                loginManager.saveToken(sessionToken);
                return sessionToken;
            }
        }

        Log.w(TAG, "❌ No hay token válido en ningún manager");
        return null;
    }

    /**
     * 🔄 Renovar token de forma síncrona para usar en el interceptor
     */
    private String refreshTokenSync() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> newTokenRef = new AtomicReference<>();

        tokenRefreshService.refreshToken(new TokenRefreshService.TokenRefreshCallback() {
            @Override
            public void onSuccess(String newAccessToken) {
                Log.d(TAG, "✅ Token renovado exitosamente en interceptor");
                newTokenRef.set(newAccessToken);
                latch.countDown();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Error renovando token en interceptor: " + error);
                latch.countDown();
            }

            @Override
            public void onTokenExpired() {
                Log.e(TAG, "❌ Refresh token expirado en interceptor");
                latch.countDown();
            }
        });

        try {
            // Esperar máximo 10 segundos por la renovación
            boolean completed = latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            if (!completed) {
                Log.e(TAG, "❌ Timeout renovando token");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "❌ Interrupted renovando token: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        return newTokenRef.get();
    }

    /**
     * Verifica si es una ruta de autenticación
     */
    private boolean isAuthRoute(Request request) {
        String path = request.url().encodedPath();
        return path.contains("/auth/login") ||
                path.contains("/auth/register") ||
                path.contains("/auth/forgot-password") ||
                path.contains("/auth/verify-reset-code") ||
                path.contains("/auth/set-new-password") ||
                path.contains("/auth/refresh-token");
    }

    /**
     * Maneja cuando la sesión ha expirado
     */
    private void handleSessionExpired() {
        Log.d(TAG, "🔓 Session expired, redirecting to login");
        cleanupSessions();
        redirectToLogin();
    }

    /**
     * Maneja respuestas 401 no autorizadas
     */
    private void handleUnauthorized() {
        Log.d(TAG, "🔓 Unauthorized response received - logging out");
        cleanupSessions();
        redirectToLogin();
    }

    /**
     * ✅ MEJORADO: Limpiar ambas sesiones
     */
    private void cleanupSessions() {
        try {
            if (loginManager != null) {
                loginManager.clear();
            }
            if (sessionManager != null) {
                sessionManager.logout();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando sesiones: " + e.getMessage());
        }
    }

    /**
     * Redirige a la pantalla de login
     */
    private void redirectToLogin() {
        try {
            Intent intent = new Intent(context, InicioSesionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error redirigiendo a login: " + e.getMessage());
        }
    }
}