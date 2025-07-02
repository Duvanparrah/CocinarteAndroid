package com.camilo.cocinarte.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class AuthCookieJar implements CookieJar {

    private static final String PREFS_NAME = "cookie_prefs";
    private static final String COOKIE_KEY = "cookie_token";

    private SharedPreferences sharedPreferences;
    private List<Cookie> cookies;

    // Usa la URL base de tu servidor para parsear las cookies guardadas
    private static final String BASE_URL = "https://tuservidor.com";

    public AuthCookieJar(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        cookies = new ArrayList<>();

        // Cargar cookies guardadas en SharedPreferences al crear la instancia
        Set<String> savedCookies = sharedPreferences.getStringSet(COOKIE_KEY, null);
        if (savedCookies != null) {
            HttpUrl baseUrl = HttpUrl.parse(BASE_URL);
            if (baseUrl != null) {
                for (String cookieString : savedCookies) {
                    Cookie cookie = Cookie.parse(baseUrl, cookieString);
                    if (cookie != null) {
                        cookies.add(cookie);
                    }
                }
            }
        }
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        this.cookies.clear();
        this.cookies.addAll(cookies);

        // Guardar cookies como String en SharedPreferences para persistencia
        Set<String> cookieStrings = new HashSet<>();
        for (Cookie cookie : cookies) {
            cookieStrings.add(cookie.toString());
        }
        sharedPreferences.edit().putStringSet(COOKIE_KEY, cookieStrings).apply();
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> validCookies = new ArrayList<>();
        for (Cookie cookie : cookies) {
            if (cookie.matches(url)) {
                validCookies.add(cookie);
            }
        }
        return validCookies;
    }
}
