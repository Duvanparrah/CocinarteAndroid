package com.camilo.cocinarte.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CloudinaryUploader {

    private static final String TAG = "CloudinaryUploader";
    private static final String CLOUD_NAME = "dryumffhy"; // Tu cloud name real
    private static final String UPLOAD_PRESET = "cocinarte_profile"; // Debes crear este preset en Cloudinary

    private final Context context;
    private final ExecutorService executor;
    private final OkHttpClient httpClient;

    public CloudinaryUploader(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        this.httpClient = new OkHttpClient();
    }

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public void uploadImage(Uri imageUri, UploadCallback callback) {
        executor.execute(() -> {
            try {
                // Leer archivo desde URI
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    callback.onError("No se pudo leer la imagen");
                    return;
                }

                byte[] imageBytes = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    imageBytes = inputStream.readAllBytes();
                }
                inputStream.close();

                // Crear request body para Cloudinary
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("upload_preset", UPLOAD_PRESET)
                        .addFormDataPart("folder", "cocinArte/perfiles") // Organizar en carpetas
                        .addFormDataPart("file", "profile_image.jpg",
                                RequestBody.create(imageBytes, MediaType.parse("image/*")))
                        .build();

                // URL de upload de Cloudinary
                String uploadUrl = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .build();

                Response response = httpClient.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Cloudinary response: " + responseBody);

                    // Parsear respuesta JSON para obtener URL
                    String imageUrl = parseImageUrl(responseBody);
                    if (imageUrl != null) {
                        // Ejecutar callback en el hilo principal
                        ((android.app.Activity) context).runOnUiThread(() ->
                                callback.onSuccess(imageUrl));
                    } else {
                        ((android.app.Activity) context).runOnUiThread(() ->
                                callback.onError("No se pudo obtener URL de la imagen"));
                    }
                } else {
                    String errorMsg = response.code() + ": " +
                            (response.body() != null ? response.body().string() : "Error desconocido");
                    Log.e(TAG, "Error en upload: " + errorMsg);
                    ((android.app.Activity) context).runOnUiThread(() ->
                            callback.onError(errorMsg));
                }

            } catch (Exception e) {
                Log.e(TAG, "Excepción en upload: " + e.getMessage());
                ((android.app.Activity) context).runOnUiThread(() ->
                        callback.onError("Error: " + e.getMessage()));
            }
        });
    }

    private String parseImageUrl(String jsonResponse) {
        try {
            // Buscar la URL en la respuesta JSON
            // Formato típico: "secure_url":"https://res.cloudinary.com/..."
            String searchPattern = "\"secure_url\":\"";
            int startIndex = jsonResponse.indexOf(searchPattern);
            if (startIndex != -1) {
                startIndex += searchPattern.length();
                int endIndex = jsonResponse.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return jsonResponse.substring(startIndex, endIndex);
                }
            }

            // Backup: buscar "url" si no encuentra "secure_url"
            searchPattern = "\"url\":\"";
            startIndex = jsonResponse.indexOf(searchPattern);
            if (startIndex != -1) {
                startIndex += searchPattern.length();
                int endIndex = jsonResponse.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    String url = jsonResponse.substring(startIndex, endIndex);
                    // Convertir a HTTPS si es HTTP
                    if (url.startsWith("http://")) {
                        url = url.replace("http://", "https://");
                    }
                    return url;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
        }
        return null;
    }
}