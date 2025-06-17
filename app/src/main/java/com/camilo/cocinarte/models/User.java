package com.camilo.cocinarte.models;

public class User {
    private int id_usuario;
    private String correo;
    private String nombre_usuario;
    private String foto_perfil;
    private String fecha_creacion;
    private boolean isVerified;
    private String tipo_usuario;

    // Constructor vacío
    public User() {}

    // Constructor completo
    public User(int id_usuario, String correo, String nombre_usuario, String foto_perfil,
                String fecha_creacion, boolean isVerified, String tipo_usuario) {
        this.id_usuario = id_usuario;
        this.correo = correo;
        this.nombre_usuario = nombre_usuario;
        this.foto_perfil = foto_perfil;
        this.fecha_creacion = fecha_creacion;
        this.isVerified = isVerified;
        this.tipo_usuario = tipo_usuario;
    }

    // Getters
    public int getId_usuario() {
        return id_usuario;
    }

    public String getCorreo() {
        return correo;
    }

    public String getNombre_usuario() {
        return nombre_usuario;
    }

    public String getFoto_perfil() {
        return foto_perfil;
    }

    public String getFecha_creacion() {
        return fecha_creacion;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getTipo_usuario() {
        return tipo_usuario;
    }

    // Setters
    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }

    public void setFoto_perfil(String foto_perfil) {
        this.foto_perfil = foto_perfil;
    }

    public void setFecha_creacion(String fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public void setTipo_usuario(String tipo_usuario) {
        this.tipo_usuario = tipo_usuario;
    }

    // Método para obtener las iniciales del nombre (para mostrar cuando no hay foto)
    public String getInitials() {
        if (nombre_usuario == null || nombre_usuario.trim().isEmpty()) {
            return "U"; // Usuario por defecto
        }

        String[] parts = nombre_usuario.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else {
            return nombre_usuario.substring(0, Math.min(2, nombre_usuario.length())).toUpperCase();
        }
    }

    // Método para verificar si tiene foto de perfil
    public boolean hasFotoPerfil() {
        return foto_perfil != null && !foto_perfil.trim().isEmpty();
    }
}