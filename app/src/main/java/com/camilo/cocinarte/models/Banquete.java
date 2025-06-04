package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

public class Banquete {
    @SerializedName("titulo")
    private String nombre;

    private int likes;
    private int comentarios;
    private int compartidos;

    private String descripcion;

    @SerializedName("imagen_url")
    private String imagenUrl;

    // Getters y setters

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getComentarios() { return comentarios; }
    public void setComentarios(int comentarios) { this.comentarios = comentarios; }

    public int getCompartidos() { return compartidos; }
    public void setCompartidos(int compartidos) { this.compartidos = compartidos; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}
