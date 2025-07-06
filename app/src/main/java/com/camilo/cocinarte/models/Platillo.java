package com.camilo.cocinarte.models;

public class Platillo {
    private String nombre_platillo;
    private String descripcion;
    private String imagenUrl;

    public Platillo() {
    }

    public Platillo(String nombre_platillo, String descripcion, String imagenUrl) {
        this.nombre_platillo = nombre_platillo;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
    }

    public String getNombre_platillo() {
        return nombre_platillo;
    }

    public void setNombre_platillo(String nombre_platillo) {
        this.nombre_platillo = nombre_platillo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}