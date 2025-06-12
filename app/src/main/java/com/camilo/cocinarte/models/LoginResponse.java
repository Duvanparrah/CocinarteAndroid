package com.camilo.cocinarte.models;
// LoginResponse.java


public class LoginResponse {
    private String token;
    private String message;
    private User user;

    // Getters
    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    // Clase interna User
    public static class User {
        private int id;
        private String email;
        private String nombre;
        private String foto;
        private boolean isVerified;
        private String tipo_usuario;

        public int getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getNombre() {
            return nombre;
        }

        public String getFoto() {
            return foto;
        }

        public boolean isVerified() {
            return isVerified;
        }

        public String getTipo_usuario() {
            return tipo_usuario;
        }
    }
}
