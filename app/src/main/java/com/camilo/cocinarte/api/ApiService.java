package com.camilo.cocinarte.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.camilo.cocinarte.ui.authentication.InicioSesionActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiService {
    private static final String TAG = "ApiService";
    private static final String BASE_URL = "https://cocinarte-backend-production.up.railway.app";

    private RequestQueue requestQueue;
    private Context context;
    private LoginManager loginManager;
    private TokenRefreshService tokenRefreshService;

    public ApiService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.loginManager = new LoginManager(context);
        this.tokenRefreshService = new TokenRefreshService(context);
    }

    // ============================================
    // INTERFACES PARA CALLBACKS
    // ============================================

    public interface PagoCallback {
        void onSuccess(String message, String referencia);
        void onError(String error);
    }

    public interface PlanNutricionalCallback {
        void onSuccess(JSONObject planGenerado);
        void onError(String error);
    }

    public interface IngredientesCallback {
        void onSuccess(JSONObject ingredientesPorCategoria);
        void onError(String error);
    }

    // ============================================
    // MÉTODO PRINCIPAL: HACER REQUEST CON AUTO-REFRESH
    // ============================================

    /**
     * 🔄 Hacer request con manejo automático de token expirado
     */
    /**
     * 🔄 Hacer request con manejo automático de token expirado
     */
    private void makeAuthenticatedRequest(int method, String url, JSONObject requestBody,
                                          Response.Listener<JSONObject> successListener,
                                          String token, PagoCallback callback) {

        JsonObjectRequest newRequest = new JsonObjectRequest(
                method,
                url,
                requestBody,
                successListener,
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        Log.w(TAG, "🔄 Token expirado (401), intentando renovar...");

                        tokenRefreshService.refreshToken(new TokenRefreshService.TokenRefreshCallback() {
                            @Override
                            public void onSuccess(String newAccessToken) {
                                Log.d(TAG, "✅ Token renovado, reintentando request original...");

                                JsonObjectRequest retryRequest = new JsonObjectRequest(
                                        method, url, requestBody, successListener,
                                        retryError -> handleNormalError(retryError, callback)
                                ) {
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        Map<String, String> headers = new HashMap<>();
                                        headers.put("Authorization", "Bearer " + newAccessToken);
                                        headers.put("Content-Type", "application/json");
                                        return headers;
                                    }
                                };

                                requestQueue.add(retryRequest);
                            }

                            @Override
                            public void onFailure(String refreshError) {
                                Log.e(TAG, "❌ Error renovando token: " + refreshError);
                                callback.onError("Error de autenticación: " + refreshError);
                            }

                            @Override
                            public void onTokenExpired() {
                                Log.e(TAG, "❌ Sesión completamente expirada, redirigiendo a login");
                                redirectToLogin();
                                callback.onError("Sesión expirada. Por favor, inicia sesión nuevamente");
                            }
                        });
                    } else {
                        handleNormalError(error, callback);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        newRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                30000, 0, 1.0f
        ));

        requestQueue.add(newRequest);
    }

    private void handleNormalError(VolleyError error, PagoCallback callback) {
        String errorMsg = "Error en la petición";
        if (error.networkResponse != null) {
            try {
                String responseBody = new String(error.networkResponse.data, "utf-8");
                JSONObject errorJson = new JSONObject(responseBody);
                errorMsg = errorJson.optString("error", errorMsg);
            } catch (Exception e) {
                Log.e(TAG, "Error parseando error: " + e.getMessage());
            }
        }
        Log.e(TAG, "❌ " + errorMsg);
        callback.onError(errorMsg);
    }

    /**
     * 🔓 Redirigir a pantalla de login
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

    // ============================================
    // MÉTODOS PARA PLANES NUTRICIONALES (ACTUALIZADOS CON AUTO-REFRESH)
    // ============================================

    /**
     * 🆓 Activar plan gratuito CON manejo automático de tokens
     */
    public void activarPlanGratis(String token, PagoCallback callback) {
        String url = BASE_URL + "/api/pagos/activar-gratis";

        JSONObject requestBody = new JSONObject();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        Log.d(TAG, "✅ Plan gratuito activado: " + response.toString());
                        String referencia = response.getJSONObject("data").getString("referencia");
                        callback.onSuccess("Plan gratuito activado", referencia);
                    } catch (JSONException e) {
                        Log.e(TAG, "❌ Error parseando respuesta plan gratuito: " + e.getMessage());
                        callback.onError("Error procesando respuesta del servidor");
                    }
                },
                null // Error listener se asigna en makeAuthenticatedRequest
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // ✅ USAR EL NUEVO SISTEMA CON AUTO-REFRESH
        makeAuthenticatedRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        Log.d(TAG, "✅ Plan gratuito activado: " + response.toString());
                        String referencia = response.getJSONObject("data").getString("referencia");
                        callback.onSuccess("Plan gratuito activado", referencia);
                    } catch (JSONException e) {
                        Log.e(TAG, "❌ Error parseando respuesta plan gratuito: " + e.getMessage());
                        callback.onError("Error procesando respuesta del servidor");
                    }
                }, token, callback);
    }

    /**
     * 💳 Procesar pago plan pro CON manejo automático de tokens
     */
    public void procesarPagoPlanPro(String token, String metodoPago, String referenciaPago,
                                    double monto, PagoCallback callback) {
        String url = BASE_URL + "/api/pagos/procesar-pago-pro";

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("metodo_pago", metodoPago);
            requestBody.put("referencia_pago", referenciaPago);
            requestBody.put("monto_pagado", monto);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestBody,
                    response -> {
                        try {
                            Log.d(TAG, "✅ Plan Pro procesado: " + response.toString());
                            String referencia = response.getJSONObject("data").getString("referencia");
                            callback.onSuccess("Plan Pro activado", referencia);
                        } catch (JSONException e) {
                            Log.e(TAG, "❌ Error parseando respuesta plan pro: " + e.getMessage());
                            callback.onError("Error procesando respuesta del servidor");
                        }
                    },
                    null // Error listener se asigna en makeAuthenticatedRequest
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    if (token != null && !token.isEmpty()) {
                        headers.put("Authorization", "Bearer " + token);
                    }
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            // ✅ USAR EL NUEVO SISTEMA CON AUTO-REFRESH
            makeAuthenticatedRequest(Request.Method.POST, url, requestBody,
                    response -> {
                        try {
                            Log.d(TAG, "✅ Plan Pro procesado: " + response.toString());
                            String referencia = response.getJSONObject("data").getString("referencia");
                            callback.onSuccess("Plan Pro activado", referencia);
                        } catch (JSONException e) {
                            Log.e(TAG, "❌ Error parseando respuesta plan pro: " + e.getMessage());
                            callback.onError("Error procesando respuesta del servidor");
                        }
                    }, token, callback);


        } catch (JSONException e) {
            Log.e(TAG, "❌ Error creando request pago: " + e.getMessage());
            callback.onError("Error preparando datos de pago");
        }
    }

    /**
     * 🍎 Crear plan nutricional con IA CON manejo automático de tokens
     */
    public void crearPlanNutricional(String token, PlanNutricionalData planData,
                                     PlanNutricionalCallback callback) {
        String url = BASE_URL + "/api/planes-nutricionales/crear";

        try {
            JSONObject requestBody = planData.toJSONObject();

            Log.d(TAG, "🍎 Enviando datos plan nutricional: " + requestBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestBody,
                    response -> {
                        try {
                            Log.d(TAG, "✅ Plan nutricional creado: " + response.toString());
                            callback.onSuccess(response);
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error procesando respuesta plan: " + e.getMessage());
                            callback.onError("Error procesando plan nutricional");
                        }
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            Log.w(TAG, "🔄 Token expirado creando plan, renovando...");

                            tokenRefreshService.refreshToken(new TokenRefreshService.TokenRefreshCallback() {
                                @Override
                                public void onSuccess(String newAccessToken) {
                                    Log.d(TAG, "✅ Token renovado, reintentando crear plan...");
                                    // Reintentar con nuevo token
                                    crearPlanNutricionalWithToken(newAccessToken, planData, callback);
                                }

                                @Override
                                public void onFailure(String refreshError) {
                                    Log.e(TAG, "❌ Error renovando token para plan: " + refreshError);
                                    callback.onError("Error de autenticación: " + refreshError);
                                }

                                @Override
                                public void onTokenExpired() {
                                    Log.e(TAG, "❌ Sesión expirada creando plan");
                                    redirectToLogin();
                                    callback.onError("Sesión expirada");
                                }
                            });
                        } else {
                            String errorMsg = "Error creando plan nutricional";
                            if (error.networkResponse != null) {
                                try {
                                    String responseBody = new String(error.networkResponse.data, "utf-8");
                                    JSONObject errorJson = new JSONObject(responseBody);
                                    errorMsg = errorJson.optString("error", errorMsg);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parseando error plan: " + e.getMessage());
                                }
                            }
                            Log.e(TAG, "❌ " + errorMsg);
                            callback.onError(errorMsg);
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    if (token != null && !token.isEmpty()) {
                        headers.put("Authorization", "Bearer " + token);
                    }
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);

        } catch (JSONException e) {
            Log.e(TAG, "❌ Error creando request plan: " + e.getMessage());
            callback.onError("Error preparando datos del plan");
        }
    }

    /**
     * 🍎 Método auxiliar para reintentar crear plan con nuevo token
     */
    private void crearPlanNutricionalWithToken(String token, PlanNutricionalData planData,
                                               PlanNutricionalCallback callback) {
        String url = BASE_URL + "/api/planes-nutricionales/crear";

        try {
            JSONObject requestBody = planData.toJSONObject();

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestBody,
                    response -> {
                        Log.d(TAG, "✅ Plan nutricional creado (reintento): " + response.toString());
                        callback.onSuccess(response);
                    },
                    error -> {
                        String errorMsg = "Error creando plan nutricional";
                        if (error.networkResponse != null) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject errorJson = new JSONObject(responseBody);
                                errorMsg = errorJson.optString("error", errorMsg);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parseando error plan: " + e.getMessage());
                            }
                        }
                        Log.e(TAG, "❌ " + errorMsg);
                        callback.onError(errorMsg);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    if (token != null && !token.isEmpty()) {
                        headers.put("Authorization", "Bearer " + token);
                    }
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);

        } catch (JSONException e) {
            Log.e(TAG, "❌ Error creando request plan (reintento): " + e.getMessage());
            callback.onError("Error preparando datos del plan");
        }
    }

    /**
     * 🥗 Obtener ingredientes por categorías (sin autenticación)
     */
    public void obtenerIngredientesPorCategorias(IngredientesCallback callback) {
        String url = BASE_URL + "/api/planes-nutricionales/ingredientes-categorias";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "✅ Ingredientes obtenidos: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Error obteniendo ingredientes";
                    Log.e(TAG, "❌ " + errorMsg);
                    callback.onError(errorMsg);
                }
        );

        requestQueue.add(request);
    }

    /**
     * 📊 Obtener plan activo del usuario CON manejo automático de tokens
     */
    public void obtenerPlanActivo(String token, PlanNutricionalCallback callback) {
        String url = BASE_URL + "/api/planes-nutricionales/mi-plan";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "✅ Plan activo obtenido: " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        Log.w(TAG, "🔄 Token expirado obteniendo plan activo, renovando...");

                        tokenRefreshService.refreshToken(new TokenRefreshService.TokenRefreshCallback() {
                            @Override
                            public void onSuccess(String newAccessToken) {
                                Log.d(TAG, "✅ Token renovado, reintentando obtener plan activo...");
                                // Reintentar con nuevo token
                                obtenerPlanActivoWithToken(newAccessToken, callback);
                            }

                            @Override
                            public void onFailure(String refreshError) {
                                Log.e(TAG, "❌ Error renovando token para plan activo: " + refreshError);
                                callback.onError("Error de autenticación: " + refreshError);
                            }

                            @Override
                            public void onTokenExpired() {
                                Log.e(TAG, "❌ Sesión expirada obteniendo plan activo");
                                redirectToLogin();
                                callback.onError("Sesión expirada");
                            }
                        });
                    } else {
                        String errorMsg = "Error obteniendo plan activo";
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            errorMsg = "No tienes un plan nutricional activo";
                        }
                        Log.e(TAG, "❌ " + errorMsg);
                        callback.onError(errorMsg);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        requestQueue.add(request);
    }

    /**
     * 📊 Método auxiliar para reintentar obtener plan activo con nuevo token
     */
    private void obtenerPlanActivoWithToken(String token, PlanNutricionalCallback callback) {
        String url = BASE_URL + "/api/planes-nutricionales/mi-plan";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "✅ Plan activo obtenido (reintento): " + response.toString());
                    callback.onSuccess(response);
                },
                error -> {
                    String errorMsg = "Error obteniendo plan activo";
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                        errorMsg = "No tienes un plan nutricional activo";
                    }
                    Log.e(TAG, "❌ " + errorMsg);
                    callback.onError(errorMsg);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // ============================================
    // CLASE DE DATOS PARA PLAN NUTRICIONAL (ORIGINAL)
    // ============================================

    public static class PlanNutricionalData {
        public String objetivo;
        public String sexo;
        public int edad;
        public int altura;
        public double peso;
        public String nivelActividad;
        public boolean entrenamientoFuerza;
        public String tipoDieta = "Normal";
        public Map<String, List<Integer>> ingredientesSeleccionados;
        public String tipoPlan;

        public JSONObject toJSONObject() throws JSONException {
            JSONObject json = new JSONObject();

            json.put("objetivo", objetivo);
            json.put("sexo", sexo);
            json.put("edad", edad);
            json.put("altura", altura);
            json.put("peso", peso);
            json.put("nivel_actividad", nivelActividad);
            json.put("entrenamiento_fuerza", entrenamientoFuerza);
            json.put("tipo_dieta", tipoDieta);
            json.put("tipo_plan", tipoPlan);

            // Convertir ingredientes seleccionados
            if (ingredientesSeleccionados != null) {
                JSONObject ingredientesJson = new JSONObject();
                for (Map.Entry<String, List<Integer>> entry : ingredientesSeleccionados.entrySet()) {
                    JSONArray idsArray = new JSONArray();
                    for (Integer id : entry.getValue()) {
                        idsArray.put(id);
                    }
                    ingredientesJson.put(entry.getKey(), idsArray);
                }
                json.put("ingredientes_seleccionados", ingredientesJson);
            }

            return json;
        }
    }

    // ============================================
    // MÉTODOS UTILITARIOS
    // ============================================

    /**
     * 🧹 Limpiar caché de requests
     */
    public void clearRequestQueue() {
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
            Log.d(TAG, "🧹 Cola de requests limpiada");
        }
    }

    /**
     * 🔄 Reiniciar servicio (útil después de logout)
     */
    public void reiniciarServicio() {
        clearRequestQueue();
        Log.d(TAG, "🔄 Servicio reiniciado");
    }
}