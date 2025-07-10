package com.camilo.cocinarte.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TokenRefreshService {
    private static final String TAG = "TokenRefreshService";
    private static final String BASE_URL = "https://cocinarte-backend-production.up.railway.app";

    private Context context;
    private RequestQueue requestQueue;
    private LoginManager loginManager;

    public TokenRefreshService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.loginManager = new LoginManager(context);
    }

    public interface TokenRefreshCallback {
        void onSuccess(String newAccessToken);
        void onFailure(String error);
        void onTokenExpired(); // Cuando el refresh token también expiró
    }

    /**
     * 🔄 Renovar token de acceso usando refresh token
     */
    public void refreshToken(TokenRefreshCallback callback) {
        String refreshToken = loginManager.getRefreshToken();

        if (refreshToken == null) {
            Log.e(TAG, "❌ No hay refresh token disponible");
            callback.onTokenExpired();
            return;
        }

        String url = BASE_URL + "/api/auth/refresh-token";

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("refresh_token", refreshToken);

            Log.d(TAG, "🔄 Renovando token...");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestBody,
                    response -> {
                        try {
                            Log.d(TAG, "✅ Respuesta refresh token: " + response.toString());

                            // Extraer el nuevo access token
                            String newAccessToken = response.getString("access_token");
                            String newRefreshToken = response.optString("refresh_token", refreshToken);

                            // Guardar los nuevos tokens
                            loginManager.saveTokens(newAccessToken, newRefreshToken);

                            Log.d(TAG, "✅ Token renovado exitosamente");
                            callback.onSuccess(newAccessToken);

                        } catch (JSONException e) {
                            Log.e(TAG, "❌ Error parseando respuesta de refresh: " + e.getMessage());
                            callback.onFailure("Error procesando nueva sesión");
                        }
                    },
                    error -> {
                        Log.e(TAG, "❌ Error renovando token: " + error.getMessage());

                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;

                            if (statusCode == 401 || statusCode == 403) {
                                Log.e(TAG, "❌ Refresh token expirado o inválido");
                                // Limpiar datos de sesión
                                loginManager.clear();
                                callback.onTokenExpired();
                            } else {
                                String errorMsg = "Error de conectividad (" + statusCode + ")";
                                try {
                                    String responseBody = new String(error.networkResponse.data, "utf-8");
                                    JSONObject errorJson = new JSONObject(responseBody);
                                    errorMsg = errorJson.optString("error", errorMsg);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parseando error: " + e.getMessage());
                                }
                                callback.onFailure(errorMsg);
                            }
                        } else {
                            callback.onFailure("Error de conexión");
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);

        } catch (JSONException e) {
            Log.e(TAG, "❌ Error creando request refresh: " + e.getMessage());
            callback.onFailure("Error preparando renovación de sesión");
        }
    }

    /**
     * 🔍 Verificar si el token necesita renovación y renovarlo automáticamente
     */
    public void checkAndRefreshToken(TokenRefreshCallback callback) {
        if (loginManager.needsTokenRefresh()) {
            Log.d(TAG, "🔄 Token necesita renovación, renovando automáticamente...");
            refreshToken(callback);
        } else {
            Log.d(TAG, "✅ Token aún válido");
            String currentToken = loginManager.getToken();
            callback.onSuccess(currentToken);
        }
    }

    /**
     * 🔓 Cerrar sesión y limpiar todos los datos
     */
    public void logout() {
        Log.d(TAG, "🔓 Cerrando sesión y limpiando datos...");
        loginManager.clear();

        // También limpiar SessionManager si existe
        try {
            // Si tienes SessionManager, también limpiarlo
            // SessionManager.getInstance(context).logout();
        } catch (Exception e) {
            Log.e(TAG, "Error limpiando SessionManager: " + e.getMessage());
        }
    }
}