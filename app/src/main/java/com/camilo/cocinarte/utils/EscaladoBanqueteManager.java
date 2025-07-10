package com.camilo.cocinarte.utils;

import android.content.Context;
import android.util.Log;

import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.BanqueteApi;
import com.camilo.cocinarte.models.EscaladoResponse;
import com.camilo.cocinarte.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EscaladoBanqueteManager {

    private static final String TAG = "EscaladoBanqueteManager";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    private final Context context;
    private final SessionManager sessionManager;
    private final BanqueteApi banqueteApi;

    // Constructor
    public EscaladoBanqueteManager(Context context) {
        this.context = context;
        try {
            this.sessionManager = SessionManager.getInstance(context);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.banqueteApi = ApiClient.getClient(context).create(BanqueteApi.class);
    }



    // Interface para callbacks
    public interface EscaladoCallback {
        void onSuccess(EscaladoResponse.EscaladoData escaladoData);
        void onError(String error);
        void onProgress(String mensaje);
    }

    // Método principal para escalar banquete
    public void escalarBanquete(int banqueteId, int personasOriginales, int personasNuevas, EscaladoCallback callback) {
        Log.d(TAG, "🍽️ Iniciando escalado:");
        Log.d(TAG, "   - Banquete ID: " + banqueteId);
        Log.d(TAG, "   - Personas originales: " + personasOriginales);
        Log.d(TAG, "   - Personas nuevas: " + personasNuevas);

        // Validaciones
        if (!validarPersonas(personasNuevas)) {
            callback.onError("Número de personas inválido (debe estar entre 1 y 1000)");
            return;
        }

        if (personasOriginales == personasNuevas) {
            callback.onError("Las personas ya están configuradas para " + personasNuevas);
            return;
        }

        // Preparar datos para el escalado
        callback.onProgress("Preparando datos de escalado...");

        try {
            JSONObject requestData = new JSONObject();
            requestData.put("banquete_id", banqueteId);
            requestData.put("original_portions", personasOriginales);
            requestData.put("new_portions", personasNuevas);
            requestData.put("use_ai", true);
            requestData.put("preserve_ratios", true);

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestData.toString()
            );

            String authToken = "Bearer " + sessionManager.getAuthToken();

            callback.onProgress("Calculando con IA inteligente...");

            // Ejecutar llamada API
            banqueteApi.escalarBanquete(requestBody, authToken).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String jsonResponse = response.body().string();
                            Log.d(TAG, "✅ Respuesta de escalado recibida");
                            Log.d(TAG, "📄 JSON Response: " + jsonResponse.substring(0, Math.min(500, jsonResponse.length())) + "...");

                            // Parsear respuesta
                            EscaladoResponse escaladoResponse = parseEscaladoResponse(jsonResponse);

                            if (escaladoResponse != null && escaladoResponse.isSuccess()) {
                                callback.onProgress("Escalado completado exitosamente");
                                callback.onSuccess(escaladoResponse.getData());
                            } else {
                                String errorMsg = escaladoResponse != null ? escaladoResponse.getMessage() : "Error desconocido";
                                callback.onError("Error en el escalado: " + errorMsg);
                            }

                        } catch (IOException | JSONException e) {
                            Log.e(TAG, "❌ Error al procesar respuesta de escalado", e);
                            callback.onError("Error procesando respuesta: " + e.getMessage());
                        }
                    } else {
                        Log.e(TAG, "❌ Error en llamada de escalado: " + response.code());
                        callback.onError("Error del servidor: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "❌ Error de conexión en escalado", t);
                    callback.onError("Error de conexión: " + t.getMessage());
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "❌ Error preparando datos de escalado", e);
            callback.onError("Error preparando datos: " + e.getMessage());
        }
    }

    // Parsear respuesta JSON a objeto EscaladoResponse
    private EscaladoResponse parseEscaladoResponse(String jsonResponse) throws JSONException {
        JSONObject json = new JSONObject(jsonResponse);

        EscaladoResponse response = new EscaladoResponse();
        response.setSuccess(json.optBoolean("success", false));
        response.setMessage(json.optString("message", ""));

        if (json.has("data")) {
            JSONObject dataJson = json.getJSONObject("data");
            EscaladoResponse.EscaladoData data = parseEscaladoData(dataJson);
            response.setData(data);
        }

        return response;
    }

    // Parsear datos de escalado - ADAPTADO PARA TU MODELO
    private EscaladoResponse.EscaladoData parseEscaladoData(JSONObject dataJson) throws JSONException {
        EscaladoResponse.EscaladoData data = new EscaladoResponse.EscaladoData();

        // Datos básicos - adaptados a los nombres del JSON del servidor
        data.setBanqueteId(dataJson.optInt("banquete_id", 0));
        data.setOriginalPortions(dataJson.optInt("originalPeople", dataJson.optInt("original_portions", 0)));
        data.setNewPortions(dataJson.optInt("newPeople", dataJson.optInt("new_portions", 0)));
        data.setScaleFactor(dataJson.optDouble("scaleFactor", dataJson.optDouble("scale_factor", 1.0)));
        data.setAiProcessed(dataJson.optBoolean("ai_processed", false));
        data.setAiVersion(dataJson.optString("ai_version", ""));
        data.setProcessingTimeMs(dataJson.optLong("processing_time_ms", 0));

        Log.d(TAG, "🔍 Parseando datos de escalado:");
        Log.d(TAG, "   - Scale Factor: " + data.getScaleFactor());
        Log.d(TAG, "   - Original People: " + data.getOriginalPortions());
        Log.d(TAG, "   - New People: " + data.getNewPortions());

        // Parsear ingredientes escalados - IMPLEMENTACIÓN COMPLETA ADAPTADA
        List<EscaladoResponse.IngredienteEscalado> ingredientesEscalados = new ArrayList<>();

        if (dataJson.has("scaledIngredients")) {
            JSONArray ingredientesArray = dataJson.getJSONArray("scaledIngredients");
            Log.d(TAG, "📦 Parseando " + ingredientesArray.length() + " ingredientes escalados");

            for (int i = 0; i < ingredientesArray.length(); i++) {
                try {
                    JSONObject ingredienteJson = ingredientesArray.getJSONObject(i);
                    EscaladoResponse.IngredienteEscalado ingrediente = parseIngredienteEscalado(ingredienteJson);
                    if (ingrediente != null) {
                        ingredientesEscalados.add(ingrediente);
                        Log.d(TAG, "✅ Ingrediente " + (i+1) + " parseado: " + ingrediente.getName());
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "❌ Error parseando ingrediente " + i, e);
                }
            }
        }

        data.setScaledIngredients(ingredientesEscalados);
        Log.d(TAG, "📊 Total ingredientes parseados: " + ingredientesEscalados.size());

        // Parsear recomendaciones
        List<String> recomendaciones = new ArrayList<>();
        if (dataJson.has("recommendations")) {
            JSONArray recomendacionesArray = dataJson.getJSONArray("recommendations");
            for (int i = 0; i < recomendacionesArray.length(); i++) {
                recomendaciones.add(recomendacionesArray.getString(i));
            }
        }
        data.setRecommendations(recomendaciones);

        return data;
    }

    // Método para parsear ingrediente escalado individual - ADAPTADO PARA TU MODELO
    private EscaladoResponse.IngredienteEscalado parseIngredienteEscalado(JSONObject ingredienteJson) throws JSONException {
        EscaladoResponse.IngredienteEscalado ingrediente = new EscaladoResponse.IngredienteEscalado();

        // Datos básicos del ingrediente - adaptados a tu modelo
        ingrediente.setId(ingredienteJson.optInt("id", 0));
        ingrediente.setName(ingredienteJson.optString("name", ""));
        ingrediente.setOriginalQuantity(ingredienteJson.optString("originalQuantity", ""));
        ingrediente.setScaledQuantity(ingredienteJson.optString("scaledQuantity", ""));
        ingrediente.setCategory(ingredienteJson.optString("category", ""));

        // Scaling notes - combinar información de IA si existe
        String scalingNotes = "";
        if (ingredienteJson.has("aiReasonning")) {
            scalingNotes = ingredienteJson.optString("aiReasonning", "");
        }
        if (ingredienteJson.has("scalingNotes")) {
            scalingNotes = ingredienteJson.optString("scalingNotes", scalingNotes);
        }
        ingrediente.setScalingNotes(scalingNotes);

        // Parsear información nutricional
        if (ingredienteJson.has("nutrition")) {
            JSONObject nutritionJson = ingredienteJson.getJSONObject("nutrition");
            EscaladoResponse.NutricionEscalada nutricion = new EscaladoResponse.NutricionEscalada();

            nutricion.setCalories(nutritionJson.optDouble("calories", 0.0));
            nutricion.setProtein(nutritionJson.optDouble("protein", 0.0));
            nutricion.setCarbs(nutritionJson.optDouble("carbs", 0.0));
            nutricion.setFats(nutritionJson.optDouble("fats", 0.0));
            nutricion.setSugar(nutritionJson.optDouble("sugar", 0.0));
            nutricion.setFiber(nutritionJson.optDouble("fiber", 0.0));
            nutricion.setSodium(nutritionJson.optDouble("sodium", 0.0));

            ingrediente.setNutrition(nutricion);
        }

        return ingrediente;
    }

    // Métodos estáticos de utilidad
    public static boolean validarPersonas(int personas) {
        return personas >= 1 && personas <= 1000;
    }

    public static double calcularFactorEscala(int personasOriginales, int personasNuevas) {
        if (personasOriginales <= 0) return 1.0;
        return (double) personasNuevas / personasOriginales;
    }

    public static String formatearFactor(double factor) {
        return DECIMAL_FORMAT.format(factor) + "x";
    }

    public static String obtenerMensajeEscalado(int personasOriginales, int personasNuevas) {
        if (personasNuevas > personasOriginales) {
            return "Aumentando ingredientes para más personas";
        } else if (personasNuevas < personasOriginales) {
            return "Reduciendo ingredientes para menos personas";
        } else {
            return "Sin cambios en las cantidades";
        }
    }

    // Método para formatear tiempo de procesamiento
    public static String formatearTiempoProcesamiento(long tiempoMs) {
        if (tiempoMs < 1000) {
            return tiempoMs + "ms";
        } else {
            long segundos = TimeUnit.MILLISECONDS.toSeconds(tiempoMs);
            return segundos + "s";
        }
    }

    // Método para obtener descripción del factor de escalado
    public static String obtenerDescripcionFactor(double factor) {
        if (factor > 2.0) {
            return "Escalado grande (más del doble)";
        } else if (factor > 1.5) {
            return "Escalado considerable";
        } else if (factor > 1.0) {
            return "Escalado moderado";
        } else if (factor == 1.0) {
            return "Sin escalado";
        } else if (factor > 0.5) {
            return "Reducción moderada";
        } else {
            return "Reducción considerable";
        }
    }

    // Método para validar que el escalado sea razonable
    public static boolean esEscaladoRazonable(int personasOriginales, int personasNuevas) {
        double factor = calcularFactorEscala(personasOriginales, personasNuevas);
        return factor >= 0.1 && factor <= 10.0; // Entre 0.1x y 10x
    }

    // Método para obtener recomendaciones basadas en el factor
    public static String obtenerRecomendacionEscalado(double factor) {
        if (factor > 3.0) {
            return "💡 Considera dividir la preparación en lotes más pequeños";
        } else if (factor > 2.0) {
            return "💡 Verifica que tienes suficientes utensilios y espacio";
        } else if (factor < 0.3) {
            return "💡 Cuidado con ingredientes que no se pueden dividir fácilmente";
        } else if (factor < 0.5) {
            return "💡 Algunos condimentos pueden necesitar ajuste manual";
        } else {
            return "💡 Escalado óptimo para mejores resultados";
        }
    }
}