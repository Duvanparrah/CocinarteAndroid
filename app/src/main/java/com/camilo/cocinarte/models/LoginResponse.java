package com.camilo.cocinarte.models;

public class LoginResponse {
    private String message;
    private String token;
    private Usuario usuario;
    // Getters y setters


    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}