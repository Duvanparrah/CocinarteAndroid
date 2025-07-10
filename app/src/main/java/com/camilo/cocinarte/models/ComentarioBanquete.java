package com.camilo.cocinarte.models;

import java.io.Serializable;

public class ComentarioBanquete implements Serializable {
    private int id;
    private String contenido;
    private String fechaCreacion;
    private String fechaEdicion;
    private boolean editado;
    private int usuarioId;
    private String nombreUsuario;
    private String fotoPerfil;

    public ComentarioBanquete(int id, String contenido, String fechaCreacion, String fechaEdicion,
                              boolean editado, int usuarioId, String nombreUsuario, String fotoPerfil) {
        this.id = id;
        this.contenido = contenido;
        this.fechaCreacion = fechaCreacion;
        this.fechaEdicion = fechaEdicion;
        this.editado = editado;
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.fotoPerfil = fotoPerfil;
    }

    // Getters
    public int getId() { return id; }
    public String getContenido() { return contenido; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getFechaEdicion() { return fechaEdicion; }
    public boolean isEditado() { return editado; }
    public int getUsuarioId() { return usuarioId; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getFotoPerfil() { return fotoPerfil; }

    // Setters
    public void setContenido(String contenido) { this.contenido = contenido; }
    public void setFechaEdicion(String fechaEdicion) { this.fechaEdicion = fechaEdicion; }
    public void setEditado(boolean editado) { this.editado = editado; }
}