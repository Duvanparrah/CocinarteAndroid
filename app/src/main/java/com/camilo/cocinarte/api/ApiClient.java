package com.camilo.cocinarte.api;

import android.content.Context;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.MyCookieJar;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(new MyCookieJar(context))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.18.7:5000/")




                    //.baseUrl("http://10.0.2.2:3000/") // Cambia a tu IP si es necesario
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}



//package com.camilo.cocinarte.api;
//
//import android.content.Context;
//
//import com.camilo.cocinarte.api.MyCookieJar;
//
//import okhttp3.OkHttpClient;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class ApiClient {
//    private static Retrofit retrofit = null;
//
//    public static Retrofit getClient(Context context) {
//        if (retrofit == null) {
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .cookieJar(new MyCookieJar(context))
//                    .build();
//
//            retrofit = new Retrofit.Builder()
//                    .baseUrl("http://192.168.18.7:5000/")
//
//
//
//
//                    //.baseUrl("http://10.0.2.2:3000/") // Cambia a tu IP si es necesario
//                    .client(client)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//        }
//        return retrofit;
//    }
//}
