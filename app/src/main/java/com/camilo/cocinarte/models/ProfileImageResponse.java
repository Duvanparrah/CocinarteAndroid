package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

public class ProfileImageResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    @SerializedName("user")
    private Usuario user;

    // Getters
    public String getMessage() {
        return message;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public Usuario getUser() {
        return user;
    }


    public static class Usuario {

        @SerializedName("id")
        private int id;

        @SerializedName("email")
        private String email;

        @SerializedName("nombre")
        private String nombre;

        @SerializedName("foto_perfil")
        private String fotoPerfil;

        @SerializedName("isVerified")
        private boolean isVerified;

        @SerializedName("tipo_usuario")
        private String tipoUsuario;

        // Getters
        public int getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getNombre() {
            return nombre;
        }

        public String getFotoPerfil() {
            return fotoPerfil;
        }

        public boolean isVerified() {
            return isVerified;
        }

        public String getTipoUsuario() {
            return tipoUsuario;
        }
    }
}