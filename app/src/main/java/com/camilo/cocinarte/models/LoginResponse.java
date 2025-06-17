package com.camilo.cocinarte.models;

public class LoginResponse {
    private String token;
    private UserData user; // Cambió de "usuario" a "user"
    private String message;

    // Constructor vacío
    public LoginResponse() {}

    // Constructor completo
    public LoginResponse(String token, UserData user, String message) {
        this.token = token;
        this.user = user;
        this.message = message;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public UserData getUser() {
        return user;
    }

    public UserData getUsuario() {
        return user; // Método de compatibilidad
    }

    public String getMessage() {
        return message;
    }

    // Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Método helper para verificar si el login fue exitoso
    public boolean isSuccess() {
        return token != null && !token.isEmpty();
    }

    // Clase interna para los datos del usuario en la respuesta
    public static class UserData {
        private int id;
        private String email;
        private String nombre;
        private String foto;
        private boolean isVerified;
        private String tipo_usuario;

        // Constructor vacío
        public UserData() {}

        // Getters
        public int getId() {
            return id;
        }

        public int getId_usuario() {
            return id; // Método de compatibilidad
        }

        public String getEmail() {
            return email;
        }

        public String getCorreo() {
            return email; // Método de compatibilidad
        }

        public String getNombre() {
            return nombre;
        }

        public String getNombre_usuario() {
            return nombre; // Método de compatibilidad
        }

        public String getFoto() {
            return foto;
        }

        public String getFoto_perfil() {
            return foto; // Método de compatibilidad
        }

        public boolean isVerified() {
            return isVerified;
        }

        public String getTipo_usuario() {
            return tipo_usuario;
        }

        // Setters
        public void setId(int id) {
            this.id = id;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public void setFoto(String foto) {
            this.foto = foto;
        }

        public void setVerified(boolean verified) {
            isVerified = verified;
        }

        public void setTipo_usuario(String tipo_usuario) {
            this.tipo_usuario = tipo_usuario;
        }

        // Método helper para verificar si tiene foto
        public boolean hasFotoPerfil() {
            return foto != null && !foto.trim().isEmpty() && !foto.equals("null");
        }
    }
}