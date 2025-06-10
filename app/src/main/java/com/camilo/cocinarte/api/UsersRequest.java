package com.camilo.cocinarte.api;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import okhttp3.*;

public class UsersRequest {

    private static final String URL = "http://192.168.18.7:5000/api/auth/login";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    public void login(String email, String password) {
        String jsonBody = "{"
                + "\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\""
                + "}";

        RequestBody body = RequestBody.create(jsonBody, JSON);

        Log.d("|||email", email);
        Log.d("|||pass", password);
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Error de red
                e.printStackTrace();
                Log.e("|||test", "error de conexion : "+e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("|||test", "Test exitoso: " + responseBody);

                    // Guardar cookies aquí
                } else {
                    Log.d("|||Error", "test error : " + response.code());
                }

            }
        });
    }


    public Context context;

    public void UserPrefsManager(Context context) {
        this.context = context;
    }

    public void changeName(String name) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", name);
        editor.apply(); // O .commit() si lo quieres sincrónico
    }

    public void changePassword(String password) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("password", password);
        editor.apply();
    }

    public String getSavedName() {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("username", ""); // "" es el valor por defecto si no hay nada guardado
    }

    public String getSavedPassword() {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("password", "");
    }

}



//package com.camilo.cocinarte.api;
//import static android.content.Context.MODE_PRIVATE;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.util.Log;
//
//import java.io.IOException;
//
//import okhttp3.*;
//
//public class UsersRequest {
//
//    private static final String URL = "http://192.168.18.7:5000/api/auth/login";
//    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
//
//    OkHttpClient client = new OkHttpClient();
//
//    public void login(String email, String password) {
//        String jsonBody = "{"
//                + "\"email\":\"" + email + "\","
//                + "\"password\":\"" + password + "\""
//                + "}";
//
//        RequestBody body = RequestBody.create(jsonBody, JSON);
//
//        Log.d("|||email", email);
//        Log.d("|||pass", password);
//        Request request = new Request.Builder()
//                .url(URL)
//                .post(body)
//                .build();
//
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                // Error de red
//                e.printStackTrace();
//                Log.e("|||test", "error de conexion : "+e.getMessage());
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String responseBody = response.body().string();
//                    Log.d("|||test", "Test exitoso: " + responseBody);
//
//                    // Guardar cookies aquí
//                } else {
//                    Log.d("|||Error", "test error : " + response.code());
//                }
//
//            }
//        });
//    }
//
//
//    public Context context;
//
//    public void UserPrefsManager(Context context) {
//        this.context = context;
//    }
//
//    public void changeName(String name) {
//        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("username", name);
//        editor.apply(); // O .commit() si lo quieres sincrónico
//    }
//
//    public void changePassword(String password) {
//        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("password", password);
//        editor.apply();
//    }
//
//    public String getSavedName() {
//        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
//        return prefs.getString("username", ""); // "" es el valor por defecto si no hay nada guardado
//    }
//
//    public String getSavedPassword() {
//        SharedPreferences prefs = context.getSharedPreferences("user_prefs", MODE_PRIVATE);
//        return prefs.getString("password", "");
//    }
//
//}
