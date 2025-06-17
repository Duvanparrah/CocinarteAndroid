package com.camilo.cocinarte.api;

import android.content.Context;

import com.camilo.cocinarte.api.auth.AuthCookieJar;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiConfig {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(new AuthCookieJar(context))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://TU_BACKEND_URL/") // Reemplaza con tu URL real
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}