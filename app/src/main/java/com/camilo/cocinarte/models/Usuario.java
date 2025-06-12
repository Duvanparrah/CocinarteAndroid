package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Usuario implements Serializable  {

    @SerializedName("id_usuario")
    private int idUsuario;

    private String correo;

    @SerializedName("contrasena")
    private String contrasena;

    @SerializedName("nombre_usuario")
    private String nombreUsuario;

    @SerializedName("foto_perfil")
    private String fotoPerfil;

    @SerializedName("tipo_usuario")
    private String tipoUsuario;

    @SerializedName("fecha_creacion")
    private String fechaCreacion;

    // Getters y Setters

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}