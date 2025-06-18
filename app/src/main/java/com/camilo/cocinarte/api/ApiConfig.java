package com.camilo.cocinarte.api;

import android.content.Context;

import com.camilo.cocinarte.api.auth.AuthCookieJar;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class ApiConfig {

    private static final String BASE_URL = "https://cocinarte-production.up.railway.app/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(new AuthCookieJar(context))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create()) // Para strings simples
                    .addConverterFactory(GsonConverterFactory.create())    // Para objetos como ResetPasswordRequest
                    .build();
        }
        return retrofit;
    }
}