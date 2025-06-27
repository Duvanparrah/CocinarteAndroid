package com.camilo.cocinarte.models;

public class LoginResponse {
    private String token;
    private String message;
    private UserData user; // Mantendremos "UserData" del modelo original

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

    public String getMessage() {
        return message;
    }

    public UserData getUser() {
        return user;
    }

    public UserData getUsuario() {
        return user; // Método de compatibilidad
    }

    // Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    // Método helper para verificar si el login fue exitoso
    public boolean isSuccess() {
        return token != null && !token.isEmpty();
    }

    // Clase interna combinada para los datos del usuario
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

        public int getId_usuario() {
            return id; // Compatibilidad
        }

        public String getCorreo() {
            return email; // Compatibilidad
        }

        public String getNombre_usuario() {
            return nombre; // Compatibilidad
        }

        public String getFoto_perfil() {
            return foto; // Compatibilidad
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
