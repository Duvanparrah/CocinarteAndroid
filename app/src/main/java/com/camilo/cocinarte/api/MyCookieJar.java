package com.camilo.cocinarte.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class MyCookieJar implements CookieJar {
    private static final String PREFS_NAME = "cookie_prefs";
    private static final String COOKIE_KEY = "cookie_token";
    private static final String TAG = "MyCookieJar";

    private final SharedPreferences sharedPreferences;
    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();
    private final String BASE_URL = "https://tuservidor.com";

    public MyCookieJar(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Cargar cookies persistidas
        Set<String> savedCookies = sharedPreferences.getStringSet(COOKIE_KEY, null);
        if (savedCookies != null) {
            HttpUrl baseUrl = HttpUrl.parse(BASE_URL);
            if (baseUrl != null) {
                for (String cookieString : savedCookies) {
                    Cookie cookie = Cookie.parse(baseUrl, cookieString);
                    if (cookie != null) {
                        addCookie(baseUrl.host(), cookie);
                    }
                }
            }
        }
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        String host = url.host();
        for (Cookie newCookie : cookies) {
            addCookie(host, newCookie);
            Log.d(TAG, "Saved cookie: " + newCookie.name() + " for " + host);
        }
        saveCookiesToPreferences();
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        String host = url.host();
        List<Cookie> validCookies = new ArrayList<>();

        List<Cookie> storedCookies = cookieStore.get(host);
        if (storedCookies != null) {
            for (Cookie cookie : storedCookies) {
                if (cookie.matches(url)) {
                    validCookies.add(cookie);
                }
            }
        }

        Log.d(TAG, "Sending " + validCookies.size() + " cookies for " + host);
        return validCookies;
    }

    public void clear() {
        cookieStore.clear();
        sharedPreferences.edit().remove(COOKIE_KEY).apply();
        Log.d(TAG, "All cookies cleared");
    }

    public void clearForDomain(String domain) {
        cookieStore.remove(domain);
        saveCookiesToPreferences();
        Log.d(TAG, "Cookies cleared for domain: " + domain);
    }

    public Cookie getCookie(String domain, String name) {
        List<Cookie> cookies = cookieStore.get(domain);
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.name().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private void addCookie(String host, Cookie cookie) {
        List<Cookie> cookies = cookieStore.getOrDefault(host, new ArrayList<>());
        cookies.removeIf(existingCookie -> existingCookie.name().equals(cookie.name()));
        cookies.add(cookie);
        cookieStore.put(host, cookies);
    }

    private void saveCookiesToPreferences() {
        Set<String> cookieStrings = new HashSet<>();
        for (List<Cookie> cookies : cookieStore.values()) {
            for (Cookie cookie : cookies) {
                cookieStrings.add(cookie.toString());
            }
        }
        sharedPreferences.edit().putStringSet(COOKIE_KEY, cookieStrings).apply();
    }
}
