package com.camilo.cocinarte.api;

import android.content.Context;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://cocinarte-backend-production.up.railway.app/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            // ✅ NUEVO: Configurar AuthInterceptor para manejo automático de tokens
            AuthInterceptor authInterceptor = null;
            try {
                authInterceptor = new AuthInterceptor(context);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            // Configurar logging para debug
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
                    Log.d(TAG, "HTTP: " + message)
            );
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configurar cliente HTTP
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    // ✅ AGREGAR AuthInterceptor PRIMERO (antes del logging)
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        // Agregar headers por defecto
                        return chain.proceed(
                                chain.request()
                                        .newBuilder()
                                        .addHeader("Content-Type", "application/json")
                                        .addHeader("Accept", "application/json")
                                        .build()
                        );
                    });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();

            Log.d(TAG, "✅ Retrofit configurado con AuthInterceptor para manejo automático de tokens");
        }
        return retrofit;
    }

    /**
     * 🔄 Método para reinicializar el cliente (útil después de logout)
     */
    public static void resetClient() {
        retrofit = null;
        Log.d(TAG, "🔄 Cliente Retrofit reinicializado");
    }
}