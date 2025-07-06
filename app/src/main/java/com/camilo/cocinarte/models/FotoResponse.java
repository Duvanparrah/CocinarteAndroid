package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

public class FotoResponse {

    @SerializedName("url")
    private String url;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private boolean success;

    // Constructor vacío
    public FotoResponse() {}

    // Constructor con parámetros
    public FotoResponse(String url, String message, boolean success) {
        this.url = url;
        this.message = message;
        this.success = success;
    }

    // Getters y setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "FotoResponse{" +
                "url='" + url + '\'' +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}