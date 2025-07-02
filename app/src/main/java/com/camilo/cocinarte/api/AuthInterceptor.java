package com.camilo.cocinarte.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.ui.authentication.InicioSesionActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final Context context;
    private final SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = SessionManager.getInstance(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Si es una ruta de autenticación, no agregar token
        if (isAuthRoute(originalRequest)) {
            return chain.proceed(originalRequest);
        }

        // Verificar si hay token y sesión válida
        String token = sessionManager.getAuthToken();
        if (token == null || sessionManager.isSessionExpired()) {
            // Si no hay token o la sesión expiró, redirigir a login
            handleSessionExpired();
            return chain.proceed(originalRequest);
        }

        // Agregar token al header
        Request authenticatedRequest = originalRequest.newBuilder()
                .header(HEADER_AUTHORIZATION, TOKEN_PREFIX + token)
                .build();

        Response response = chain.proceed(authenticatedRequest);

        // Manejar respuestas 401 (no autorizado)
        if (response.code() == 401) {
            handleUnauthorized(response);
        }

        // Renovar sesión en respuestas exitosas
        if (response.isSuccessful()) {
            sessionManager.renewSession();
        }

        return response;
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
                path.contains("/auth/set-new-password");
    }

    /**
     * Maneja cuando la sesión ha expirado
     */
    private void handleSessionExpired() {
        Log.d(TAG, "Session expired, redirecting to login");
        sessionManager.logout();
        redirectToLogin();
    }

    /**
     * Maneja respuestas 401 no autorizadas
     */
    private void handleUnauthorized(Response response) {
        Log.d(TAG, "Unauthorized response received");

        // Intentar refrescar el token si hay refresh token
        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken != null && !refreshToken.isEmpty()) {
            // TODO: Implementar lógica de refresh token
            Log.d(TAG, "Attempting to refresh token");
        } else {
            // Si no hay refresh token, cerrar sesión
            sessionManager.logout();
            redirectToLogin();
        }
    }

    /**
     * Redirige a la pantalla de login
     */
    private void redirectToLogin() {
        Intent intent = new Intent(context, InicioSesionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}