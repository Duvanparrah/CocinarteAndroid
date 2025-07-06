package com.camilo.cocinarte.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.camilo.cocinarte.models.Banquete;
import com.camilo.cocinarte.session.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manager centralizado para operaciones con banquetes
 * Maneja la lógica de negocio y abstrae las llamadas a la API
 */
public class BanqueteManager {

    private static final String TAG = "BanqueteManager";
    private static BanqueteManager instance;

    private Context context;
    private BanqueteApi banqueteApi;
    private SessionManager sessionManager;

    // Constructor privado para Singleton
    private BanqueteManager(Context context) {
        this.context = context.getApplicationContext();
        this.banqueteApi = ApiClient.getClient(this.context).create(BanqueteApi.class);
        this.sessionManager = SessionManager.getInstance(this.context);

        Log.d(TAG, "🏗️ BanqueteManager inicializado");
    }

    // ✅ MÉTODO SINGLETON
    public static synchronized BanqueteManager getInstance(Context context) {
        if (instance == null) {
            instance = new BanqueteManager(context);
        }
        return instance;
    }

    // ✅ INTERFACES DE CALLBACK
    public interface BanqueteCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    // ✅ OBTENER TODOS LOS BANQUETES
    public void obtenerTodosBanquetes(BanqueteCallback<List<Banquete>> callback) {
        Log.d(TAG, "🍽️ Obteniendo todos los banquetes...");

        // Verificar si hay autenticación
        boolean hasAuth = sessionManager.isLoggedIn() && sessionManager.hasValidToken();

        Call<List<Banquete>> call;

        if (hasAuth) {
            String token = "Bearer " + sessionManager.getAuthToken();
            call = banqueteApi.obtenerTodosBanquetes(token);
            Log.d(TAG, "📞 Llamada CON autenticación");
        } else {
            call = banqueteApi.obtenerTodosBanquetes();
            Log.d(TAG, "📞 Llamada SIN autenticación");
        }

        call.enqueue(new Callback<List<Banquete>>() {
            @Override
            public void onResponse(@NonNull Call<List<Banquete>> call, @NonNull Response<List<Banquete>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Banquete> banquetes = response.body();
                    Log.d(TAG, "✅ Banquetes obtenidos: " + banquetes.size());
                    callback.onSuccess(banquetes);
                } else {
                    String error = "Error al obtener banquetes: " + response.code();
                    Log.e(TAG, "❌ " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Banquete>> call, @NonNull Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "❌ " + error, t);
                callback.onError(error);
            }
        });
    }

    // ✅ OBTENER BANQUETE POR ID
    public void obtenerBanquetePorId(int banqueteId, BanqueteCallback<Banquete> callback) {
        Log.d(TAG, "🔍 Obteniendo banquete por ID: " + banqueteId);

        // Verificar si hay autenticación
        boolean hasAuth = sessionManager.isLoggedIn() && sessionManager.hasValidToken();

        Call<Banquete> call;

        if (hasAuth) {
            String token = "Bearer " + sessionManager.getAuthToken();
            call = banqueteApi.obtenerBanquetePorId(banqueteId, token);
            Log.d(TAG, "📞 Llamada CON autenticación para ID: " + banqueteId);
        } else {
            call = banqueteApi.obtenerBanquetePorId(banqueteId);
            Log.d(TAG, "📞 Llamada SIN autenticación para ID: " + banqueteId);
        }

        call.enqueue(new Callback<Banquete>() {
            @Override
            public void onResponse(@NonNull Call<Banquete> call, @NonNull Response<Banquete> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Banquete banquete = response.body();
                    Log.d(TAG, "✅ Banquete obtenido: " + banquete.getNombre());
                    Log.d(TAG, "   - Platillos: " + banquete.getCantidadPlatillos());
                    Log.d(TAG, "   - Ingredientes: " + banquete.getCantidadIngredientes());
                    callback.onSuccess(banquete);
                } else {
                    String error = "Error al obtener banquete: " + response.code();
                    Log.e(TAG, "❌ " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Banquete> call, @NonNull Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "❌ " + error, t);
                callback.onError(error);
            }
        });
    }

    // ✅ BUSCAR BANQUETES
    public void buscarBanquetes(String query, BanqueteCallback<List<Banquete>> callback) {
        Log.d(TAG, "🔍 Buscando banquetes con query: " + query);

        // Para búsqueda, primero obtenemos todos y luego filtramos localmente
        // En una implementación real, tendrías un endpoint específico de búsqueda
        obtenerTodosBanquetes(new BanqueteCallback<List<Banquete>>() {
            @Override
            public void onSuccess(List<Banquete> banquetes) {
                // Filtrar localmente
                List<Banquete> resultados = filtrarBanquetesLocalmente(banquetes, query);
                Log.d(TAG, "✅ Búsqueda completada. Resultados: " + resultados.size());
                callback.onSuccess(resultados);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error en búsqueda: " + error);
                callback.onError(error);
            }
        });
    }

    // ✅ FILTRAR BANQUETES LOCALMENTE
    private List<Banquete> filtrarBanquetesLocalmente(List<Banquete> banquetes, String query) {
        Log.d(TAG, "🔍 Filtrando " + banquetes.size() + " banquetes con query: " + query);

        java.util.List<Banquete> resultados = new java.util.ArrayList<>();
        String queryLower = query.toLowerCase().trim();

        for (Banquete banquete : banquetes) {
            boolean coincide = false;

            // Buscar en nombre
            if (banquete.getNombre() != null &&
                    banquete.getNombre().toLowerCase().contains(queryLower)) {
                coincide = true;
            }

            // Buscar en descripción
            if (!coincide && banquete.getDescripcionPreparacion() != null &&
                    banquete.getDescripcionPreparacion().toLowerCase().contains(queryLower)) {
                coincide = true;
            }

            // Buscar en dificultad
            if (!coincide && banquete.getDificultad() != null &&
                    banquete.getDificultad().toLowerCase().contains(queryLower)) {
                coincide = true;
            }

            // Buscar por cantidad de personas (si la query es un número)
            if (!coincide) {
                try {
                    int queryPersonas = Integer.parseInt(queryLower);
                    if (banquete.getCantidadPersonas() == queryPersonas) {
                        coincide = true;
                    }
                } catch (NumberFormatException e) {
                    // No es un número, continuar
                }
            }

            if (coincide) {
                resultados.add(banquete);
            }
        }

        Log.d(TAG, "✅ Filtrado completado: " + resultados.size() + " resultados");
        return resultados;
    }

    // ✅ OBTENER BANQUETES FAVORITOS
    public void obtenerBanquetesFavoritos(BanqueteCallback<List<Banquete>> callback) {
        Log.d(TAG, "⭐ Obteniendo banquetes favoritos...");

        if (!sessionManager.isLoggedIn()) {
            callback.onError("Usuario no autenticado");
            return;
        }

        String token = "Bearer " + sessionManager.getAuthToken();

        banqueteApi.obtenerBanquetesFavoritos(token).enqueue(new Callback<List<Banquete>>() {
            @Override
            public void onResponse(@NonNull Call<List<Banquete>> call, @NonNull Response<List<Banquete>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Banquete> favoritos = response.body();
                    Log.d(TAG, "✅ Banquetes favoritos obtenidos: " + favoritos.size());
                    callback.onSuccess(favoritos);
                } else {
                    String error = "Error al obtener favoritos: " + response.code();
                    Log.e(TAG, "❌ " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Banquete>> call, @NonNull Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "❌ " + error, t);
                callback.onError(error);
            }
        });
    }

    // ✅ AGREGAR/QUITAR BANQUETE DE FAVORITOS - CORREGIDO
    public void toggleFavoritoBanquete(int banqueteId, BanqueteCallback<Boolean> callback) {
        Log.d(TAG, "⭐ Toggle favorito para banquete: " + banqueteId);

        if (!sessionManager.isLoggedIn()) {
            callback.onError("Usuario no autenticado");
            return;
        }

        String token = "Bearer " + sessionManager.getAuthToken();

        // Primero verificar si ya es favorito
        banqueteApi.verificarBanqueteFavorito(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        boolean esFavorito = obj.optBoolean("esFavorito", false);

                        // Ejecutar acción opuesta
                        if (esFavorito) {
                            quitarDeFavoritos(banqueteId, token, callback);
                        } else {
                            agregarAFavoritos(banqueteId, token, callback);
                        }

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error al procesar verificación de favorito", e);
                        callback.onError("Error al verificar favorito");
                    }
                } else {
                    Log.e(TAG, "❌ Error al verificar favorito: " + response.code());
                    callback.onError("Error al verificar favorito");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión al verificar favorito", t);
                callback.onError("Error de conexión");
            }
        });
    }

    // ✅ AGREGAR A FAVORITOS - CORREGIDO
    private void agregarAFavoritos(int banqueteId, String token, BanqueteCallback<Boolean> callback) {
        banqueteApi.agregarBanqueteAFavoritos(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Banquete agregado a favoritos");
                    callback.onSuccess(true); // true = agregado
                } else {
                    String error = "Error al agregar favorito: " + response.code();
                    Log.e(TAG, "❌ " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "❌ " + error, t);
                callback.onError(error);
            }
        });
    }

    // ✅ QUITAR DE FAVORITOS - CORREGIDO
    private void quitarDeFavoritos(int banqueteId, String token, BanqueteCallback<Boolean> callback) {
        banqueteApi.quitarBanqueteDeFavoritos(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Banquete quitado de favoritos");
                    callback.onSuccess(false); // false = quitado
                } else {
                    String error = "Error al quitar favorito: " + response.code();
                    Log.e(TAG, "❌ " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "❌ " + error, t);
                callback.onError(error);
            }
        });
    }

    // ✅ VERIFICAR SI ES FAVORITO - CORREGIDO
    public void verificarFavorito(int banqueteId, BanqueteCallback<Boolean> callback) {
        Log.d(TAG, "🔍 Verificando si banquete " + banqueteId + " es favorito");

        if (!sessionManager.isLoggedIn()) {
            callback.onSuccess(false);
            return;
        }

        String token = "Bearer " + sessionManager.getAuthToken();

        banqueteApi.verificarBanqueteFavorito(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        boolean esFavorito = obj.optBoolean("esFavorito", false);

                        Log.d(TAG, "✅ Verificación completada. Es favorito: " + esFavorito);
                        callback.onSuccess(esFavorito);

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error al procesar verificación", e);
                        callback.onSuccess(false); // Default: no es favorito
                    }
                } else {
                    Log.e(TAG, "❌ Error al verificar favorito: " + response.code());
                    callback.onSuccess(false); // Default: no es favorito
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión al verificar favorito", t);
                callback.onSuccess(false); // Default: no es favorito
            }
        });
    }

    // ✅ FILTRAR BANQUETES POR DIFICULTAD
    public void filtrarPorDificultad(String dificultad, BanqueteCallback<List<Banquete>> callback) {
        Log.d(TAG, "🔍 Filtrando banquetes por dificultad: " + dificultad);

        obtenerTodosBanquetes(new BanqueteCallback<List<Banquete>>() {
            @Override
            public void onSuccess(List<Banquete> banquetes) {
                java.util.List<Banquete> filtrados = new java.util.ArrayList<>();

                for (Banquete banquete : banquetes) {
                    if (banquete.getDificultad() != null &&
                            banquete.getDificultad().equalsIgnoreCase(dificultad)) {
                        filtrados.add(banquete);
                    }
                }

                Log.d(TAG, "✅ Filtrado por dificultad completado: " + filtrados.size() + " resultados");
                callback.onSuccess(filtrados);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ✅ FILTRAR BANQUETES POR CANTIDAD DE PERSONAS
    public void filtrarPorPersonas(int minPersonas, int maxPersonas, BanqueteCallback<List<Banquete>> callback) {
        Log.d(TAG, "🔍 Filtrando banquetes por personas: " + minPersonas + "-" + maxPersonas);

        obtenerTodosBanquetes(new BanqueteCallback<List<Banquete>>() {
            @Override
            public void onSuccess(List<Banquete> banquetes) {
                java.util.List<Banquete> filtrados = new java.util.ArrayList<>();

                for (Banquete banquete : banquetes) {
                    int personas = banquete.getCantidadPersonas();
                    if (personas >= minPersonas && personas <= maxPersonas) {
                        filtrados.add(banquete);
                    }
                }

                Log.d(TAG, "✅ Filtrado por personas completado: " + filtrados.size() + " resultados");
                callback.onSuccess(filtrados);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ✅ OBTENER ESTADÍSTICAS DE USO
    public void obtenerEstadisticasUso() {
        Log.d(TAG, "📊 Estadísticas de uso de BanqueteManager:");
        Log.d(TAG, "   - Context: " + (context != null ? "✅" : "❌"));
        Log.d(TAG, "   - API: " + (banqueteApi != null ? "✅" : "❌"));
        Log.d(TAG, "   - Session: " + (sessionManager != null ? "✅" : "❌"));
        Log.d(TAG, "   - Autenticado: " + (sessionManager != null && sessionManager.isLoggedIn()));
    }

    // ✅ LIMPIAR CACHE (si implementas cache en el futuro)
    public void limpiarCache() {
        Log.d(TAG, "🧹 Limpiando cache de banquetes...");
        // TODO: Implementar limpieza de cache cuando sea necesario
    }

    // ✅ VERIFICAR CONECTIVIDAD
    public boolean hayConectividad() {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            Log.d(TAG, "🌐 Conectividad: " + (isConnected ? "✅" : "❌"));
            return isConnected;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error verificando conectividad", e);
            return false;
        }
    }

    // ✅ REINICIALIZAR MANAGER
    public static void reinicializar(Context context) {
        Log.d(TAG, "🔄 Reinicializando BanqueteManager...");
        instance = null;
        getInstance(context);
    }
}